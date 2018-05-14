package com.pewee.rpc.clientmanage;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;


/**
 * 客户端拉取服务
 * @author pccc
 *
 */
public class ClientRegistor {
	
private static final Properties sysproperties;
	
	private static final String localhost;
	
	public static final String clientrpcsocket;
	
	private static final String servicezoopath = "/peweeRpcServicesUrlList";
	
	private static final String clientzoopath = "/peweeRpcClientUrlList";
	
	private static final CuratorFramework client;
	
	private static final List<String> list = new CopyOnWriteArrayList<String>();
	
	public static List<String> getUrlList() {
		return list;
	}

	static {
		
		/**
		 * 1.初始化 client ip,端口
		 */
		sysproperties = System.getProperties();
		localhost = NetUtils.getLocalHost();
		clientrpcsocket = localhost+ ":12345";
		sysproperties.put("RPCclientAddress", clientrpcsocket);
		System.out.println("系统配置:" + sysproperties);
		
		/**
		 * 2.初始化zkclient
		 */
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(
				1000, 10);
		String zooaddr = (String) PropertiesUtil.getProps().get("rpc.client.zookeeper.addr");
		System.out.println("zoo地址:" + zooaddr);
		client = CuratorFrameworkFactory.newClient(zooaddr, 10000,
				5000, retryPolicy);
		client.getConnectionStateListenable().addListener(
				new ConnectionStateListener() {
					public void stateChanged(CuratorFramework client,
							ConnectionState state) {
						if (state == ConnectionState.LOST) {
							System.out.println("丢失连接");
						} else if (state == ConnectionState.CONNECTED) {
							System.out.println("已经连接"  + client.getState());
						} else if (state == ConnectionState.RECONNECTED) {
							System.out.println("重新连接");
						}

					}

				});
		client.start();
		
		/**
		 * 开始在指定zoo路径下挂载客户端的Url
		 */
		try {
			Stat stat = client.checkExists().forPath(clientzoopath);
			if(null == stat){
				client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
				.inBackground(new BackgroundCallback() {
					
					@Override
					public void processResult(CuratorFramework paramCuratorFramework,
							CuratorEvent paramCuratorEvent) throws Exception {
						// TODO Auto-generated method stub
						System.out.println(clientzoopath + "已初始化");
					}
				})
				.forPath(clientzoopath,"Urls".getBytes());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			final String zoolocalUrl = clientzoopath + "/" + clientrpcsocket;
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
				.forPath(zoolocalUrl,clientrpcsocket.getBytes());
			} else {
				client.setData().inBackground(new BackgroundCallback() {
					
					@Override
					public void processResult(CuratorFramework paramCuratorFramework,
							CuratorEvent paramCuratorEvent) throws Exception {
						// TODO Auto-generated method stub
						System.out.println(zoolocalUrl + "已更新");
					}
				}).forPath(zoolocalUrl, clientrpcsocket.getBytes());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/**
		 * 添加对服务者的监听
		 */
		PathChildrenCache childrenCache = new PathChildrenCache(client, servicezoopath, true);
		try {
			childrenCache.start(StartMode.POST_INITIALIZED_EVENT);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
					throws Exception {
				// TODO Auto-generated method stub
				System.out.println("服务者节点改变!!将启用策略!!!" + event.getType() + ">>"
				+ event.getData().getPath() + ">>" + new String(event.getData().getData()));
				refreshList();
				System.out.println("list刷新后:" + list);
			}

			private void refreshList() {
				// TODO Auto-generated method stub
				list.clear();
				GetChildrenBuilder childrenBuilder = client.getChildren();
				try {
					List<String> listdir = childrenBuilder.forPath(servicezoopath);
					for (String string : listdir) {
						byte[] data = client.getData().forPath(servicezoopath + "/" + string);
						list.add(new String(data));
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
		/**
		 * 拉取服务者的urls
		 */
		GetChildrenBuilder childrenBuilder = client.getChildren();
		try {
			List<String> listdir = childrenBuilder.forPath(servicezoopath);
			for (String string : listdir) {
				byte[] data = client.getData().forPath(servicezoopath + "/" + string);
				list.add(new String(data));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("初始化list:" + list);
		
	}
	
	public static CuratorFramework getClient() {
		return client;
	}

	
}
