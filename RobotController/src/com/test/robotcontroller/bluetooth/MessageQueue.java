package com.test.robotcontroller.bluetooth;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageQueue {
	ConcurrentLinkedQueue<Integer> incomingMessages = new ConcurrentLinkedQueue<Integer>();
	
	public void queueMessage(String message) {
		try {
			incomingMessages.add(Integer.getInteger(message));
		} catch(Exception ex) {
			
		}
	}
	
	public Integer getNextMessage() {
		return incomingMessages.poll();
	}

	public void clearQueue() {
		incomingMessages.clear();
	}
}
