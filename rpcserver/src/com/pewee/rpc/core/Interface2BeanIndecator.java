package com.pewee.rpc.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 用于指示接口和实现类的map
 * @author pewee
 *
 */
public class Interface2BeanIndecator {
	
	public static final Map<String,Object> handlerMap = new ConcurrentHashMap<>();
	
}
