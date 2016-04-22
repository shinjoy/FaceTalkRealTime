package com.nomadsoft.chat.openServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

import com.google.gson.Gson;
import com.nomadsoft.chat.db.dto.Packet;
import com.nomadsoft.chat.utility.IntByteConvert;

public class PacketEncoder extends MessageToByteEncoder<Packet> {

    // TODO Use CharsetEncoder instead.
    /**
     * Creates a new instance with the current system character set.
     */
    public PacketEncoder() {

    }
/*
    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) throws Exception {
    	Gson gson = new Gson();
		String resMsg = gson.toJson(msg.getParam());

		byte[] data = resMsg.getBytes("UTF-8");		
		int size = data.length + 8 + 4 + 2;
		
		
		
		//ByteBuf buf = ctx.alloc().buffer(size);	
	
		
		ByteBuffer ba = ByteBuffer.allocate(size);
		
		ba.put(ChatServerHandler.packetHeader, 0, 8);
		ba.put((byte) msg.getPt());
		ba.put((byte) msg.getSt());
		
		byte[] sizeData = IntByteConvert.toByteArray(data.length);
		
		ba.put(sizeData);
		ba.put(data);
		
		
		ByteBuf buf = ctx.alloc().buffer(size + 1);		
		buf.setBytes(0, ChatServerHandler.packetHeader);
		buf.setByte(9, msg.getPt());
		buf.setByte(10, msg.getSt());
		buf.setBytes(11, sizeData);
		buf.setBytes(15, data);
		
		
		byte[] rHeader = new byte[8]; 
		buf.getBytes(0, rHeader, 0, 8);		
		
		ByteBuf btb = ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap("sssadf asdf")
        		, Charset.forName("UTF-8"));
		
		btb.setIndex(readerIndex, writerIndex)
        out.add(btb);
    }
*/
    
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out)
			throws Exception {
		// TODO Auto-generated method stub
		out.writeBytes(ChatServerHandler.packetHeader);
		ctx.flush();
		
		//out = ctx.alloc().buffer(8);
		//out.setBytes(0, ChatServerHandler.packetHeader);
	}
}
