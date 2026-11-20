package com.myazan.app;
import android.app.*;import android.content.*;import android.os.*;import java.util.*;
public final class AzanScheduler {
 private AzanScheduler(){}
 private static PendingIntent pending(Context c,int index){
  Intent i=new Intent(c,AzanReceiver.class).setAction("com.myazan.app.PRAYER_"+index).putExtra("prayer",PrayerTimes.NAMES[index]);
  return PendingIntent.getBroadcast(c,100+index,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
 }
 public static boolean exactAllowed(Context c){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);return a!=null&&(Build.VERSION.SDK_INT<31||a.canScheduleExactAlarms());}
 public static void schedule(Context c){
  AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(a==null)return;
  long now=System.currentTimeMillis();
  for(int i=0;i<5;i++){
   PendingIntent p=pending(c,i);a.cancel(p);
   if(!Prefs.enabled(c,PrayerTimes.NAMES[i]))continue;
   for(int days=0;days<3;days++){
    Calendar date=Calendar.getInstance();date.add(Calendar.DATE,days);
    Long when=PrayerTimes.calculate(date.getTime(),Prefs.latitude(c),Prefs.longitude(c),Prefs.method(c),Prefs.hanafi(c),Prefs.offsets(c)).get(PrayerTimes.NAMES[i]);
    if(when!=null&&when>now+1500){
      if(exactAllowed(c))a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,p);
      else a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,p);
      break;
    }
   }
  }
 }
}