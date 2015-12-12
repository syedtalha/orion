package com.syedtalha.orion.models;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.Currency;
import java.util.Random;

import android.content.Context;
import android.support.v4.util.CircularArray;
import android.util.Log;

import com.syedtalha.orion.execution.CommunicationDumbSensorDataReceptionRunnable;
import com.syedtalha.orion.execution.CommunicationSendingRunnable;
import com.syedtalha.orion.execution.robotlogic.PIDController;
import com.syedtalha.orion.models.UltraSonicSensor.UltraSonicSensorPosition;

public class VehicleSystem {
	enum MotorPosition {
	    FRONT_RIGHT,FRONT_LEFT,REAR_RIGHT,REAR_LEFT 
	}
	
	//MISCELANEOUS
	final CommunicationSendingRunnable commnSendingRunnable;
	CommunicationDumbSensorDataReceptionRunnable commnReceptionRunnable;
	RobotControlRunnable robotControlRunnable;
	BigInteger directionBitSet;
	Thread commnSendThread, commnReceptionThread;
	Thread robotControlThread;
	
	//ANDROID SENSORS
	FusedGyroscopeModel gyro;
	
	//ARDUINO MODULES
	public DCMotor frontRightMotor, frontLeftMotor, rearRightMotor, rearLeftMotor;
	public UltraSonicSensor frontSensor, backSensor, rightSensor, leftSensor;

	public VehicleSystem(FileInputStream mInputStream,
			FileOutputStream mOutputStream, Context context) {
		commnSendingRunnable = new CommunicationSendingRunnable(mInputStream, mOutputStream);
		commnReceptionRunnable = new CommunicationDumbSensorDataReceptionRunnable(mInputStream);
		robotControlRunnable = new RobotControlRunnable();
		commnSendThread = new Thread( this.commnSendingRunnable);
		commnSendThread.setPriority(Thread.MAX_PRIORITY);
		commnReceptionThread = new Thread(commnReceptionRunnable);
		commnReceptionThread.setPriority(Thread.MAX_PRIORITY);
		robotControlThread = new Thread(null, this.robotControlRunnable, "MINE");
		robotControlThread.setPriority(Thread.MAX_PRIORITY);
		directionBitSet = new BigInteger(4, new Random());
		frontRightMotor = new DCMotor(MotorPosition.FRONT_RIGHT);
		frontLeftMotor = new DCMotor(MotorPosition.FRONT_LEFT);
		rearRightMotor = new DCMotor(MotorPosition.REAR_RIGHT);
		rearLeftMotor = new DCMotor(MotorPosition.REAR_LEFT);

		frontSensor = new UltraSonicSensor(UltraSonicSensorPosition.FRONT);
		backSensor = new UltraSonicSensor(UltraSonicSensorPosition.REAR);
		rightSensor = new UltraSonicSensor(UltraSonicSensorPosition.RIGHT);
		leftSensor = new UltraSonicSensor(UltraSonicSensorPosition.LEFT);
		
		gyro = new FusedGyroscopeModel(context);
	}

	
	
	public void beginCommunication() {
		robotControlThread.start();
		commnSendThread.start();
		commnReceptionThread.start();
		commnReceptionRunnable.addNewUSSensorDataListener(backSensor);
		commnReceptionRunnable.addNewUSSensorDataListener(frontSensor);
		commnReceptionRunnable.addNewUSSensorDataListener(rightSensor);
		commnReceptionRunnable.addNewUSSensorDataListener(leftSensor);
		gyro.beginListening();

	}
	
	public boolean tearDown()	{
		gyro.stopListening();
		robotControlThread.interrupt();
		commnSendThread.interrupt();
		commnReceptionThread.interrupt();
		return true;
	}

	public boolean stopThrottles() {

		frontLeftMotor.stopThrottle();
		frontRightMotor.stopThrottle();
		rearLeftMotor.stopThrottle();
		rearRightMotor.stopThrottle();

		return true;
	}
	
