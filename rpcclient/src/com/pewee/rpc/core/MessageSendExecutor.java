package com.pewee.rpc.core;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Random;

import com.pewee.rpc.clientmanage.ClientRegistor;

/**
 * 执行器,用反射来生成代理接口的子类
 * 该类为单例
 * 其中导入RpcServerConnector用于管理框架开关
 * @author pewee
 *
 */
public class MessageSendExecutor {
	
	private RpcServerConnector conn = RpcServerConnector.INSTANCE;
	
	
	/**
	 * 单机版
	 * @param serverAddress
	 */
    public MessageSendExecutor(String serverAddress) {
    	conn.load(serverAddress);
    }
    
    /**
     * 采用负载均衡
     * @throws Exception 
     */
    public MessageSendExecutor() throws Exception {
    	List<String> urlList = ClientRegistor.getUrlList();
    	if(urlList.size() <= 0){
    		throw new Exception("错误:没有可用服务!!!!检查服务是否启动??");
    	}
    	String url = urlList.get(new Random().nextInt(urlList.size()));
    	System.out.println("负载均衡后:" + url);
    	conn.load(url);
    }

    public void stop() {
    	conn.unLoad();
    }
    
    /**
     * 传入接口的class
     * @param rpcInterface
     * @return
     */
    public static <T> T execute(Class<T> rpcInterface) {
        return (T) Proxy.newProxyInstance(
                rpcInterface.getClassLoader(),
                new Class<?>[]{rpcInterface},
                new MessageSendProxy<T>(rpcInterface)
        );
    }
    
    public static <T> T execute(Class<T> rpcInterface,Long waitTime) {
        return (T) Proxy.newProxyInstance(
                rpcInterface.getClassLoader(),
                new Class<?>[]{rpcInterface},
                new MessageSendProxy<T>(rpcInterface,waitTime)
        );
    }
	
}
