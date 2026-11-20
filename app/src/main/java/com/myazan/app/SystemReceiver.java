package com.myazan.app;
import android.content.*;
public class SystemReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){AzanScheduler.schedule(c);}
}