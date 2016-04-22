package com.nomadsoft.chat.db.dto;

public class Packet {
	int pt = 0;
	int st = 0;
	
	String param = "";
	long sendTick = 0;
	long bufferKey = 0;
	

	String destID = "";
	String serverID = "";
	
	
	public String getServerID() {
		return serverID;
	}

	public void setServerID(String serverID) {
		this.serverID = serverID;
	}

	public long getSendTick() {
		return sendTick;
	}

	public void setSendTick(long sendTick) {
		this.sendTick = sendTick;
	}



	
	public long getBufferKey() {
		return bufferKey;
	}

	public void setBufferKey(long bufferKey) {
		this.bufferKey = bufferKey;
	}

	public String getDestID() {
		return destID;
	}

	public void setDestID(String destID) {
		this.destID = destID;
	}

	public int getPt() {
		return pt;
	}

	public void setPt(int pt) {
		this.pt = pt;
	}

	public int getSt() {
		return st;
	}

	public void setSt(int st) {
		this.st = st;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

}
