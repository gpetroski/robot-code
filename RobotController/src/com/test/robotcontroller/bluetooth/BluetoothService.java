package com.test.robotcontroller.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothService {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String LOG_TAG = BluetoothService.class.getCanonicalName();
    
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device
    
	private static BluetoothService bluetooth;
	private BluetoothAdapter bluetoothAdapter;		
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
	private int state = 0;	
   
	public BluetoothService() throws Exception {
        // Get local Bluetooth adapter
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            throw new Exception("Bluetooth not supported.");
        }
	}
	
	public boolean isBluetoothEnabled() {
		return bluetoothAdapter.isEnabled();
	}
	
	public void sendMessage(String message) {
		Log.d(LOG_TAG, "Sending message " + message);
		if(connectedThread != null && state == STATE_CONNECTED) {
			connectedThread.write(message);
		}
	}
	
	public Set<BluetoothDevice> getPairedDevices() {
		return bluetoothAdapter.getBondedDevices();
	}
	
	public void connect(String address) {
    	Log.i(LOG_TAG, "Connecting to address: " + address);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if(device == null) {
        	Log.e(LOG_TAG, "Unable to find device with address: " + address);
        	return;
        }

        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {connectThread.cancel(); connectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
	}
	
	public void disconnect() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        setState(STATE_NONE);
	}

	public static void setCurrentService(BluetoothService bluetoothService) {
		bluetooth = bluetoothService;
	}

	public static BluetoothService getCurrentService() {
		return bluetooth;
	}
	
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
    	Log.i(LOG_TAG, "connected");

        // Cancel the thread that completed the connection
        if (connectThread != null) {connectThread.cancel(); connectThread = null;}

        // Cancel any thread currently running a connection
        if (connectedThread != null) {connectedThread.cancel(); connectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

        setState(STATE_CONNECTED);
    }
    
    private synchronized void setState(int state) {
        Log.i(LOG_TAG, "setState() " + this.state + " -> " + state);
        this.state = state;
    }
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    public void run() {
	        // Cancel discovery because it will slow down the connection
	        bluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                connectThread = null;
            }
	 
	        // Do work to manage the connection (in a separate thread)
            // Start the connected thread
            connected(mmSocket, mmDevice);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	    }
	 
	    public void run() {
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
	            try {
	                // Read from the InputStream
	                bytes = mmInStream.read(buffer);
	                // Send the obtained bytes to the UI activity
	                Log.i(LOG_TAG, new String(buffer, "UTF8"));
	            } catch (IOException e) {
	                break;
	            }
	        }
	    }
	 
	    /* Call this from the main activity to send data to the remote device */
	    public void write(String message) {
	        try {
	            mmOutStream.write(message.getBytes());
	        } catch (IOException e) { }
	    }
	 
	    /* Call this from the main activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
}


