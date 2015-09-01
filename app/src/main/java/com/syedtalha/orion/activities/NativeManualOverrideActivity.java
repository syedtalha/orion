package com.syedtalha.orion.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.syedtalha.orion.R;
import com.syedtalha.orion.R.id;
import com.syedtalha.orion.R.layout;
import com.syedtalha.orion.application.OrionApplication;
import com.syedtalha.orion.execution.BotControlRunnable;
import com.syedtalha.orion.ui.joystick.JoystickMovedListener;
import com.syedtalha.orion.ui.joystick.JoystickView;

public class NativeManualOverrideActivity extends ActionBarActivity {
	TextView txtX, txtY;
	JoystickView joystick;

	OrionApplication app;
	BotControlRunnable botControlRunnable;
	Thread botControlThread;

	// private JoystickMovedListener _listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_native_manual_override);
		setContentView(R.layout.joystick);

		app = (OrionApplication)getApplication();
		botControlRunnable = new BotControlRunnable(app.getGlobalFileInputStream(),app.getGlobalFileOutputStream(),this,BotControlRunnable.BOT_CONTROL_RUNNABLE_NATIVE_MANUAL_OVERRIDE_CONTEXT);
		botControlThread = new Thread(null, botControlRunnable, "MINE");
		botControlThread.setPriority(Thread.MAX_PRIORITY);
		botControlThread.start();
		
		txtX = (TextView) findViewById(R.id.TextViewX);
		txtY = (TextView) findViewById(R.id.TextViewY);
		joystick = (JoystickView) findViewById(R.id.joystickView);
		// joystick.setMoveResolution(50);
		joystick.setMovementRange(255);
		// joystick.setAutoReturnToCenter(false);
		joystick.setOnJostickMovedListener(_listener);
	}

	private JoystickMovedListener _listener = new JoystickMovedListener() {

		@Override
		public void OnMoved(int pan, int tilt) {
			txtX.setText(Integer.toString(pan));
			txtY.setText(Integer.toString(tilt));
			botControlRunnable.setxSpeed(pan);
			botControlRunnable.setySpeed(tilt);
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

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.native_manual_override, menu); return
	 * true; }
	 * 
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { // Handle
	 * action bar item clicks here. The action bar will // automatically handle
	 * clicks on the Home/Up button, so long // as you specify a parent activity
	 * in AndroidManifest.xml. int id = item.getItemId(); if (id ==
	 * R.id.action_settings) { return true; } return
	 * super.onOptionsItemSelected(item); }
	 */
}
