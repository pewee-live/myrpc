package com.pewee.rpc.anno;

public class InterfaceGetter {
	
	public static Class<?>[] getInterfaces(Class<?> clazz){
		
		String name = clazz.getName();
		Implbean annotation = clazz.getAnnotation(Implbean.class);
		System.out.println(name + "指定一个唯一接口" +annotation.interfaces() );
		Class<?>[] interfaces = clazz.getInterfaces();
		for (Class<?> class1 : interfaces) {
			System.out.println(name + "的全部接口为:" + class1);
		}
		
		return interfaces;
		
	}
	
}
