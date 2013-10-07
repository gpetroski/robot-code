package com.test.robotcontroller.bluetooth;

public class RobotControlMessage {
	private String direction;
	private int duration;

	public RobotControlMessage(String direction, int duration) {
		this.direction = direction;
		this.duration = duration;
	}
	
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
}
