package com.pewee.rpc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import com.pewee.rpc.entity.MessageRequest;

public class MessageSendProxy<T> implements InvocationHandler{
	
	private Class<T> cls;
	
	private Long waittime;

    public MessageSendProxy(Class<T> cls) {
        this.cls = cls;
    }
	
	public MessageSendProxy(Class<T> cls, Long waittime) {
		this.cls = cls;
		this.waittime = waittime;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MessageRequest request = new MessageRequest();
        request.setMessageId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setTypeParameters(method.getParameterTypes());
        request.setParameters(args);

        MessageSendHandler handler = RpcServerConnector.INSTANCE.getMessageSendHandler();
        MessageCallBack callBack;
        if(waittime > 0L){
        	callBack = handler.sendRequest(request,waittime);
        } else {
        	callBack = handler.sendRequest(request);
        }
        
        return callBack.start();
	}

}
