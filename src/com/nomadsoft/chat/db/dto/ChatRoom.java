package com.nomadsoft.chat.db.dto;

public class ChatRoom {
	long chatRoomSeq = 0;
	int chatRoomType = 0;
	String ownerId = "";
	String regDate = "";
	String lastMsgSeq = "";
	String title = "";
	int group_type = 0;
	int group_seq = 0;
	
	
	public long getChatRoomSeq() {
		return chatRoomSeq;
	}
	public void setChatRoomSeq(long chatRoomSeq) {
		this.chatRoomSeq = chatRoomSeq;
	}
	public int getChatRoomType() {
		return chatRoomType;
	}
	public void setChatRoomType(int chatRoomType) {
		this.chatRoomType = chatRoomType;
	}
	public String getOwnerId() {
		return ownerId;
	}
	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	public String getRegDate() {
		return regDate;
	}
	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}
	public String getLastMsgSeq() {
		return lastMsgSeq;
	}
	public void setLastMsgSeq(String lastMsgSeq) {
		this.lastMsgSeq = lastMsgSeq;
	}
	public int getGroup_type() {
		return group_type;
	}
	public void setGroup_type(int group_type) {
		this.group_type = group_type;
	}
	public int getGroup_seq() {
		return group_seq;
	}
	public void setGroup_seq(int group_seq) {
		this.group_seq = group_seq;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