	public void attainAndMaintainHeadingBySpeed(int minTargetSpeed, float targetHeading, float tolerance)	{
		attainSpeedHard(minTargetSpeed);
		double PID_Kp = 4.5; 
        double PID_Ki = 0.01;
        double PID_Kd = -0.5;
        PIDController pid = new PIDController(PID_Kp, PID_Ki, PID_Kd);
       // pid.setSetpoint(this.getGyroValues());
		//Log.i("MINE", "setting setpoint  most probable: "+pid.getSetpoint()+"Is it the same as :"+this.getGyroValues());
        pid.setSetpoint(targetHeading);
		pid.setTolerance(tolerance);
		pid.setContinuous();
		pid.setInputRange(-3.14, 3.14);
		pid.setOutputRange(-(Math.max(Math.abs(255-minTargetSpeed), Math.abs(0-minTargetSpeed))), Math.max(Math.abs(255-minTargetSpeed), Math.abs(0-minTargetSpeed)));
		pid.enable();
		
		do	{
			//Log.i("MINE", "Current Gyro Value :"+ gyro.getCurrentValues());
			pid.setCurrentInput(gyro.getCurrentValues());
			double resultantOP = pid.performPID();
			//Log.i("MINE", "PID "+pid.getP()+" "+pid.getI()+" "+pid.getD()+" Current Gyro: "+ gyro.getCurrentValues()+"  PID resultant: " + resultantOP+"Err:"+pid.getError());
			if (resultantOP<0.000000000f)	{
				//tiltForwardLeftHard((Math.abs((int)resultantOP)), minTargetSpeed);
				tiltForwardLeftHard(255-minTargetSpeed, minTargetSpeed);
				//tiltForwardLeft(1);

			}
			if (resultantOP>0.000000000f)	{
				//tiltForwardRightHard((Math.abs((int)resultantOP)), minTargetSpeed);
				tiltForwardRightHard(255-minTargetSpeed, minTargetSpeed);
				//tiltForwardRight(1);

			}
		
		//} while (true);
			//}	while(this.frontSensor.getMostRecentRange()>50||this.frontSensor.getMostRecentRange()==0);
		}	while(!pid.onTarget());
		this.stopThrottles();
		
		//return true;
	}

	public boolean attainBackwardSpeedSoft(int targetSpeed) {
		//assuming speed is not negative at this point
		do {
			frontLeftMotor.decrementSpeedBy(1);
			frontRightMotor.decrementSpeedBy(1);
			rearLeftMotor.decrementSpeedBy(1);
			rearRightMotor.decrementSpeedBy(1);
		} while (frontLeftMotor.getNextSpeed() > -targetSpeed);

		return true;
	}
	public boolean attainForwardSpeedSoft(int targetSpeed) {
		//assuming speed is not negative at this point
		do {
			frontLeftMotor.incrementSpeedBy(1);
			frontRightMotor.incrementSpeedBy(1);
			rearLeftMotor.incrementSpeedBy(1);
			rearRightMotor.incrementSpeedBy(1);
		} while (frontLeftMotor.getNextSpeed() < targetSpeed);

		return true;
	}

	public boolean attainSpeedHard(int targetSpeed) {
		frontLeftMotor.changeSpeed(targetSpeed);
		frontRightMotor.changeSpeed(targetSpeed);
		rearLeftMotor.changeSpeed(targetSpeed);
		rearRightMotor.changeSpeed(targetSpeed);

		return true;
	}

	public boolean tiltBackwardLeftHard(int bySteps, int minValue) {
		if (areAllWheelsMovingBackward())	{
				frontLeftMotor.decrementBackwardSpeedToNegativeValueBy(bySteps, minValue);
				rearLeftMotor.decrementBackwardSpeedToNegativeValueBy(bySteps, minValue);
				
				frontRightMotor.incrementBackwardSpeedFromZeroBy(bySteps);
				rearRightMotor.incrementBackwardSpeedFromZeroBy(bySteps);
			return true;
		}	else {
			return false;
		}
	
	}

	

	

	public boolean tiltBackwarRightHard(int bySteps, int minValue) {
		if (areAllWheelsMovingBackward())	{
			frontRightMotor.decrementBackwardSpeedToNegativeValueBy(bySteps, minValue);
			rearRightMotor.decrementBackwardSpeedToNegativeValueBy(bySteps, minValue);
			
			frontLeftMotor.incrementBackwardSpeedFromZeroBy(bySteps);
			rearLeftMotor.incrementBackwardSpeedFromZeroBy(bySteps);
		return true;
	}	else {
		return false;
	}
	
	}

	public boolean tiltForwardRightHard(int bySteps, int minValue) {
		if (areAllWheelsMovingForward())	{
				frontLeftMotor.incrementForwardSpeedFromZeroBy(bySteps);
				rearLeftMotor.incrementForwardSpeedFromZeroBy(bySteps);
				
				frontRightMotor.decrementForwardSpeedToPositiveValueBy(bySteps,minValue);
				rearRightMotor.decrementForwardSpeedToPositiveValueBy(bySteps,minValue);
	
			return true;
		}	else {
			return false;
		}
	
	}

	

