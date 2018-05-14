package com.pewee.rpc.core;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * 线程工厂
 * threadFactoryNumber 每实例化一个工厂会加1作为prefix
 * mThreadNum 每个工厂生产的线程数
 * daemoThread 是否守护线程 client的将Netty操作封装为task异步进行,可以作为守护线程
 * threadGroup 所属线程组
 * @author pewee
 *
 */
public class RpcThreadFactory implements ThreadFactory{
	
	private static final AtomicInteger threadFactoryNumber = new AtomicInteger(1);

    private final AtomicInteger mThreadNum = new AtomicInteger(1);

    private final String prefix;

    private final boolean daemoThread;

    private final ThreadGroup threadGroup;
    
    
    public RpcThreadFactory() {
        this("rpcserver-threadpool-" + threadFactoryNumber.getAndIncrement(), false);
    }

    public RpcThreadFactory(String prefix) {
        this(prefix, false);
    }

    public RpcThreadFactory(String prefix, boolean daemo) {
        this.prefix = prefix + "-thread-";
        daemoThread = daemo;
        SecurityManager s = System.getSecurityManager();
        threadGroup = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    public Thread newThread(Runnable runnable) {
        String name = prefix + mThreadNum.getAndIncrement();
        Thread ret = new Thread(threadGroup, runnable, name, 0);
        ret.setDaemon(daemoThread);
        return ret;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
