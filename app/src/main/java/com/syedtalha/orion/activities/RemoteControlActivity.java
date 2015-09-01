package com.syedtalha.orion.activities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.syedtalha.orion.R;
import com.syedtalha.orion.networking.RemoteControllerRunnable;
import com.syedtalha.orion.ui.joystick.JoystickMovedListener;
import com.syedtalha.orion.ui.joystick.JoystickView;

public class RemoteControlActivity extends Activity {
	
	public static final String KEY_EXTRA_IP_ADDRESS = "key_ip_address";
	RemoteControllerRunnable remoteControllerRunnable;
	Thread remoteControlCommandsSenderThread;
	TextView txtX, txtY;
	JoystickView joystick;
	String ipAddress;
	RemoteControlActivity thisAct;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control);
		
		ipAddress = getIntent().getStringExtra(KEY_EXTRA_IP_ADDRESS);
		
		
		thisAct = this;
		remoteControllerRunnable = new  RemoteControllerRunnable();
		NetworkTestForRemoteControlTask netTestTaks = new NetworkTestForRemoteControlTask();
		netTestTaks.execute(ipAddress);
		
		}

	
	private JoystickMovedListener _listener = new JoystickMovedListener() {

		@Override
		public void OnMoved(int pan, int tilt) {
			txtX.setText(Integer.toString(pan));
			txtY.setText(Integer.toString(tilt));
			remoteControllerRunnable.setxSpeed(pan);
			remoteControllerRunnable.setySpeed(tilt);
		}

		@Override
		public void OnReleased() {
			txtX.setText("released");
			txtY.setText("released");
	
		}

		public void OnReturnedToCenter() {
			txtX.setText("stopped");
			txtY.setText("stopped");
			
		};
	};

	private class NetworkTestForRemoteControlTask extends AsyncTask<String, String, Boolean>		{

		@Override
		protected Boolean doInBackground(String... params) {
			
			try {
				remoteControllerRunnable.setServersAddress(InetAddress.getByName(ipAddress));
			} catch (UnknownHostException e) {
				e.printStackTrace();
				Log.i("MINE", "Orion Server UnknownHostExcep");
				//Toast.makeText(RemoteControlActivity.this, "Orion Server not found", Toast.LENGTH_SHORT).show();
				return false;
			}

			try {
				if (remoteControllerRunnable.getServersAddress().isReachable(4000))	{
					return true;
				}	else{return false;}
				
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("MINE", "Orion Server not rechable");
				return false;
				// TODO Auto-generated catch block
			}
			

		}
		@Override
		protected void onPostExecute(Boolean result) {
			if (result)	{
				Log.i("MINE", "Orion Server FOUND");
				setContentView(R.layout.joystick);
				txtX = (TextView) findViewById(R.id.TextViewX);
				txtY = (TextView) findViewById(R.id.TextViewY);
				joystick = (JoystickView) findViewById(R.id.joystickView);
				// joystick.setMoveResolution(50);
				joystick.setMovementRange(255);
				// joystick.setAutoReturnToCenter(false);
				joystick.setOnJostickMovedListener(_listener);
				remoteControlCommandsSenderThread= new Thread(remoteControllerRunnable);
				remoteControlCommandsSenderThread.start();
			}	else	{
				Log.i("MINE", "Orion Server not rechable..SOMETHING WRONG");
				Toast.makeText(thisAct, "Orion Server not rechable. Make sure Orion is on same network", Toast.LENGTH_SHORT).show();

				thisAct.finish();
			}
	     }

	}
	/*public void testConnection(View v)	{
		try {
			remoteControllerRunnable.setServersAddress(InetAddress.getByName(addressEditText.getText().toString()));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
