package com.test.robotcontroller.bluetooth.messages.incoming;

import com.google.gson.Gson;

public class RobotMessage {
	private MessageType type;

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}
	
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public enum MessageType {
		MOVE,
		PROXIMITY,
		LOG
	}
}
