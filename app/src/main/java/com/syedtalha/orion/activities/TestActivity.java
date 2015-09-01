package com.syedtalha.orion.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.syedtalha.orion.R;

public class TestActivity extends Activity {
	EditText ipEdittext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		ipEdittext = (EditText)findViewById(R.id.editTextIPAddr);
		Button b = (Button) findViewById(R.id.button2);
		b.setEnabled(false);
	}
	
	public void connectToServer(View v)		{
		Intent i = new Intent(this, RemoteControlActivity.class);
		i.putExtra(RemoteControlActivity.KEY_EXTRA_IP_ADDRESS, ipEdittext.getText().toString());
		startActivity(i);
	}
	public void startListening(View v)		{
		//Intent i = new Intent(this, ServerActivity.class);
		//startActivity(i);
	}
	
	
	
	
}
