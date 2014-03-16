package com.test.robotcontroller.bluetooth.messages.outgoing;

public class RobotMoveMessage {
	
	// DIRECTION
	public enum Direction {
		FORWARD(4), // 00000100
		REVERSE(8), // 00001000
		LEFT(12),  // 00001100
		RIGHT(16), // 00010000
		STOP(20), // 00010100	
		DIRECTION_BITS(28); // 00011100
		
		private int value;
		
		Direction(int value) {
			this.value = value;
		}
		
		public int getIntegerValue() {
			return this.value;
		}		
		
		public Direction valueOf(int value) {
			for(Direction dir : values()) {
				if(dir.getIntegerValue() == value) {
					return dir;
				}
			}
			return null;
		}
	}
	
	// SPEED
	public enum Speed {
		SLOW(0), // 00100000
		MEDIUM(64), // 01000000
		FAST(96),  // 01100000
		FULL(128), // 10000000
		SPEED_BITS(224); // 11100000	
		
		private int value;
		
		Speed(int value) {
			this.value = value;
		}
		
		public int getIntegerValue() {
			return this.value;
		}		
		
		public Speed valueOf(int value) {
			for(Speed speed : values()) {
				if(speed.getIntegerValue() == value) {
					return speed;
				}
			}
			return null;
		}	
	}
	
	private Direction direction;
	private Speed speed;
	private RobotMessageType messageType;
	
	public RobotMoveMessage(Direction direction, Speed speed) {
		this.speed = speed;
		this.direction = direction;
		this.messageType = RobotMessageType.MOVE_MESSAGE_TYPE;
	}

	public Byte getMessage() {
		return (byte) (messageType.getIntegerValue() | direction.getIntegerValue() | speed.getIntegerValue());
	}

	public Speed getSpeed() {
		return speed;
	}
	
	public Direction getDirection() {
		return direction;
	}
	
	public RobotMessageType getMessageType() {
		return messageType;
	}
	

}
