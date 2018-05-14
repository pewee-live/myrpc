package com.pewee.rpc.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.pewee.rpc.entity.MessageRequest;
import com.pewee.rpc.entity.MessageResponse;

/**
 * 每个方法的调用会生成一个该对象
 * 请求在sedReq发送req后会柱塞等待结果
 * 其中over为ChannelHandler.channelRead()中返回resp,则会掉over()结束请求等待
 * @author pewee
 *
 */
public class MessageCallBack {
	
	
	private MessageRequest request;
    private MessageResponse response;
    private Lock lock = new ReentrantLock();
    private Condition finish = lock.newCondition();
    private Long waittime;

    public MessageCallBack(MessageRequest request) {
        this.request = request;
        this.waittime = 0x0000000AL;
    }
    
    public MessageCallBack(MessageRequest request,Long waittime) {
		this.request = request;
		this.waittime = waittime;
	}

    public Object start() throws InterruptedException {
        try {
            lock.lock();
            //设定一下超时时间，rpc服务器太久没有相应的话，就默认返回空吧。
            finish.await(waittime*1000, TimeUnit.MILLISECONDS);//这个参数应该由调用方可自己设置,有默认值即可
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    public void over(MessageResponse reponse) {
        try {
            lock.lock();
            finish.signal();
            this.response = reponse;
        } finally {
            lock.unlock();
        }
    }
    
}
