package com.test.robotcontroller.bluetooth.messages;

public class RobotMoveMessage extends RobotMessage {
	private String direction;
	private float power;

	public RobotMoveMessage() {
		this.setType(MessageType.MOVE);
	}

	public RobotMoveMessage(String direction, float power) {
		this.setType(MessageType.MOVE);
		this.direction = direction;
		this.power = power;
	}
	
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public float getPower() {
		return power;
	}
	public void setPower(int power) {
		this.power = power;
	}
}
