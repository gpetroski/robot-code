package com.test.robotcontroller.bluetooth.messages.outgoing;

public class RobotMoveMessage {
	// Message Type
	public final static int MOVE_MESSAGE_TYPE = 1; // 00000001
	public final static int MESSAGE_TYPE_BITS = 3; // 00000011
	
	// DIRECTION
	public final static int FORWARD = 4;  // 00000100
	public final static int REVERSE = 8;  // 00001000
	public final static int LEFT    = 12;  // 00001100
	public final static int RIGHT   = 16; // 00010000
	public final static int STOP    = 20; // 00010100	
	public final static int DIRECTION_BITS = 28; // 00011100
	
	// SPEED
	public final static int SLOW 	= 0;   // 00100000
	public final static int MEDIUM  = 64;  // 01000000
	public final static int FAST    = 96;  // 01100000
	public final static int FULL    = 128; // 10000000	
	public final static int SPEED_BITS   = 224; // 11100000
	
	private Byte message;
	
	public RobotMoveMessage(int direction, int power) {
		message = (byte) (MOVE_MESSAGE_TYPE | direction | power);
	}

	public Byte getMessage() {
		return message;
	}

	public void setMessage(Byte message) {
		this.message = message;
	}
	
	public int getSpeed() {
		return message & SPEED_BITS;
	}
	
	public int getDirection() {
		return message & DIRECTION_BITS;
	}
	
	public int getMessageType() {
		return message & MESSAGE_TYPE_BITS;
	}
	

}
