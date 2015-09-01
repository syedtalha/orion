package com.syedtalha.orion.execution;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import android.content.Context;
import android.util.Log;

import com.syedtalha.orion.models.VehicleSystem;
import com.syedtalha.orion.utils.OrionUtilsAndConstants;

public class BotControlRunnable implements Runnable {
	public  FileInputStream mInputStream;
	public  FileOutputStream mOutputStream;
	VehicleSystem mVehicleSystem;
	public int xSpeed,ySpeed;
	public final static int BOT_CONTROL_RUNNABLE_AUTONOMOUS_CONTEXT =1;
	public final static int BOT_CONTROL_RUNNABLE_NATIVE_MANUAL_OVERRIDE_CONTEXT=2;
	public final static int BOT_CONTROL_RUNNABLE_REMOTE_SERVER_MANUAL_OVERRIDE_CONTEXT=3;
	public int botControlRunnableContext;
	
	int currentX=0;
	int currentY=0;
	
	
	ServerSocket serverSocket;
	Socket socket;
	InputStream in;
	byte[] serverBuffer;
	
	public int getxSpeed() {
		return xSpeed;
	}

	public void setxSpeed(int xSpeed) {
		this.xSpeed = xSpeed;
	}

	public int getySpeed() {
		return ySpeed;
	}

	public void setySpeed(int ySpeed) {
		this.ySpeed = ySpeed;
	}

	public BotControlRunnable(FileInputStream mInputStream,FileOutputStream mOutputStream, Context context, int botControlRunnableContext) {
		this.mInputStream = mInputStream;
		this.mOutputStream = mOutputStream;
		this.botControlRunnableContext= botControlRunnableContext;
		mVehicleSystem = new VehicleSystem(mInputStream, mOutputStream, context);
	}

	@Override
	public void run() {
		mVehicleSystem.beginCommunication();
		boolean truth = true;
		
		
		
		switch (this.botControlRunnableContext) {
		case BOT_CONTROL_RUNNABLE_AUTONOMOUS_CONTEXT :	{
			//float targetHead = mVehicleSystem.getGyroValues();
			
			//mVehicleSystem.rotateToHeading(2.5f, 2.0);
			mVehicleSystem.attainAndMaintainHeadingBySpeed(90, 2.0f, 25);
			mVehicleSystem.manouverToHeadingWithTranslation(3.0f,25.0);
			//mVehicleSystem.attainForwardSpeedSoft(150);
			//mVehicleSystem.stopThrottles();		//unregister listeners.. all of them
			break;
		}
		case BOT_CONTROL_RUNNABLE_NATIVE_MANUAL_OVERRIDE_CONTEXT	:	{
			
			do {
				remoteIteration();	
				//Log.i("MINE", mVehicleSystem.backSensor.getMostRecentRange()+" "+mVehicleSystem.frontSensor.getMostRecentRange()+" "+mVehicleSystem.rightSensor.getMostRecentRange()+" "+mVehicleSystem.leftSensor.getMostRecentRange()+" ");
			} while (truth);
			break;
		}
		case  BOT_CONTROL_RUNNABLE_REMOTE_SERVER_MANUAL_OVERRIDE_CONTEXT	: {
			try {
				serverSocket = new ServerSocket(OrionUtilsAndConstants.SERVER_SOCKET_PORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try {
				socket.setTcpNoDelay(true);
			} catch (SocketException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			
			
			
			try {
				socket.setReceiveBufferSize(8);
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				in = socket.getInputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serverBuffer = new byte[8];
			//byte[] first, second;
			do 	{
				try {
					in.read(serverBuffer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
				
				this.setxSpeed(OrionUtilsAndConstants.fromByteArray(new byte[] {serverBuffer[0],serverBuffer[1],serverBuffer[2],serverBuffer[3]}));
				this.setySpeed(OrionUtilsAndConstants.fromByteArray(new byte[] {serverBuffer[4],serverBuffer[5],serverBuffer[6],serverBuffer[7]}));
				//Log.i("MINE", "SB0: "+serverBuffer[0]+" SB1: "+serverBuffer[0]);
				remoteIteration();
			}	while(true);
			
		}

		default:
			break;
		}
		
		
		
		
	}
	
	
	
	
	
	
	public void remoteIteration()	{
		currentX=this.getxSpeed();
		currentY=this.getySpeed();
	
		
			
		if (currentY>=0&&currentY<255&&currentX>50)	{
			mVehicleSystem.frontLeftMotor.changeSpeed(currentX);
			mVehicleSystem.frontRightMotor.changeSpeed(currentX-currentY/2);
			mVehicleSystem.rearLeftMotor.changeSpeed(currentX);
			mVehicleSystem.rearRightMotor.changeSpeed(currentX-currentY/2);
		}	else if (currentY<0&&currentY>-255&&currentX>50)	{
			mVehicleSystem.frontLeftMotor.changeSpeed(currentX-Math.abs(currentY/2));
			mVehicleSystem.frontRightMotor.changeSpeed(currentX);
			mVehicleSystem.rearLeftMotor.changeSpeed(currentX-Math.abs(currentY/2));
			mVehicleSystem.rearRightMotor.changeSpeed(currentX);
		} else if (currentY>=0&&currentY<255&&currentX<-50)	{
			mVehicleSystem.frontLeftMotor.changeSpeed(currentX);
			mVehicleSystem.frontRightMotor.changeSpeed(currentX+(currentY/2));
			mVehicleSystem.rearLeftMotor.changeSpeed(currentX);
			mVehicleSystem.rearRightMotor.changeSpeed(currentX+(currentY/2));
		} else if (currentY<0&&currentY>-255&&currentX<-50)	{
			mVehicleSystem.frontLeftMotor.changeSpeed(currentX+Math.abs(currentY/2));
			mVehicleSystem.frontRightMotor.changeSpeed(currentX);
			mVehicleSystem.rearLeftMotor.changeSpeed(currentX+Math.abs(currentY/2));
			mVehicleSystem.rearRightMotor.changeSpeed(currentX);
		}	else if  ((currentY==255)&&(currentX<50&&currentX>-50))	{
			mVehicleSystem.turnClockWiseRight();
		}	else if ((currentY==-255)&&(currentX<50&&currentX>-50))	{
			mVehicleSystem.turnAntiClockwiseLeft();
		}
		
	}

}
