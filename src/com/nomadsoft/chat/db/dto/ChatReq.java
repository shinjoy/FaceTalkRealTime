package com.nomadsoft.chat.db.dto;

public class ChatReq {
	int cqId = 0;
	String roomIDX = "";
	String reqTYPE = "";
	String memberID = "";
	String memberName = "";
	String regDATE = "";
	String param1 = "";
	String param2 = "";
	
	//추가 	
	String msgKey = "";  //신고로 입력한 글의 ID 소명 댓글은 이순서대로 기록된다.
	String msgDate = "";
	String contents = "";

	String thumb1 = "";
	String thumb2 = "";
	String thumb3 = "";
	
	String fileName1 = "";
	String fileName2 = "";
	String fileName3 = "";
	String targetid = "";
	
	public String getTargetid() {
		return targetid;
	}
	public void setTargetid(String targetid) {
		this.targetid = targetid;
	}
	
	
	public String getMsgKey() {
		return msgKey;
	}
	public void setMsgKey(String msgKey) {
		this.msgKey = msgKey;
	}
	public String getMsgDate() {
		return msgDate;
	}
	public void setMsgDate(String msgDate) {
		this.msgDate = msgDate;
	}
	
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getThumb1() {
		return thumb1;
	}
	public void setThumb1(String thumb1) {
		this.thumb1 = thumb1;
	}
	public String getThumb2() {
		return thumb2;
	}
	public void setThumb2(String thumb2) {
		this.thumb2 = thumb2;
	}
	public String getThumb3() {
		return thumb3;
	}
	public void setThumb3(String thumb3) {
		this.thumb3 = thumb3;
	}
	public String getFileName1() {
		return fileName1;
	}
	public void setFileName1(String fileName1) {
		this.fileName1 = fileName1;
	}
	public String getFileName2() {
		return fileName2;
	}
	public void setFileName2(String fileName2) {
		this.fileName2 = fileName2;
	}
	public String getFileName3() {
		return fileName3;
	}
	public void setFileName3(String fileName3) {
		this.fileName3 = fileName3;
	}
	public int getCqId() {
		return cqId;
	}
	public void setCqId(int cqId) {
		this.cqId = cqId;
	}
	public String getRoomIDX() {
		return roomIDX;
	}
	public void setRoomIDX(String roomIDX) {
		this.roomIDX = roomIDX;
	}
	public String getReqTYPE() {
		return reqTYPE;
	}
	public void setReqTYPE(String reqTYPE) {
		this.reqTYPE = reqTYPE;
	}
	public String getMemberID() {
		return memberID;
	}
	public void setMemberID(String memberID) {
		this.memberID = memberID;
	}
	public String getRegDATE() {
		return regDATE;
	}
	public void setRegDATE(String regDATE) {
		this.regDATE = regDATE;
	}
	public String getParam1() {
		return param1;
	}
	public void setParam1(String param1) {
		this.param1 = param1;
	}
	public String getParam2() {
		return param2;
	}
	public void setParam2(String param2) {
		this.param2 = param2;
	}
	public String getMemberName() {
		return memberName;
	}
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
}
