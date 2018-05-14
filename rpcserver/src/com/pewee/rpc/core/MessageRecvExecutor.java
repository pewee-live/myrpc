package com.pewee.rpc.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.pewee.rpc.anno.Implbean;
import com.pewee.rpc.anno.InterfaceGetter;
import com.pewee.rpc.servicemanage.ServiceRegistor;


public class MessageRecvExecutor implements ApplicationContextAware, InitializingBean {

    private String serverAddress;
    private final static String DELIMITER = ":";

    private static ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) RpcThreadPoolCreater.getExecutor("serverbusinesspool", 16, -1);;

    public MessageRecvExecutor() {
    	this.serverAddress = ServiceRegistor.rpcsocket;;
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    	Map<String, Object> map = ctx.getBeansWithAnnotation(Implbean.class);
    	System.out.println("共" + map.size() + "实体类");
    	Iterator<String> iterator = map.keySet().iterator();
    	while(iterator.hasNext()){
    		System.out.println("============");
    		Object bean = map.get(iterator.next());
    		Class<?>[] interfaces = InterfaceGetter.getInterfaces(bean.getClass());
    		for (Class<?> class1 : interfaces) {
    			Interface2BeanIndecator.handlerMap.put(class1.getName(), bean);
			}
    		
    		System.out.println("============");
    	}
    	
    }

    public void afterPropertiesSet() throws Exception {
        //netty的线程池模型设置成主从线程池模式，这样可以应对高并发请求
        //当然netty还支持单线程、多线程网络IO模型，可以根据业务需求灵活配置
        ThreadFactory threadRpcFactory = new RpcThreadFactory("NettyRPC ThreadFactory");
        
        //方法返回到Java虚拟机的可用的处理器数量
        int parallel = Runtime.getRuntime().availableProcessors() * 2;
    
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup(parallel,threadRpcFactory,SelectorProvider.provider());
        
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker).channel(NioServerSocketChannel.class)
                    .childHandler(new MessageRecvChannelInitializer(Interface2BeanIndecator.handlerMap))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] ipAddr = serverAddress.split(MessageRecvExecutor.DELIMITER);

            if (ipAddr.length == 2) {
                String host = ipAddr[0];
                int port = Integer.parseInt(ipAddr[1]);
                ChannelFuture future = bootstrap.bind(host, port).sync();
                System.out.printf("[author pewee] Netty RPC Server start success ip:%s port:%d\n", host, port);
                future.channel().closeFuture().sync();
            } else {
                System.out.printf("[author pewee] Netty RPC Server start fail!\n");
            }
        } finally {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
        }
    }
}


