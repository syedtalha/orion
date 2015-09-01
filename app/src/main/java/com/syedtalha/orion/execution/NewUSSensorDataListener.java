package com.syedtalha.orion.execution;

import java.util.EventListener;

public interface NewUSSensorDataListener extends EventListener {
	public void OnNewUSSensorData(byte[] newRangeValues);
}