	public boolean tiltForwardLeftHard(int bySteps, int minValue) {
		if (areAllWheelsMovingForward())	{
				frontRightMotor.incrementForwardSpeedFromZeroBy(bySteps);
				rearRightMotor.incrementForwardSpeedFromZeroBy(bySteps);
				
				
				frontLeftMotor.decrementForwardSpeedToPositiveValueBy(bySteps,minValue);
				rearLeftMotor.decrementForwardSpeedToPositiveValueBy(bySteps,minValue);
			return true;
		}	else {
			return false;
		}
	
	}
	
	public boolean tiltForwardRightSoft(int bySteps, int minValue) {
		if (areAllWheelsMovingForward())	{
			for(;bySteps>0;bySteps--)	{
				frontLeftMotor.incrementForwardSpeedFromZeroBy(1);
				rearLeftMotor.incrementForwardSpeedFromZeroBy(1);
				
				frontRightMotor.decrementForwardSpeedToPositiveValueBy(1,minValue);
				rearRightMotor.decrementForwardSpeedToPositiveValueBy(1,minValue);
			}
			return true;
		}	else {
			return false;
		}
	
	}

	

	public boolean tiltForwardLeftSoft(int bySteps, int minValue) {
		if (areAllWheelsMovingForward())	{
			for(;bySteps>0;bySteps--)	{
				frontRightMotor.incrementForwardSpeedFromZeroBy(1);
				rearRightMotor.incrementForwardSpeedFromZeroBy(1);
				
				
				frontLeftMotor.decrementForwardSpeedToPositiveValueBy(1,minValue);
				rearLeftMotor.decrementForwardSpeedToPositiveValueBy(1,minValue);
			}
			return true;
		}	else {
			return false;
		}
	
	}

	public boolean areAllWheelsMovingForward() {
		return (frontRightMotor.isMovingForward()&&frontLeftMotor.isMovingForward()&&rearLeftMotor.isMovingForward()&&rearRightMotor.isMovingForward());
		//return true;
	}
	
	private boolean areAllWheelsMovingBackward() {
		return (frontRightMotor.isMovingBackward()&&frontLeftMotor.isMovingBackward()&&rearLeftMotor.isMovingBackward()&&rearRightMotor.isMovingBackward());

		// TODO Auto-generated method stub
		//return false;
	}

	public void test() {
		
		int i;
		for (i = 0; i <= 255; i++) {
			frontRightMotor.changeSpeed(i);
	}
		for (i = 0; i <= 255; i++) {
			frontLeftMotor.changeSpeed(i);
	}
		for (i = 0; i <= 255; i++) {
			rearRightMotor.changeSpeed(i);
	}
		for (i = 0; i <= 255; i++) {
			rearLeftMotor.changeSpeed(i);
	}
		
		
	}
	
	public boolean rotateToHeading(float toFusedGyroHeading, double percentTolerance)		{
		double PID_Kp = 4.5; 
        double PID_Ki = 0.01;
        double PID_Kd = -0.5;
        PIDController pid = new PIDController(PID_Kp, PID_Ki, PID_Kd);
        pid.setSetpoint(toFusedGyroHeading);
		pid.setTolerance(percentTolerance);
		pid.setContinuous();
		pid.setInputRange(-3.14, 3.14);
		pid.setOutputRange(-255, 255);
		pid.enable();
		boolean truth = true;
		do {
			pid.setCurrentInput(gyro.getCurrentValues());
			double resultantOP = pid.performPID();
			//Log.i("MINE", "PID "+pid.getP()+" "+pid.getI()+" "+pid.getD()+" Current Gyro: "+ gyro.getCurrentValues()+"  PID resultant: " + resultantOP+"Err:"+pid.getError());
			if (resultantOP<0.000000000f)	{
				this.turnAntiClockwiseLeft();
				//tiltForwardLeftHard((Math.abs((int)resultantOP)), minTargetSpeed);

			}
			if (resultantOP>0.000000000f)	{
				this.turnClockWiseRight();
				//tiltForwardRightHard((Math.abs((int)resultantOP)), minTargetSpeed);

			}
		//} while (truth);
			} while (!pid.onTarget());
		this.stopThrottles();
		return true;
	}

