package de.simu.decoit.android.decomap.observer.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

import de.simu.decoit.android.decomap.messaging.MessageParameter;

/**
* observe use of camera
*
* @author Markus Schölzel, Decoit GmbH
*/
public class CameraReceiver extends BroadcastReceiver {

	private final MessageParameter mp = MessageParameter.getInstance();

	@Override
	public void onReceive(Context arg0, Intent arg1) {
        mp.setLastPictureTakenDate(new Date());
	}
}
