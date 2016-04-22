package com.nomadsoft.chat.openServer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.nomadsoft.chat.db.dto.VirtualMember;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatUserContext {
	
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static final ConcurrentHashMap<Channel, ChatUserContext> contextMap = new ConcurrentHashMap<Channel, ChatUserContext>();
    static final ConcurrentHashMap<String, ChatUserContext> userContextMap = new ConcurrentHashMap<String, ChatUserContext>();
    static final ConcurrentHashMap<String, ChatUserContext> pcUserContextMap = new ConcurrentHashMap<String, ChatUserContext>();
    static final ConcurrentHashMap<String, ChatUserContext> remoteContextMap = new ConcurrentHashMap<String, ChatUserContext>();
    static final ConcurrentHashMap<String, ChatUserContext> vcUserContextMap = new ConcurrentHashMap<String, ChatUserContext>();
    
    
	public static ConcurrentHashMap<String, ChatUserContext> getVcusercontextmap() {
		return vcUserContextMap;
	}

	public static ChannelGroup getChannels() {
		return channels;
	}

	public static ConcurrentHashMap<Channel, ChatUserContext> getContextmap() {
		return contextMap;
	}
    
    public static ConcurrentHashMap<String, ChatUserContext> getUserContextMap() {
		return userContextMap;
	}
	
    public static ConcurrentHashMap<String, ChatUserContext> getPcUserContextMap() {
		return pcUserContextMap;
	}
    
    public static ConcurrentHashMap<String, ChatUserContext> getRemoteContextMap() {
		return remoteContextMap;
	}

    
	int status = 0;
	Channel channel = null;
	String id = "";
	String authCode = "";
	String name = "";
	int type = 0;
	String phone = "";
	String groupID = "";
	String mode = "";
	String deviceId = "";
	List<ChatUserContext> vmList = null;
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}

	public ChatUserContext(Channel channel) {
		this.channel = channel;
	}
	
	public int getStatus() {
		return status;
	}
	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public List<ChatUserContext> getVmList() {
		return vmList;
	}

	public void setVmList(List<ChatUserContext> vmList) {
		this.vmList = vmList;
	}

}