	public void turnClockWiseRight() {
		//for (int i = bySteps;i>0;i--)	{
			frontRightMotor.changeSpeed(-255);
			rearRightMotor.changeSpeed(-255);
			frontLeftMotor.changeSpeed(255);
			rearLeftMotor.changeSpeed(0);
		//}
		
	}

	public void turnAntiClockwiseLeft() {
		//for (int i = bySteps;i>0;i--)	{
		frontRightMotor.changeSpeed(255);
		rearRightMotor.changeSpeed(255);
		frontLeftMotor.changeSpeed(-255);
		rearLeftMotor.changeSpeed(-255);
	//}
	
	}
	
	public void turnClockWiseRightWithTranslation() {
		
			this.attainSpeedHard(200);
			this.tiltForwardRightHard(100, 140);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.stopThrottles();
			this.attainSpeedHard(-200);
			this.tiltBackwardLeftHard(100, 140);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.stopThrottles();
		
		
	}

	public void turnAntiClockwiseLeftWithTranslation() {
		
			this.attainSpeedHard(200);
			this.tiltForwardLeftHard(100, 140);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.stopThrottles();
			this.attainSpeedHard(-200);
			this.tiltBackwarRightHard(100, 140);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.stopThrottles();
	
		
	
	}
	public void manouverToHeadingWithTranslation(float targetHeading, double errorPercentMargin)	{
		double PID_Kp = 4.5; 
        double PID_Ki = 0.01;
        double PID_Kd = -0.5;
        PIDController pid = new PIDController(PID_Kp, PID_Ki, PID_Kd);
        pid.setSetpoint(targetHeading);
		pid.setTolerance(errorPercentMargin);
		pid.setContinuous();
		pid.setInputRange(-3.14, 3.14);
		pid.setOutputRange(-255, 255);
		pid.enable();
		do {
			pid.setCurrentInput(this.getGyroValues());
			double resultantOP = pid.performPID();
			//Log.i("MINE", "PID "+pid.getP()+" "+pid.getI()+" "+pid.getD()+" Current Gyro: "+ gyro.getCurrentValues()+"  PID resultant: " + resultantOP+"Err:"+pid.getError());
			if (resultantOP<0.000000000f)	{
				this.turnAntiClockwiseLeftWithTranslation();
				//tiltForwardLeftHard((Math.abs((int)resultantOP)), minTargetSpeed);

			}
			if (resultantOP>0.000000000f)	{
				this.turnClockWiseRightWithTranslation();
				//tiltForwardRightHard((Math.abs((int)resultantOP)), minTargetSpeed);

			}
		} while (!pid.onTarget());
		this.stopThrottles();
	}
	
