package com.syedtalha.orion.application;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Application;

public class OrionApplication extends Application {
	public  FileInputStream globalFileInputStream;
	public FileInputStream getGlobalFileInputStream() {
		return globalFileInputStream;
	}



	public void setGlobalFileInputStream(FileInputStream globalFileInputStream) {
		this.globalFileInputStream = globalFileInputStream;
	}



	public FileOutputStream getGlobalFileOutputStream() {
		return globalFileOutputStream;
	}



	public void setGlobalFileOutputStream(FileOutputStream globalFileOutputStream) {
		this.globalFileOutputStream = globalFileOutputStream;
	}



	public  FileOutputStream globalFileOutputStream;
	@Override
	  public void onCreate()
	  {
	    super.onCreate();
	     
	     // Initialize the singletons so their instances
	    // are bound to the application process.
	  }
	 
	 
	   
	 
}
