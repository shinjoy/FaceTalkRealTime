package com.nomadsoft.chat.db.dto;

import com.google.android.gcm.server.Sender;
import com.notnoop.apns.ApnsService;

public class PushService {
	String service_id = "";
	String gcm_pkg_id = "";
	String gcm_api_key = "";
	int apns_production = 0;
	String apns_path = "";
	String apns_pass = "";
	ApnsService apnsService = null;
	Sender gcmSender = null;	
	public String getService_id() {
		return service_id;
	}
	public void setService_id(String service_id) {
		this.service_id = service_id;
	}
	public String getGcm_pkg_id() {
		return gcm_pkg_id;
	}
	public void setGcm_pkg_id(String gcm_pkg_id) {
		this.gcm_pkg_id = gcm_pkg_id;
	}
	public String getGcm_api_key() {
		return gcm_api_key;
	}
	public void setGcm_api_key(String gcm_api_key) {
		this.gcm_api_key = gcm_api_key;
	}
	public int getApns_production() {
		return apns_production;
	}
	public void setApns_production(int apns_production) {
		this.apns_production = apns_production;
	}
	public String getApns_path() {
		return apns_path;
	}
	public void setApns_path(String apns_path) {
		this.apns_path = apns_path;
	}
	public String getApns_pass() {
		return apns_pass;
	}
	public void setApns_pass(String apns_pass) {
		this.apns_pass = apns_pass;
	}
	
	public ApnsService getApnsService() {
		return apnsService;
	}
	public void setApnsService(ApnsService apnsService) {
		this.apnsService = apnsService;
	}
	public Sender getGcmSender() {
		return gcmSender;
	}
	public void setGcmSender(Sender gcmSender) {
		this.gcmSender = gcmSender;
	}
}
