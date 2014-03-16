package com.test.robotcontroller.bluetooth.messages.outgoing;

public enum RobotMessageType {
	MOVE_MESSAGE_TYPE(1), // 00000001
	MESSAGE_TYPE_BITS(3); // 00000011
	
	private int value;
	
	RobotMessageType(int value) {
		this.value = value;
	}
	
	public int getIntegerValue() {
		return this.value;
	}

}
