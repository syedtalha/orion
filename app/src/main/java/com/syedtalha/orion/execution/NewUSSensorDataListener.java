package com.syedtalha.orion.execution;

import java.util.EventListener;

public interface NewUSSensorDataListener extends EventListener {
	void OnNewUSSensorData(byte[] newRangeValues);
}
