package test.startserver;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartServer {
	
public static void main(String[] args) {
	
		new ClassPathXmlApplicationContext("spring-rpc-netty.xml");
		Thread  thread = Thread.currentThread();
		try {
			thread.wait();
		} catch (InterruptedException e) {
			//主程序不退出
		}
}
}
