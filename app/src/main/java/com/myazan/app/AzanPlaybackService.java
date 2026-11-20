package com.myazan.app;
import android.app.*;import android.content.*;import android.content.pm.ServiceInfo;import android.media.*;import android.net.Uri;import android.os.*;import android.util.Log;import java.io.File;
public class AzanPlaybackService extends Service {
 public static final String ACTION_PLAY="com.myazan.app.PLAY",ACTION_STOP="com.myazan.app.STOP";
 private MediaPlayer player;private static final String CHANNEL="azan_playback";
 @Override public void onCreate(){super.onCreate();NotificationManager mgr=getSystemService(NotificationManager.class);mgr.createNotificationChannel(new NotificationChannel(CHANNEL,"Azan playback",NotificationManager.IMPORTANCE_LOW));}
 @Override public int onStartCommand(Intent i,int flags,int startId){
  if(i!=null&&ACTION_STOP.equals(i.getAction())){stopSelf();return START_NOT_STICKY;}
  String prayer=i==null?"Prayer":i.getStringExtra("prayer");if(prayer==null)prayer="Prayer";
  Intent stop=new Intent(this,AzanPlaybackService.class).setAction(ACTION_STOP);
  PendingIntent pi=PendingIntent.getService(this,80,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
   .setContentTitle(prayer+" Azan").setContentText("Your recorded Azan is playing").setOngoing(true)
   .addAction(new Notification.Action.Builder(android.R.drawable.ic_media_pause,"Stop Azan",pi).build()).build();
  if(Build.VERSION.SDK_INT>=29)startForeground(7,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);else startForeground(7,n);
  release();String path=Prefs.audioPath(this);
  if(path.isEmpty()||!new File(path).isFile()){stopSelf();return START_NOT_STICKY;}
  try{
   player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
   player.setDataSource(this,Uri.fromFile(new File(path)));player.setOnCompletionListener(mp->stopSelf());
   player.setOnErrorListener((mp,w,e)->{stopSelf();return true;});player.prepare();player.start();
  }catch(Exception e){Log.e("MyAzan","Playback failed",e);stopSelf();}
  return START_NOT_STICKY;
 }
 private void release(){if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();player=null;}}
 @Override public void onDestroy(){release();super.onDestroy();}
 @Override public IBinder onBind(Intent i){return null;}
}