package com.test.robotcontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.test.robotcontroller.bluetooth.BluetoothService;
import com.test.robotcontroller.bluetooth.MessageQueue;

public class MainActivity extends Activity {
	private static final String LOG_TAG = MainActivity.class.getCanonicalName();
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private BluetoothService bluetoothService;
	private Intent connectIntent;
	private MessageQueue messageQueue;
	private AutoPilotController autoPilotController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

    @Override
    public void onStart() {
        super.onStart();
		
		try {
			BluetoothService.setCurrentService(new BluetoothService());
			bluetoothService = BluetoothService.getCurrentService();
		} catch (Exception e) { 
        	Log.e(LOG_TAG, "Bluetooth not supported");
			finish();
			return;
		}        
        
		if(!bluetoothService.isBluetoothEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
        setupButtons();
    }
    
    public void setupButtons() {	
        connectIntent = new Intent(this, DeviceListActivity.class);
        Button connectButton = (Button) findViewById(R.id.connectButton);
        connectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE);
            }
        });	

    	
        ImageButton rightButton = (ImageButton) findViewById(R.id.rightButton);
        rightButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    bluetoothService.sendMessage("r");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bluetoothService.sendMessage("s");
                }
                return true;
            }
        });	

        ImageButton upButton = (ImageButton) findViewById(R.id.upButton);
        upButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    bluetoothService.sendMessage("f");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bluetoothService.sendMessage("s");
                }
                return true;
            }
        });

        ImageButton leftButton = (ImageButton) findViewById(R.id.leftButton);
        leftButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    bluetoothService.sendMessage("l");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bluetoothService.sendMessage("s");
                }
                return true;
            }
        });

        ImageButton downButton = (ImageButton) findViewById(R.id.downButton);
        downButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    bluetoothService.sendMessage("b");
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    bluetoothService.sendMessage("s");
                }
                return true;
            }
        });
        
        ToggleButton toggleAutoButton = (ToggleButton) findViewById(R.id.autoPilotToggleButton);
        toggleAutoButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				//bluetoothService.
			}
        });	
    }
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (bluetoothService != null) {
        	bluetoothService.disconnect();
        }
    }
    
    
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        
        case REQUEST_ENABLE_BT:
            if (resultCode == Activity.RESULT_OK) {
            	Log.i(LOG_TAG, "Successfully enabled bluetooth");
            } else {
            	Log.i(LOG_TAG, "Could not enable bluetooth");
            	finish();
            }
            break;
            
        case REQUEST_CONNECT_DEVICE:
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            	bluetoothService.connect(address);
            } else {
            	Log.i(LOG_TAG, "Could not connect");
            }
            break;
        	
        }
    }

}