	public float getGyroValues()	{
		return this.gyro.getCurrentValues();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	public class RobotControlRunnable implements Runnable {

		int[] intPacketToBeSent;

		public RobotControlRunnable() {
			intPacketToBeSent = new int[5];
		}

		@Override
		public void run() {
			do {
				synchronized (commnSendingRunnable)	{
					try {
						commnSendingRunnable.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (frontRightMotor.getNextSpeed() >= 0) {
						directionBitSet = directionBitSet.setBit(0);
					} else {
						directionBitSet = directionBitSet.clearBit(0);
					}

					if (frontLeftMotor.getNextSpeed() >= 0) {
						directionBitSet = directionBitSet.setBit(1);
					} else {
						directionBitSet = directionBitSet.clearBit(1);
					}

					if (rearRightMotor.getNextSpeed() >= 0) {
						directionBitSet = directionBitSet.setBit(2);
					} else {
						directionBitSet = directionBitSet.clearBit(2);
					}
					if (rearLeftMotor.getNextSpeed() >= 0) {
						directionBitSet = directionBitSet.setBit(3);
					} else {
						directionBitSet = directionBitSet.clearBit(3);
					}

					intPacketToBeSent[0] = directionBitSet.byteValue();
					intPacketToBeSent[1] = Math.abs(frontRightMotor.getNextSpeed());
					intPacketToBeSent[2] = Math.abs(frontLeftMotor.getNextSpeed());
					intPacketToBeSent[3] = Math.abs(rearRightMotor.getNextSpeed());
					intPacketToBeSent[4] = Math.abs(rearLeftMotor.getNextSpeed());
					//Log.i("MINE", intPacketToBeSent[0]+" "+intPacketToBeSent[1]+" "+intPacketToBeSent[2]+" "+intPacketToBeSent[3]);
					commnSendingRunnable.updatePacket(intPacketToBeSent);
					//int[] sens = commnReceptionRunnable.getLattestPacket();
					//Log.i("MINE", Integer.toString(sens[0])+" "+Integer.toString(sens[1])+" "+Integer.toString(sens[2])+" "+Integer.toString(sens[0]));
					
				}
				

			} while (true);
		}

	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public class DCMotor {
		private static final int INITIAL_SPEED = 0;
		int previousSpeed;
		int nextSpeed;
		int tempPrevSpeed ;
		MotorPosition motorPosition;
		
		
		
		
		public int getPreviousSpeed() {
			return previousSpeed;
		}


		public boolean isMovingForward() {
			return getNextSpeed() > 0;
		}
		
		
		public boolean isMovingBackward() {
			return getNextSpeed() < 0;
		}

		public boolean isStopped()		{
			return getNextSpeed() == 0;
			
		}

		public void setPreviousSpeed(int previousSpeed) {
			this.previousSpeed = previousSpeed;
		}


		public int getNextSpeed() {
			return nextSpeed;
		}


		public  boolean setNextSpeed(int nextSpeed) {
			if (nextSpeed>255||nextSpeed<(-255))	{
				return false;
			}	
			else 	{
				this.nextSpeed = nextSpeed;
				return true;
			}
		}

		
		public DCMotor(MotorPosition motorPosition) {
			this.motorPosition=motorPosition;
			this.setPreviousSpeed(0);
			this.setNextSpeed(INITIAL_SPEED);
		}
		
		
		public void applyBreak()	{
			
		}

		public void stopThrottle()	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tempPrevSpeed = this.getNextSpeed();
				this.setNextSpeed(0); 
				this.setPreviousSpeed(tempPrevSpeed);
			}
		}
		
		public  boolean incrementBackwardSpeedFromZeroBy(int bySteps)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((this.getNextSpeed()>=-255)&&this.getNextSpeed()<=0)	{
					tempPrevSpeed = this.getNextSpeed();
					this.setNextSpeed(tempPrevSpeed-bySteps); 
					this.setPreviousSpeed(tempPrevSpeed);
					return true;
				}	else	{
					return false;
				}
				
			}
					
			}
		
		public  boolean decrementBackwardSpeedToNegativeValueBy(int bySteps,int toValue)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((this.getNextSpeed()>=-255)&&this.getNextSpeed()<toValue)	{
					tempPrevSpeed = this.getNextSpeed();
					this.setNextSpeed(tempPrevSpeed+bySteps); 
					this.setPreviousSpeed(tempPrevSpeed);
					return true;
				}	else	{
					return false;
				}
				
			}
					
			}
		
		public  boolean incrementForwardSpeedFromZeroBy(int bySteps)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((this.getNextSpeed()<=255)&&this.getNextSpeed()>=0)	{
					tempPrevSpeed = this.getNextSpeed();
					this.setNextSpeed(tempPrevSpeed+bySteps); 
					this.setPreviousSpeed(tempPrevSpeed);
					return true;
				}	else	{
					return false;
				}
				
			}
					
			}
		
		public  boolean decrementForwardSpeedToPositiveValueBy(int bySteps, int toValue)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if ((this.getNextSpeed()<=255)&&this.getNextSpeed()>toValue)	{
					tempPrevSpeed = this.getNextSpeed();
					this.setNextSpeed(tempPrevSpeed-bySteps); 
					this.setPreviousSpeed(tempPrevSpeed);
					return true;
				}	else	{
					return false;
				}
				
			}
					
			}
		public  void incrementSpeedBy(int bySteps)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tempPrevSpeed = this.getNextSpeed();
				this.setNextSpeed(tempPrevSpeed+bySteps); 
				if (this.getNextSpeed() >255)	{
					this.setNextSpeed(255);
				}
				this.setPreviousSpeed(tempPrevSpeed);
			}
					
			}
		public  void decrementSpeedBy(int bySteps)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tempPrevSpeed = this.getNextSpeed();
				this.setNextSpeed(tempPrevSpeed-bySteps); 
				if (this.getNextSpeed() < -255)	{
					this.setNextSpeed(-255);
				}
				this.setPreviousSpeed(tempPrevSpeed);
			}
					
			}
		public  void changeSpeed(int toSpeed)	{
			synchronized (commnSendingRunnable)	{
				try {
					commnSendingRunnable.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tempPrevSpeed = this.getNextSpeed();
				this.setNextSpeed(toSpeed); 
				this.setPreviousSpeed(tempPrevSpeed);
			}
		}
		
		
		
	}


}
