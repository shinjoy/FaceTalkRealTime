package com.nomadsoft.chat.linkedNetwork;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.nomadsoft.chat.config.ServerList;
import com.nomadsoft.chat.db.dto.Param;
import com.nomadsoft.chat.db.dto.ServerInfo;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.openServer.ChatServerHandler;
import com.nomadsoft.chat.openServer.ProtocolType;
import com.nomadsoft.chat.utility.IntByteConvert;

public class ClientHandler extends SimpleChannelInboundHandler<Object>{

    private static final Logger logger = Logger.getLogger(
            ClientHandler.class.getName());

    private ChannelHandlerContext ctx;
    private String serverId = "";
    
    public ClientHandler(String serverId) {
    	this.serverId = serverId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)
            throws Exception {
        this.ctx = ctx;

        // Initialize the message.
        //content = ctx.alloc().directBuffer().writeZero(messageSize);

        // Send the initial messages.
        
        ServerInfo serverInfo = ServerList.getInstance().getLocalServer();        
        if(serverInfo != null) {
	        Param packet = new Param();
	        packet.put("id", serverInfo.getServer_id());
	        packet.put("authCode", serverInfo.getAuth_code());
	        packet.put("mode", "1");
	        
			ChatServerHandler.sendData(ctx.channel(), ProtocolType.ptRemoteHeartBeat
					, ProtocolType.stRemoteHeartBeatReq, packet);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        // Server is supposed to send nothing, but if it sends something, discard it.
    	try {        	
    		ByteBuf in = (ByteBuf) msg;
        	if (in.capacity() > 14) {    		
        		
        		byte[] rHeader = new byte[8]; 
    			in.getBytes(0, rHeader, 0, 8);
    			
    			if (ChatServerHandler.packetHeader[0] == in.getByte(0) &&
    					ChatServerHandler.packetHeader[1] == in.getByte(1) &&
    					ChatServerHandler.packetHeader[2] == in.getByte(2) &&
						ChatServerHandler.packetHeader[3] == in.getByte(3) &&
						ChatServerHandler.packetHeader[4] == in.getByte(4) &&
						ChatServerHandler.packetHeader[5] == in.getByte(5) &&
						ChatServerHandler.packetHeader[6] == in.getByte(6) &&
						ChatServerHandler.packetHeader[7] == in.getByte(7)
    					) {
    			
    	    		int pt = in.getByte(8);
    	    		int st = in.getByte(9);    
    	    		
    	    		int size = IntByteConvert.fromByte(in.getByte(10), in.getByte(11), in.getByte(12), in.getByte(13));    		
    	    		int rSize = in.capacity() - 6-8;
    	    		
    	    		if(rSize >= size) {
    	    			byte[] data = new byte[size];
    	        		in.getBytes(14, data, 0, size);    	        		
    	    			JsonElement jo = ChatServerHandler.formatJsonData(data);
						
    	        		ClientEventHandler.onReceived(ctx.channel(), pt, st, jo, serverId);    	        		
    	    		} else {
    	    			//µ¥ÀÌÅÍ ´úµé¾î¿È.
    	    		}   
    			}		
        	}    
        	
		} catch (Exception e) {
			// TODO: handle exception
	    	Log.l(Level.WARNING,
	                "Unexpected exception from downstream.", e);
		}
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,
            Throwable cause) throws Exception {
        // Close the connection when an exception is raised.
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.",
                cause);
        ctx.close();
    }

}

