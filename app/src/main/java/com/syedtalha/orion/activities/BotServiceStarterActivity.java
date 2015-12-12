package com.syedtalha.orion.activities;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.syedtalha.orion.R;
import com.syedtalha.orion.application.OrionApplication;
import com.syedtalha.orion.execution.BotControlRunnable;
import com.syedtalha.orion.models.VehicleSystem;
import com.syedtalha.orion.services.OrionBackgroundService;

public class BotServiceStarterActivity extends Activity implements OnCheckedChangeListener {
	private PendingIntent mPermissionIntent;
	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
	private boolean mPermissionRequestPending;
	private UsbManager mUsbManager;
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;

	ToggleButton serviceToggler;
	Intent serviceIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bot_service_starter);
		
		serviceToggler = (ToggleButton) findViewById(R.id.toggleButtonOrionService);
		serviceToggler.setOnCheckedChangeListener(this);
		serviceIntent = new Intent(this, OrionBackgroundService.class);
		
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		// mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mInputStream != null && mOutputStream != null) {
			return;
		}

		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d("MINE", "mAccessory is null");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		//closeAccessory();
	}

	/**
	 * Called when the activity is no longer needed prior to being removed from
	 * the activity stack.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mUsbReceiver);
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = intent
							.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);// UsbManager.getAccessory(serviceIntent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d("MINE", "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = intent
						.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);// UsbManager.getAccessory(serviceIntent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};

	private void openAccessory(UsbAccessory accessory) {
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			
			OrionApplication app = (OrionApplication)getApplication();
		     
		    // Call a custom application method
			app.setGlobalFileInputStream(mInputStream);
			app.setGlobalFileOutputStream(mOutputStream);
			
			//BotControlRunnable botControlRunnable = new BotControlRunnable(mInputStream,mOutputStream,this);
			//Thread thread = new Thread(null, botControlRunnable, "MINE");
			//thread.setPriority(Thread.MAX_PRIORITY);
			//thread.start();
			Log.d("MINE", "accessory opened");
		} else {
			Log.d("MINE", "accessory open fail");
		}
	}

	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;

			mAccessory = null;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked)	{
			startService(serviceIntent);
		}	else	{
			stopService(serviceIntent);
		}
		
	}

	public void startManualOverrideActivity(View view)	{
		Intent i = new Intent(this, NativeManualOverrideActivity.class);
		startActivity(i);
	}
	public void startServerActivity(View view)		{
		Intent i = new Intent(this, ServerActivity.class);
		startActivity(i);
	}
	
}
