package com.nomadsoft.chat.openServer;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.util.TimerTask;

import com.nomadsoft.chat.db.dao.ServerDao;
import com.nomadsoft.chat.log.Log;
import com.nomadsoft.chat.noticeService.ChatReqThread;
import com.nomadsoft.chat.noticeService.NoticeSendThread;
import com.nomadsoft.util.push.NoticePushSendThread;
import com.nomadsoft.util.push.PushSendThread;

public class ChatTimer extends TimerTask{
	
	GlobalTrafficShapingHandler gtsh = null;
	public ChatTimer(GlobalTrafficShapingHandler gtsh) {
		this.gtsh = gtsh;
	}
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			String log = "Timer Channer Size : " + ChatUserContext.getChannels().size()
					+ " Context Size : " + ChatUserContext.getContextmap().size()
					+ " UserContext Size : " + ChatUserContext.getUserContextMap().size()
					+ " RemoteContext Size : " + ChatUserContext.getRemoteContextMap().size()
					+ " read Size : " + gtsh.trafficCounter().cumulativeReadBytes()
					+ " write Size : " + gtsh.trafficCounter().cumulativeWrittenBytes();			
		
			
			//업데이트.
			ServerDao.updateServerInfo(ChatUserContext.getContextmap().size()
					, ChatUserContext.getUserContextMap().size()
					, ChatUserContext.getRemoteContextMap().size()
					, gtsh.trafficCounter().cumulativeReadBytes()
					, gtsh.trafficCounter().cumulativeWrittenBytes());
		
			
			gtsh.trafficCounter().resetCumulativeTime();
		
			
			if(PushSendThread.isRunning() == false) {
				PushSendThread.startThread();
			}
			
			if(NoticePushSendThread.isRunning() == false) {
				NoticePushSendThread.startThread();
			}
			
			if(NoticeSendThread.isRunning() == false) {
				NoticeSendThread.startThread();	
			}
			
			if(ChatReqThread.isRunning() == false) {
				ChatReqThread.startThread();	
			}
			
			Log.i(log);			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
