package com.test.robotcontroller.bluetooth.messages;

import com.test.robotcontroller.bluetooth.messages.incoming.RobotMessage;

public class RobotProximityMessage extends RobotMessage {
	private int value;

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
