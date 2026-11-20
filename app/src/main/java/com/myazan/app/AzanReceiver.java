package com.myazan.app;
import android.content.*;import android.os.*;import android.util.Log;
public class AzanReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){
  String prayer=i==null?null:i.getStringExtra("prayer");
  if(prayer!=null&&Prefs.enabled(c,prayer)){
   Intent play=new Intent(c,AzanPlaybackService.class).setAction(AzanPlaybackService.ACTION_PLAY).putExtra("prayer",prayer);
   try{if(Build.VERSION.SDK_INT>=26)c.startForegroundService(play);else c.startService(play);}
   catch(Exception e){Log.e("MyAzan","Playback service rejected",e);}
  }
  AzanScheduler.schedule(c);
 }
}