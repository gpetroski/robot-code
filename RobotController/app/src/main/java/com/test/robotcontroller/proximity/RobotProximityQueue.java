package com.test.robotcontroller.proximity;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import com.test.robotcontroller.event.incoming.ProximityEvent;

public class RobotProximityQueue {
	private static final int QUEUE_SIZE = 10;
	CircularFifoQueue<ProximityEvent> proximityReadings = new CircularFifoQueue<ProximityEvent>(QUEUE_SIZE);
	
	public void queueProximity(ProximityEvent proximity) {
		proximityReadings.add(proximity);
	}
	
	public int getAvgReading() {
		int sum = 0;
		if(proximityReadings.size() >= QUEUE_SIZE) {
			for(int i = 0; i < QUEUE_SIZE; i++) {
				sum += proximityReadings.poll().getProximity();
			}
			return sum / QUEUE_SIZE;
		} else {
			return -1;
		}
	}

	public void clearProximities() {
		proximityReadings.clear();
	}
}
