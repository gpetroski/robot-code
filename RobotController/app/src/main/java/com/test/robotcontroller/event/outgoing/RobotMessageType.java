package com.test.robotcontroller.event.outgoing;

public enum RobotMessageType {
	MOVE_MESSAGE_TYPE(1), // 00000001
	GET_PING_MESSAGE_TYPE(2), // 00000010
	MESSAGE_TYPE_BITS(3); // 00000011
	
	private int value;
	
	RobotMessageType(int value) {
		this.value = value;
	}
	
	public int getIntegerValue() {
		return this.value;
	}

}
