package com.nomadsoft.chat.openServer;

//https://github.com/netty/netty/wiki/User-guide-for-5.x
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.util.Date;
import java.util.Timer;
import java.util.concurrent.Executors;

/**
 * Discards any incoming data.
 */
public class ChatServer {
	
    private final int port;

    public ChatServer(int port) {
        this.port = port;
    }
	 
	 
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
       	    final GlobalTrafficShapingHandler gtsh = new GlobalTrafficShapingHandler(                			                 			 
    			 Executors.newScheduledThreadPool(4), 0, 0, GlobalTrafficShapingHandler.DEFAULT_CHECK_INTERVAL);
        	
        	
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {                	                 	 
                	 ch.pipeline().addLast("readTimeoutHandler", new ReadTimeoutHandler(120));                                    	 
                	 ch.pipeline().addLast("globalTrafficShapingHandler", gtsh);
                	 
                	 ch.pipeline().addLast("frameDecoder", new ChatDecoder());
                	 ch.pipeline().addLast(new ChatServerHandler());
                 }
             });           

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync();

            
            //타이머 실행
            Timer timer = new Timer();
            timer.schedule(new ChatTimer(gtsh), new Date(), 60000); //1분에 한번.
            
            
            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
