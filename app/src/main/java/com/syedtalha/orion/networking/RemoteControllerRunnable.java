package com.syedtalha.orion.networking;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import com.syedtalha.orion.utils.OrionUtilsAndConstants;

public class RemoteControllerRunnable implements Runnable{
	Socket socket;
	InetAddress serversAddress;
	OutputStream out;
	byte[] buffer;
	
	int xSpeed=0;
	int ySpeed=0;

	
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
	
	public InetAddress getServersAddress() {
		return serversAddress;
	}
	public void setServersAddress(InetAddress serversAddress) {
		this.serversAddress = serversAddress;
	}
	
	
	@Override
	public void run() {
		try {
			socket = new Socket(serversAddress, OrionUtilsAndConstants.SERVER_SOCKET_PORT);
			//socket = new DatagramSocket(OrionUtilsAndConstants.SERVER_SOCKET_PORT, serversAddress);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			socket.setSendBufferSize(8);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			socket.setTcpNoDelay(true);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			out = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//receptionBuffer = new byte[4];
		byte[] first, second;
		do {
			first = OrionUtilsAndConstants.toByteArray(xSpeed);
			second= OrionUtilsAndConstants.toByteArray(ySpeed);
			buffer = new byte[] {first[0],first[1],first[2],first[3],second[0],second[1],second[2],second[3]};
			//receptionBuffer[0]=(byte) this.getxSpeed();
			//receptionBuffer[1]=(byte) this.getySpeed();
			try {
				out.write(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	while(true);
	}

	
}
