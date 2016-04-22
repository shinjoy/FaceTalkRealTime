package com.nomadsoft.chat.db.dto;


public class BoardImageFile {
	String filename_normal = "";  
	String localfilename_normal = "";
	
	String filename_server = "";
	String thumbFilename_server = "";
	String fileType = "";
	
	public String getThumbFilename_server() {
		return thumbFilename_server;
	}
	public void setThumbFilename_server(String thumbFilename_server) {
		this.thumbFilename_server = thumbFilename_server;
	}

	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	String upfileName = "";
	String upfileThumbName = "";
	
	String upfileNamePullPath = "";
	String upfileThumbNamePullPath = "";
	
	
	public String getUpfileNamePullPath() {
		return upfileNamePullPath;
	}
	public void setUpfileNamePullPath(String upfileNamePullPath) {
		this.upfileNamePullPath = upfileNamePullPath;
	}
	public String getUpfileThumbNamePullPath() {
		return upfileThumbNamePullPath;
	}
	public void setUpfileThumbNamePullPath(String upfileThumbNamePullPath) {
		this.upfileThumbNamePullPath = upfileThumbNamePullPath;
	}
	public String getFilename_server() {
		return filename_server;
	}
	public String getUpfileName() {
		return upfileName;
	}
	public void setUpfileName(String upfileName) {
		this.upfileName = upfileName;
	}
	public String getUpfileThumbName() {
		return upfileThumbName;
	}
	public void setUpfileThumbName(String upfileThumbName) {
		this.upfileThumbName = upfileThumbName;
	}
	public void setFilename_server(String filename_server) {
		this.filename_server = filename_server;
	}

	boolean needUpdate = false;	
	
	public String getFilename_normal() {
		return filename_normal;
	}
	public void setFilename_normal(String filename_normal) {
		this.filename_normal = filename_normal;
	}
	public String getLocalfilename_normal() {
		return localfilename_normal;
	}
	public void setLocalfilename_normal(String localfilename_normal) {
		this.localfilename_normal = localfilename_normal;
	}
	public boolean isNeedUpdate() {
		return needUpdate;
	}
	public void setNeedUpdate(boolean needUpdate) {
		this.needUpdate = needUpdate;
	}
	
}
