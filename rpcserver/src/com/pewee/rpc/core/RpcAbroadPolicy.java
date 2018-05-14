package com.pewee.rpc.core;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
/**
 * RPC 服务器,或客户端队列满时的拒绝策略,客户端是一请求
 * @author pewee
 *
 */
public class RpcAbroadPolicy extends AbortPolicy{
	
	private final String threadName;

    public RpcAbroadPolicy(String threadName) {
        this.threadName = threadName;
    }

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
		// TODO Auto-generated method stub
		String msg = String.format("RpcServer["
                + " Thread Name: %s, Pool Size: %d (active: %d, core: %d, max: %d, largest: %d), Task: %d (completed: %d),"
                + " Executor status:(isShutdown:%s, isTerminated:%s, isTerminating:%s)]",
                threadName, e.getPoolSize(), e.getActiveCount(), e.getCorePoolSize(), e.getMaximumPoolSize(), e.getLargestPoolSize(),
                e.getTaskCount(), e.getCompletedTaskCount(), e.isShutdown(), e.isTerminated(), e.isTerminating());
        System.out.println(msg);
        throw new RejectedExecutionException(msg);
		
	}
	
    
    
}
