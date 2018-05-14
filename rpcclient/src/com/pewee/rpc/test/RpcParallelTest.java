package com.pewee.rpc.test;

import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.time.StopWatch;

import com.pewee.rpc.core.MessageSendExecutor;

public class RpcParallelTest {

    public static void main(String[] args) throws Exception {
       /*final MessageSendExecutor executor = new MessageSendExecutor("127.0.0.1:18888");
        //并行度10000
        int parallel = 10000;

        //开始计时
        StopWatch sw = new StopWatch();
        sw.start();

        CountDownLatch signal = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(parallel);

        for (int index = 0; index < parallel; index++) {
            CalcParallelRequestThread client = new CalcParallelRequestThread(executor, signal, finish, index);
            new Thread(client).start();
        }
        
        //10000个并发线程瞬间发起请求操作
        signal.countDown();
        finish.await();
        
        sw.stop();

        String tip = String.format("RPC调用总共耗时: [%s] 毫秒", sw.getTime());
        System.out.println(tip);

        executor.stop();*/
    	
    	final MessageSendExecutor executor = new MessageSendExecutor();
    	Calculate calc = executor.execute(Calculate.class,2L);
        int add = calc.add(100, 200);
        System.out.println("calc add result:[" + add + "]");
        executor.stop();
    	
    }
}
