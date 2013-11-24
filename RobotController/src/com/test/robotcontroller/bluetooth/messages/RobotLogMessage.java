package com.test.robotcontroller.bluetooth.messages;


public class RobotLogMessage extends RobotMessage {
	private String message;
	
	public RobotLogMessage() {
		this.setType(MessageType.LOG);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
