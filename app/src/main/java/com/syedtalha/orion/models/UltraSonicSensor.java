package com.syedtalha.orion.models;

import com.syedtalha.orion.execution.NewUSSensorDataListener;

public class UltraSonicSensor implements NewUSSensorDataListener {
	enum UltraSonicSensorPosition {
	    FRONT,LEFT,RIGHT,REAR
	}
	int currentRange;
	UltraSonicSensorPosition mPosition;
	
	public UltraSonicSensor(UltraSonicSensorPosition mPosition) {
		this.mPosition = mPosition;
		// TODO Auto-generated constructor stub
		
	}

	public void startListening()		{
		
	}
	public int getMostRecentRange()	{
		return this.currentRange;
	}
	public void setRange(int newRange)	{
		this.currentRange = newRange;
	}
	
	
	
	

	@Override
	public void OnNewUSSensorData(byte[] newRangeValues) {
		// TODO Auto-generated method stub
		switch (mPosition) {
		case FRONT:	{
			setRange(newRangeValues[0]);
			break;
		}
		case REAR:	{
			setRange(newRangeValues[1]);
			break;
		}
		case RIGHT:	{
			setRange(newRangeValues[2]);
			break;
		}
		case LEFT:	{
			setRange(newRangeValues[3]);
			break;
		}
			
			

		default:
			break;
		}
	}
}
