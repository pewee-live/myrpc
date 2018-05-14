package com.pewee.rpc.core;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.pewee.rpc.clientmanage.ClientRegistor;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * 用于连接服务方
 * 该对象为单例
 * client连接server的过程被包装为MessageSendInitializeTask,这个方法只会在RpcServerConnector.load()执行一次,
 * 连接完成后通过channelFutuer的回调将MessageSendHandler放入RpcServerConnector,并将signal上等待的用户线程唤醒
 * MessageSendHandler会在代理中从RpcServerConnector get到,并用其中的方法发送请求,若MessageSendHandler为null则会让全部用户线程在signal上等待
 * @author pewee
 *
 */
public class RpcServerConnector {
	
	public volatile static RpcServerConnector INSTANCE  = new RpcServerConnector();
	
	private final static String DELIMITER = ":";
	
	 //方法返回到Java虚拟机的可用的处理器数量
    private final static int parallel = Runtime.getRuntime().availableProcessors() * 2;
    
    //netty nio线程池
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(parallel);
    private static ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) RpcThreadPoolCreater.getExecutor("rpc-client",16, -1);
    //这里送入MessageSendHandler 是为了在关闭客户端时方便messageSendHandler.close() 和初始化client线程池,及nio线程池一起关闭,达到安全关闭client的目的
    private MessageSendHandler messageSendHandler = null;
	
  //等待Netty服务端链路建立通知信号
    private Lock lock = new ReentrantLock();
    private Condition signal = lock.newCondition();
    
	private RpcServerConnector() {
		super();
	}
	
	public void load(String serverAddress) {
        String[] ipAddr = serverAddress.split(RpcServerConnector.DELIMITER);
        if (ipAddr.length == 2) {
            String host = ipAddr[0];
            int port = Integer.parseInt(ipAddr[1]);
            final InetSocketAddress remoteAddr = new InetSocketAddress(host, port);

            threadPoolExecutor.submit(new MessageSendInitializeTask(eventLoopGroup, remoteAddr, this));
        }
    }
	
	public void setMessageSendHandler(MessageSendHandler messageInHandler) {
        try {
            lock.lock();
            this.messageSendHandler = messageInHandler;
            System.out.println("messageSendHandler设置成功");
            //唤醒所有等待客户端RPC线程
            signal.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public MessageSendHandler getMessageSendHandler() throws InterruptedException {
        try {
            lock.lock();
            //Netty服务端链路没有建立完毕之前，先挂起等待
            if (messageSendHandler == null) {
            	 System.out.println("链路建立中,等待messageSendHandler设置");
                signal.await();
            }
            return messageSendHandler;
        } finally {
            lock.unlock();
        }
    }

    public void unLoad() {
    	System.out.print("开始关闭链路");
       // messageSendHandler.close();
        System.out.print("=");
        threadPoolExecutor.shutdown();
        System.out.print("=");
        eventLoopGroup.shutdownGracefully();
        ClientRegistor.getClient().close();
        System.out.println("关闭链路完成");
    }

	public Lock getLock() {
		return lock;
	}

	public void setLock(Lock lock) {
		this.lock = lock;
	}

	public Condition getSignal() {
		return signal;
	}

	public void setSignal(Condition signal) {
		this.signal = signal;
	}
	
	
	
}
