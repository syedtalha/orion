package com.syedtalha.orion.execution;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CommunicationSendingRunnable implements Runnable {
	FileInputStream mInputStream;
	public FileInputStream getmInputStream() {
		return mInputStream;
	}

	public void setmInputStream(FileInputStream mInputStream) {
		this.mInputStream = mInputStream;
	}

	public FileOutputStream getmOutputStream() {
		return mOutputStream;
	}

	public void setmOutputStream(FileOutputStream mOutputStream) {
		this.mOutputStream = mOutputStream;
	}

	public byte[] getBuffer() {
		return sendingBuffer;
	}

	public void setBuffer(byte[] buffer) {
		this.sendingBuffer = buffer;
	}

	FileOutputStream mOutputStream;
	byte[] sendingBuffer;
	

	public CommunicationSendingRunnable(FileInputStream mInputStream,
			FileOutputStream mOutputStream) {
		this.setmInputStream(mInputStream);
		this.setmOutputStream(mOutputStream);
		this.setBuffer(new byte[5]);
	}

	@Override
	public void run() {
		do {
			//this.setUpdatable(false);
				
				try {
					mOutputStream.write(sendingBuffer);
					//mInputStream.read
				} catch (IOException e) {
				
			}
				//Log.i("MINE", sendingBuffer[0]+" "+sendingBuffer[1]+" "+sendingBuffer[2]+" "+sendingBuffer[3]);
				synchronized (this) {
					this.notifyAll();
				}
				//this.setUpdatable(true);
			
		} while (mOutputStream != null);
	}

	public  void updatePacket(int[] packetToBeSent) {
		sendingBuffer[0] = (byte) (packetToBeSent[0]);
		sendingBuffer[1] = (byte) (packetToBeSent[1]);
		sendingBuffer[2] = (byte) (packetToBeSent[2]);
		sendingBuffer[3] = (byte) (packetToBeSent[3]);
		sendingBuffer[4] = (byte) (packetToBeSent[4]);
	}

}
