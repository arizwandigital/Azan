package com.myazan.app;
import android.content.*;import java.util.*;
public final class Prefs {
 private Prefs(){}
 public static SharedPreferences get(Context c){return c.getSharedPreferences("my_azan_settings",0);}
 public static double latitude(Context c){return Double.longBitsToDouble(get(c).getLong("lat",Double.doubleToLongBits(26.8467)));}
 public static double longitude(Context c){return Double.longBitsToDouble(get(c).getLong("lon",Double.doubleToLongBits(80.9462)));}
 public static void setCoordinates(Context c,double lat,double lon){get(c).edit().putLong("lat",Double.doubleToLongBits(lat)).putLong("lon",Double.doubleToLongBits(lon)).apply();}
 public static String method(Context c){return get(c).getString("method","Karachi");}
 public static boolean hanafi(Context c){return get(c).getBoolean("hanafi",true);}
 public static boolean enabled(Context c,String p){return get(c).getBoolean("enabled_"+p,true);}
 public static int offset(Context c,String name){return get(c).getInt("offset_"+name,0);}
 public static String fixedTime(Context c,String name){return get(c).getString("fixed_"+name,"");}
 public static int[] offsets(Context c){int[] a=new int[5];for(int i=0;i<5;i++)a[i]=offset(c,PrayerTimes.NAMES[i]);return a;}
 public static Map<String,Long> times(Context c,Date day){
  Map<String,Long> out=PrayerTimes.calculate(day,latitude(c),longitude(c),method(c),hanafi(c),offsets(c));
  Calendar cal=Calendar.getInstance();cal.setTime(day);
  for(String prayer:PrayerTimes.NAMES){
   String fixed=fixedTime(c,prayer);
   if(!fixed.matches("\\d{2}:\\d{2}"))continue;
   try {
    String[] hm=fixed.split(":");int hour=Integer.parseInt(hm[0]),min=Integer.parseInt(hm[1]);
    if(hour>23||min>59)continue;
    Calendar at=(Calendar)cal.clone();at.set(Calendar.HOUR_OF_DAY,hour);at.set(Calendar.MINUTE,min);
    at.set(Calendar.SECOND,0);at.set(Calendar.MILLISECOND,0);
    out.put(prayer,at.getTimeInMillis());
   }catch(Exception ignored){}
  }
  return out;
 }
 public static Map<String,Long> today(Context c){return times(c,new Date());}
 public static String audioPath(Context c){return get(c).getString("audio_path","");}
}