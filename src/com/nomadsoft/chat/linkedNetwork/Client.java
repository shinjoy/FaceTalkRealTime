package com.nomadsoft.chat.linkedNetwork;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.nomadsoft.chat.config.ServerList;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.ServerInfo;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatTimer;
import com.nomadsoft.chat.openServer.ChatUserContext;
import com.nomadsoft.chat.utility.IntByteConvert;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client extends Thread{
	
	final ConcurrentHashMap<String, Channel> clientChannelMap = new ConcurrentHashMap<String, Channel>();
	
	EventLoopGroup group = null;
	Bootstrap bootstrap = null;
	
	
	public Bootstrap getBootstrap() {
		return bootstrap;
	}

	public void setBootstrap(Bootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	static Client client = new Client();
	public static Client getInstance() {
		
		return client;
	}
	
	public ConcurrentHashMap<String, Channel> getClientChannelMap() {
		return clientChannelMap;
	}

	public Client() {
		group = new NioEventLoopGroup();
		
		Bootstrap strap = new Bootstrap();
		bootstrap = strap.group(group);		
		bootstrap = bootstrap.channel(NioSocketChannel.class);       
		
        ClientPacketBufferThread clientPacketBufferThread = ClientPacketBufferThread.getInstance();
        clientPacketBufferThread.start();
        
        ClientBufferConsumerThread clientBufferConsumerThread = ClientBufferConsumerThread.getInstance();
        clientBufferConsumerThread.start();        
	}
	
	public void close() {
		try {
			if(group != null) {
				group.shutdownGracefully();	
			}	
		} catch (Exception e) {
			// TODO: handle exception
		}				
	}
	
	public void run() {
		
		try {	          
	          try {	              
	              // Make the connection attempt.
	        	  ConcurrentHashMap<String, ServerInfo> serverList = ServerList.getInstance().getRemoteServerList();
	        	  
	        	  for(ServerInfo serverInfo : serverList.values()) {
	        		  if(serverInfo.getStatus() == 1) {
		        		  Bootstrap strap = bootstrap.handler(new ClientHandler(serverInfo.getServer_id()));	        	  
			              
		        		  Log.i("run client");
		        		  Channel channel = strap.connect(serverInfo.getAddress(), serverInfo.getPort()).sync().channel();
			              
			              clientChannelMap.put(serverInfo.getServer_id(), channel);			              		              
	        		  }
	        	  }
	        	  
 
	          } finally {
	              //타이머 실행
	              Timer timer = new Timer();
	              timer.schedule(new ClientTimer(), new Date(), 10000);  	              
	          }
			
		} catch (Exception e) {
			//e.printStackTrace();
			e.printStackTrace();
		} finally {
		}
	}
}
