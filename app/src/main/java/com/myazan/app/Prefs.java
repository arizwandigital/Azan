package com.myazan.app;
import android.content.*; import java.util.*;
public final class Prefs {
 private Prefs(){}
 public static SharedPreferences get(Context c){return c.getSharedPreferences("my_azan_settings",0);}
 public static double latitude(Context c){return Double.longBitsToDouble(get(c).getLong("lat",Double.doubleToLongBits(26.8467)));}
 public static double longitude(Context c){return Double.longBitsToDouble(get(c).getLong("lon",Double.doubleToLongBits(80.9462)));}
 public static void setCoordinates(Context c,double lat,double lon){get(c).edit().putLong("lat",Double.doubleToLongBits(lat)).putLong("lon",Double.doubleToLongBits(lon)).apply();}
 public static String method(Context c){return get(c).getString("method","Karachi");}
 public static boolean hanafi(Context c){return get(c).getBoolean("hanafi",true);}
 public static boolean enabled(Context c,String p){return get(c).getBoolean("enabled_"+p,true);}
 public static int[] offsets(Context c){int[] a=new int[5];for(int i=0;i<5;i++)a[i]=get(c).getInt("offset_"+PrayerTimes.NAMES[i],0);return a;}
 public static Map<String,Long> today(Context c){return PrayerTimes.calculate(new Date(),latitude(c),longitude(c),method(c),hanafi(c),offsets(c));}
 public static String audioPath(Context c){return get(c).getString("audio_path","");}
}