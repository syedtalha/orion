package com.syedtalha.orion.utils;

import java.nio.ByteBuffer;

public class OrionUtilsAndConstants {
	public static final int SERVER_SOCKET_PORT = 5786;
	
	public static byte[] toByteArray(int value) {
	     return  ByteBuffer.allocate(4).putInt(value).array();
	}
	public static int fromByteArray(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).getInt();
	}
}
