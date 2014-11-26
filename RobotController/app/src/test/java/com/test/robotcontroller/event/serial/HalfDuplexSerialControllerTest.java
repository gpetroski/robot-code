package com.test.robotcontroller.event.serial;

import com.test.robotcontroller.event.serial.HalfDuplexSerialController;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@RunWith(RobolectricTestRunner.class)
public class HalfDuplexSerialControllerTest {

    @Test
    public void testHalfDuplex() {
        HalfDuplexSerialController serialController = new HalfDuplexSerialController(null, null, null);

        serialController.start();
    }

    public class InputOutputStream extends InputStream, OutputStream {

        @Override
        public int read() throws IOException {
            return 0;
        }

        @Override
        public void write(int i) throws IOException {

        }
    }
}