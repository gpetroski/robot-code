package com.test.robotcontroller.event.serial;

import com.test.robotcontroller.event.incoming.ReadEvent;
import com.test.robotcontroller.event.outgoing.WriteEvent;
import com.test.robotcontroller.event.serial.HalfDuplexSerialController;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.greenrobot.event.EventBus;

@RunWith(RobolectricTestRunner.class)
public class HalfDuplexSerialControllerTest {
    @Before
    public void setUp() {
        ShadowLog.stream = System.out;
    }

    @Test
    public void testHalfDuplex() {
        Queue<Integer> outgoingBytes = new LinkedList<Integer>();
        Queue<Integer> incomingBytes = new LinkedList<Integer>();

        EventBus eventBus = Mockito.mock(EventBus.class);

        HalfDuplexSerialController outboundSerial = new HalfDuplexSerialController(eventBus, new MockInputStream(incomingBytes), new MockOutputStream(outgoingBytes));
        HalfDuplexSerialController inboundSerial = new HalfDuplexSerialController(eventBus, new MockInputStream(outgoingBytes), new MockOutputStream(incomingBytes));

        outboundSerial.start();
        inboundSerial.start();

        Integer message = 123;
        WriteEvent event = new WriteEvent();
        event.setMessage(message.byteValue());
        outboundSerial.onEvent(event);
        inboundSerial.onEvent(event);
        outboundSerial.onEvent(event);
        outboundSerial.onEvent(event);
        inboundSerial.onEvent(event);

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArgumentCaptor<ReadEvent> captor = ArgumentCaptor.forClass(ReadEvent.class);
        Mockito.verify(eventBus, Mockito.times(5)).post(captor.capture());
        System.out.println(captor.getAllValues().get(0));
        System.out.println(captor.getAllValues().get(1));
        System.out.println(captor.getAllValues().get(2));
        System.out.println(captor.getAllValues().get(3));
        System.out.println(captor.getAllValues().get(4));
    }

    public class MockOutputStream extends OutputStream {
        Queue<Integer> writtenBytes;

        public MockOutputStream(Queue<Integer> writtenBytes) {
            this.writtenBytes = writtenBytes;
        }

        @Override
        public void write(int i) throws IOException {
            this.writtenBytes.add(i);
        }
    }

    public class MockInputStream extends InputStream {
        Queue<Integer> writtenBytes;

        public MockInputStream(Queue<Integer> writtenBytes) {
            this.writtenBytes = writtenBytes;
        }

        @Override
        public int read() throws IOException {
            while(writtenBytes.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return writtenBytes.poll();
        }
    }
}