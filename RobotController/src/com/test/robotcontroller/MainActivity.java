package com.test.robotcontroller;

import java.util.Locale;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.test.robotcontroller.bluetooth.BluetoothService;
import com.test.robotcontroller.bluetooth.messages.outgoing.RobotMoveMessage;
import com.test.robotcontroller.proximity.RobotProximityQueue;
import com.test.robotcontroller.tts.TTSLogger;

public class MainActivity extends Activity  implements TextToSpeech.OnInitListener {
	private static final String LOG_TAG = MainActivity.class.getCanonicalName();
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_CONNECT_DEVICE = 2;
	private BluetoothService bluetoothService;
	private Intent connectIntent;
	private RobotProximityQueue messageQueue;
	private AutoPilotController autoPilotController;
	private TTSLogger ttsLog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

    @Override
    public void onStart() {
        super.onStart();
		
		try {
			messageQueue = new RobotProximityQueue();
			BluetoothService.setCurrentService(new BluetoothService(messageQueue));
			bluetoothService = BluetoothService.getCurrentService();			
			autoPilotController = new AutoPilotController(messageQueue, bluetoothService);
		} catch (Exception e) { 
        	Log.e(LOG_TAG, "Bluetooth not supported");
			finish();
			return;
		}        
        
		if(!bluetoothService.isBluetoothEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		ttsLog = new TTSLogger(new TextToSpeech(this, this));
		TTSLogger.setCurrentLogger(ttsLog);
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
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.RIGHT, RobotMoveMessage.Speed.FULL));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
                }
                return true;
            }
        });	

        ImageButton upButton = (ImageButton) findViewById(R.id.upButton);
        upButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.FORWARD, RobotMoveMessage.Speed.FULL));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
                }
                return true;
            }
        });

        ImageButton leftButton = (ImageButton) findViewById(R.id.leftButton);
        leftButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.LEFT, RobotMoveMessage.Speed.FULL));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
                }
                return true;
            }
        });

        ImageButton downButton = (ImageButton) findViewById(R.id.downButton);
        downButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.REVERSE, RobotMoveMessage.Speed.FULL));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                	sendManualMove(new RobotMoveMessage(RobotMoveMessage.Direction.STOP, RobotMoveMessage.Speed.FULL));
                }
                return true;
            }
        });
        
        ToggleButton toggleAutoButton = (ToggleButton) findViewById(R.id.autoPilotToggleButton);
        toggleAutoButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isChecked) {
				if(isChecked && !autoPilotController.isRunning()) {
					TTSLogger.log("Autopilot enabled");
					new Thread(autoPilotController).start();
				} else if(autoPilotController.isRunning()){
					autoPilotController.stop();
					TTSLogger.log("Autopilot disabled");
				}
			}
        });	
        

        Button speakButton = (Button) findViewById(R.id.speakBtn);
        speakButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                	speakOut();
                } 
                return true;
            }
        });
    }
    
    public void sendManualMove(RobotMoveMessage message) {
    	if(!autoPilotController.isRunning()) {
    		TTSLogger.log("Moving in " + message.getDirection().name().toLowerCase() + " direction at " + message.getSpeed().name().toLowerCase() + " speed.");
    		bluetoothService.sendMessage(message.getMessage());
    	} else {
    		TTSLogger.log("Disable Autopilot to control manually");
    	}
    }
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
		autoPilotController.stop();
        if (bluetoothService != null) {
        	bluetoothService.disconnect();
        }

        if (ttsLog != null) {
        	ttsLog.shutdown();
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

	@Override
	public void onInit(int status) {
	       if (status == TextToSpeech.SUCCESS) {
	    	   
	            int result = ttsLog.setLanguage(Locale.US);
	            ttsLog.setPitch(.6f);
	 
	            if (result == TextToSpeech.LANG_MISSING_DATA
	                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
	                Log.e("TTS", "This Language is not supported");
	            } 
	 
	        } else {
	            Log.e("TTS", "Initilization Failed!");
	        }
		
	}
 
    private void speakOut() { 
        String text = "This is a test";
 
        ttsLog.logMessage(text);
    }

}
