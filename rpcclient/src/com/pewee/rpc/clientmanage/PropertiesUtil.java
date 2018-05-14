package com.pewee.rpc.clientmanage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.io.Closer;

public class PropertiesUtil {
	
	private static final Properties prop;
	
	static {
		/**
		 * 初始化配置
		 */
		prop = new Properties();
		InputStream inputStream = PropertiesUtil.class.getClassLoader().getResourceAsStream("rpc-client.properties");
		Closer closer = Closer.create();
		closer.register(inputStream);
		try {
			prop.load(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				closer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(prop);
	}
	
	private PropertiesUtil(){
		
	}
	
	public static Properties getProps(){
		return prop;
	}
	
		
}
