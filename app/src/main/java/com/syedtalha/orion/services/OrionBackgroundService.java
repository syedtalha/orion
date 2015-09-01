package com.syedtalha.orion.services;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.syedtalha.orion.application.OrionApplication;
import com.syedtalha.orion.execution.BotControlRunnable;

public class OrionBackgroundService extends Service {
	NotificationManager notificationManager;
	Notification.Builder notificationBuilder;
	 OrionApplication app;
	 BotControlRunnable botControlRunnable;
	 Thread botControlThread;
	 Notification ongoingNotification;
	private int  ONGOING_NOTIFICATION_ID=3;
	// Binder given to clients
	// private final IBinder mBinder = new LocalBinder();
	 @Override
	    public void onCreate() {
	       
	        app = (OrionApplication)getApplication();
			botControlRunnable = new BotControlRunnable(app.getGlobalFileInputStream(),app.getGlobalFileOutputStream(),this,BotControlRunnable.BOT_CONTROL_RUNNABLE_AUTONOMOUS_CONTEXT);
			botControlThread = new Thread(null, botControlRunnable, "MINE");
			botControlThread.setPriority(Thread.MAX_PRIORITY);
			//ongoingNotification = Notification.Builder
	        // Display a notification about us starting.  We put an icon in the status bar.
	        prepareNotification();
	    }
	
	

	@Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
		
		//startForeground(ONGOING_NOTIFICATION_ID, notification);
		if(!botControlThread.isAlive())	{
			botControlThread.start();
		}
	        // We want this service to continue running until it is explicitly
	        // stopped, so return sticky.
	        return START_NOT_STICKY;
	    }
	 
	
    

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    /*public class LocalBinder extends Binder {
        OrionBackgroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return OrionBackgroundService.this;
        }
    }*/

    @Override
    public IBinder onBind(Intent intent) {
      //  return mBinder;
    	return null;
    }

    
    private void prepareNotification() {
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationBuilder = new Builder(this);
        notificationBuilder.setContentTitle("Bot Controller Running..");
        notificationBuilder.setOngoing(true);
        notificationBuilder.setNumber(ONGOING_NOTIFICATION_ID);
		
	}
}
