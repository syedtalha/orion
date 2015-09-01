package com.syedtalha.orion.execution;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

public class CommunicationDumbSensorDataReceptionRunnable implements Runnable {
	
	byte[] receptionBuffer;
	FileInputStream mInputStream;
	private ArrayList<NewUSSensorDataListener> mNewUSSensorDataListenerList  = new ArrayList<>();// 
	
	
	/*public void setmNewUSSensorDataListener(
			NewUSSensorDataListener mNewUSSensorDataListener) {
		this.mNewUSSensorDataListenerList = mNewUSSensorDataListener;
	}*/
	public void removeNewUSSensorDataListener(NewUSSensorDataListener mNewUSSensorDataListener)	{
		this.mNewUSSensorDataListenerList.remove(mNewUSSensorDataListener);
	}

	public void addNewUSSensorDataListener(NewUSSensorDataListener mNewUSSensorDataListener)	{
		this.mNewUSSensorDataListenerList.add(mNewUSSensorDataListener);
	}
	
	public FileInputStream getmInputStream() {
		return mInputStream;
	}

	public void setmInputStream(FileInputStream mInputStream) {
		this.mInputStream = mInputStream;
	}

	

	public byte[] getBuffer() {
		return receptionBuffer;
	}

	public void setBuffer(byte[] buffer) {
		this.receptionBuffer = buffer;
	}

	
	

	public CommunicationDumbSensorDataReceptionRunnable(FileInputStream mFileInputStream)	{			
		this.mInputStream = mFileInputStream;
		this.setBuffer(new byte[4]);
	}

	@Override
	public void run() {
		int ret = 0;
		while (ret >= 0)		{
			try {
				ret = mInputStream.read(receptionBuffer);
				//Log.i("MINE",	Byte.toString(receptionBuffer[0])+" "+Byte.toString(receptionBuffer[1])+" "+Byte.toString(receptionBuffer[2])+" "+Byte.toString(receptionBuffer[3]));

				if (mNewUSSensorDataListenerList!=null)	{
					for(NewUSSensorDataListener obj : mNewUSSensorDataListenerList) {
						obj.OnNewUSSensorData(receptionBuffer);
		               // obj.notification(fireEvent);
		        }
					//this.mNewUSSensorDataListenerList.OnNewUSSensorData(receptionBuffer);
				}
			} catch (IOException e) {
			
		}
			synchronized (this) {
				this.notifyAll();
			}
		
		}
		
	}

	public  int[] getLattestPacket() {
		return new int[] {receptionBuffer[0],receptionBuffer[1],receptionBuffer[2],receptionBuffer[3]};
	}

}
