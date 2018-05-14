package com.pewee.rpc.core;

import java.net.InetSocketAddress;
import java.util.concurrent.locks.Lock;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 在客户端发送请求之前的一些初始化工作(连接Server建立channel)
 * 该任务被包装秤task来异步线程完成
 * 该任务只会完成一次>>1个client连接server
 * @author pewee
 *
 */
public class MessageSendInitializeTask implements Runnable{
	
	private EventLoopGroup eventLoopGroup = null;
    private InetSocketAddress serverAddress = null;
    private RpcServerConnector conn = null;
	
    
    MessageSendInitializeTask(EventLoopGroup eventLoopGroup, InetSocketAddress serverAddress, RpcServerConnector conn) {
        this.eventLoopGroup = eventLoopGroup;
        this.serverAddress = serverAddress;
        this.conn = conn;
    }
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("开始初始化连接server task 线程!!!");
		Bootstrap b = new Bootstrap();
        b.group(eventLoopGroup)
                .channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new MessageSendChannelInitializer());

        ChannelFuture channelFuture = b.connect(serverAddress);
        channelFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(final ChannelFuture channelFuture) throws Exception {
            	System.out.println("客户端与服务端连接完成回调逻辑");
                if (channelFuture.isSuccess()) {
                	System.out.println("开始回调设置MessageSendHandler");
                    MessageSendHandler handler = channelFuture.channel().pipeline().get(MessageSendHandler.class);
                    MessageSendInitializeTask.this.conn.setMessageSendHandler(handler);
                } else {
                	System.out.println("连接建立不成功!!");
                	MessageSendInitializeTask.this.conn.unLoad();
                	try {
                		System.out.println("终止等待线程");
						Lock lock = MessageSendInitializeTask.this.conn.getLock();
						lock.lock();
						MessageSendInitializeTask.this.conn.getSignal().signalAll();
						lock.unlock();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.out.println("连接失败放弃请求");
					}
                	MessageSendInitializeTask.this.conn = null;
                }
            }
        });
		
	}
	
	
	
}
