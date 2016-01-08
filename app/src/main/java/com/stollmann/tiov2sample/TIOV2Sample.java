package com.stollmann.tiov2sample;

import com.stollmann.shared.STTrace;
import com.stollmann.terminalIO.TIOManager;

import android.app.Application;

public class TIOV2Sample extends Application {

	public static final String PERIPHERAL_ID_NAME = "com.stollmann.tiov2sample.peripheralId";
	
	@Override
	public void onCreate() {
	
		STTrace.setTag("TIOV2Sample");
		STTrace.method("onCreate");
		
		TIOManager.initialize(this.getApplicationContext());
	}

}
