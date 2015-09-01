package com.syedtalha.orion.activities;

import android.app.Activity;
import android.os.Bundle;

import com.syedtalha.orion.R;
import com.syedtalha.orion.application.OrionApplication;
import com.syedtalha.orion.execution.BotControlRunnable;

public class ServerActivity extends Activity {
	OrionApplication app;
	BotControlRunnable  botControlRunnable;
	Thread botControlThread;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server);
		app = (OrionApplication)getApplication();
		botControlRunnable = new BotControlRunnable(app.getGlobalFileInputStream(),app.getGlobalFileOutputStream(),this,BotControlRunnable.BOT_CONTROL_RUNNABLE_REMOTE_SERVER_MANUAL_OVERRIDE_CONTEXT);
		botControlThread = new Thread(botControlRunnable);
		botControlThread.setPriority(Thread.MAX_PRIORITY);
		botControlThread.start();
		
		
		
	}

	
}
