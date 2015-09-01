package com.syedtalha.orion.models;


public class PointToPointTask  {
int start, end;
	public  PointToPointTask() {
	
		// TODO Auto-generated constructor stub
	}
	public boolean abort()	{
		return true;
	}
	
	public TraversalPath calculatePath()	{
		return new TraversalPath();
	}
	public boolean followPath(TraversalPath traversalPath)	{
		return true;
	}
	
}
