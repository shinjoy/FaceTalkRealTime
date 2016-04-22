package com.nomadsoft.chat.openServer;

import java.util.List;

import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.utility.IntByteConvert;
import com.nomadsoft.chat.utility.MsgKey;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ChatDecoder extends ByteToMessageDecoder{
	
	byte[] buffer = null;
	ByteBuf in = null;
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf input,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		
		if(buffer == null) {
			in = input;	
		} else {
			ByteBuf bb = ctx.alloc().directBuffer();
			
			bb.writeBytes(buffer);
			bb.writeBytes(input);
			
			in = bb;
			in.readerIndex(0);
			buffer = null;
			
			//Log.i("add : " + in.readableBytes());
		}
		
		int rSize = in.readableBytes();
		while ( rSize > 13) {
			int size = IntByteConvert.fromByte(in.getByte(10), in.getByte(11), in.getByte(12), in.getByte(13)) + 14;			
			if(rSize >= size ) {
				out.add(in.readBytes(size));				
				rSize = rSize - size;
				
				if(rSize > 0) {
					in = in.readBytes(rSize);
					
					if(rSize < 14) {
						buffer = new byte[rSize];
    	        		in.getBytes(0, buffer, 0, rSize);
    	        		//Log.i("save : " + rSize);
					}
				} else {
					in = null;
				}
			} else {
				in = in.readBytes(rSize);	
				
				buffer = new byte[rSize];
        		in.getBytes(0, buffer, 0, rSize);
				rSize = 0;
				
			}
		}
		
	}
	
	

}
