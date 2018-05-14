package com.pewee.rpc.servicemanage;

import java.util.List;
import java.util.Properties;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * 自用框架服务治理の服务端实现
 * 提供服务注册功能
 * @author pccc
 *
 */
public class ServiceRegistor {
	
	private static final Properties sysproperties;
	
	private static final String localhost;
	
	public static final String rpcsocket;
	
	private static final String servicezoopath = "/peweeRpcServicesUrlList";
	
	private static CuratorFramework client;
	
	static {
		/**
		 * 1.初始化ip,端口
		 */
		sysproperties = System.getProperties();
		localhost = NetUtils.getLocalHost();
		rpcsocket = localhost+ ":12345";
		sysproperties.put("RPCserverAddress", rpcsocket);
		System.out.println("系统配置:" + sysproperties);
		
		/**
		 * 2.初始化zkclient
		 */
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(
				1000, 10);
		String zooaddr = (String) PropertiesUtil.getProps().get("rpc.server.zookeeper.addr");
		System.out.println("zoo地址:" + zooaddr);
		client = CuratorFrameworkFactory.newClient(zooaddr, 10000,
				5000, retryPolicy);
		System.out.println("初始化client" + client);
		client.getConnectionStateListenable().addListener(
				new ConnectionStateListener() {
					public void stateChanged(CuratorFramework client,
							ConnectionState state) {
						if (state == ConnectionState.LOST) {
							System.out.println("丢失连接");
						} else if (state == ConnectionState.CONNECTED) {
							System.out.println("已经连接"  + client.getState());
							repushurl();
						} else if (state == ConnectionState.RECONNECTED) {
							System.out.println("重连client" + client);
							System.out.println("重新连接");
							repushurl();
						}

					}

				});
		client.start();
		
		/**
		 * 3.初始化服务挂载的父节点
		 * 
		 */
		try {
			Stat stat = client.checkExists().forPath(servicezoopath);
			System.out.println(stat);
			if(null == stat){
				System.out.println("开始初始化"+ servicezoopath + "路径,用于挂载服务");
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
				.inBackground(new BackgroundCallback() {
					
					@Override
					public void processResult(CuratorFramework paramCuratorFramework,
							CuratorEvent paramCuratorEvent) throws Exception {
						// TODO Auto-generated method stub
						System.out.println(servicezoopath + "已初始化");
					}
				})
				.forPath(servicezoopath,"Urls".getBytes());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static CuratorFramework getClient() {
		return client;
	}
	
	
	/**
	 * 挂载本机url于zk下
	 */
	private static void repushurl() {
		// TODO Auto-generated method stub
		/**
		 * 开始在指定zoo路径下挂载服务提供者的Url
		 */
		System.out.println("=================");
		System.out.println("开始推送本机地址!!!");
		/*try {
			Stat stat = client.checkExists().forPath(servicezoopath);
			if(null == stat){
				System.out.println("开始初始化"+ servicezoopath + "路径");
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
				.inBackground(new BackgroundCallback() {
					
					@Override
					public void processResult(CuratorFramework paramCuratorFramework,
							CuratorEvent paramCuratorEvent) throws Exception {
						// TODO Auto-generated method stub
						System.out.println(servicezoopath + "已初始化");
					}
				})
				.forPath(servicezoopath,"Urls".getBytes());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		try {
			final String zoolocalUrl = servicezoopath + "/" + rpcsocket;
			Stat stat = client.checkExists().forPath(zoolocalUrl);
			if(null == stat){
				client.create().withMode(CreateMode.EPHEMERAL)
				.inBackground(new BackgroundCallback() {
					
					@Override
					public void processResult(CuratorFramework paramCuratorFramework,
							CuratorEvent paramCuratorEvent) throws Exception {
						// TODO Auto-generated method stub
						System.out.println(zoolocalUrl + "已初始化");
					}
				})
				.forPath(zoolocalUrl,rpcsocket.getBytes());
			} else {
				client.setData().inBackground(new BackgroundCallback() {
					
					@Override
					public void processResult(CuratorFramework paramCuratorFramework,
							CuratorEvent paramCuratorEvent) throws Exception {
						// TODO Auto-generated method stub
						System.out.println(zoolocalUrl + "已更新");
					}
				}).forPath(zoolocalUrl, rpcsocket.getBytes());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("推送本机地址完成");
		try {
			List<String> list = client.getChildren().forPath(servicezoopath);
			System.out.println(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("====================");
	}

	public static void main(String[] args) {
		try {
			Stat stat = client.checkExists().forPath(servicezoopath);
			System.out.println(stat);
			List<String> list = client.getChildren().forPath(servicezoopath);
			System.out.println(list);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
