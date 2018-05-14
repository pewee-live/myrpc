# **MyRpc**
1. MyRpc是一个基于netty4.0开发的分布式RPC框架。采用zookeeper来做服务治理。  
2. 只实现了最基本的功能，其他功能需要自己实现或修改，比如负载均衡策略等。

# 如何使用
1. 下载zookeeper，配置并启动；
2. 在客户端和服务端的classpath下的properties文件中配置zookeeper地址
3. 启动服务端
4. 使用客户端测试
