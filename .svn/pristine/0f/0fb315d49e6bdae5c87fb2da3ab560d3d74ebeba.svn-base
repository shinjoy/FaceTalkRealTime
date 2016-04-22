package com.nomadsoft.chat.openServer;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class JsonDecoder {
	
	public static JsonElement decodeJson(String data) {
		JsonElement result = null;
		
		JsonParser parser = new JsonParser();
		result = (JsonObject)parser.parse(data);
		
		
		return result;
	}
}
