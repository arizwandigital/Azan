package com.myazan.app;
import java.util.*;
/** Offline solar approximation based on NOAA equations. Compare with local mosque. */
public final class PrayerTimes {
 public static final String[] NAMES={"Fajr","Dhuhr","Asr","Maghrib","Isha"};
 private PrayerTimes(){}
 public static Map<String,Long> calculate(Date date,double lat,double lon,String method,boolean hanafi,int[] offsets){
  Calendar c=Calendar.getInstance(); c.setTime(date);
  int y=c.get(Calendar.YEAR),m=c.get(Calendar.MONTH),d=c.get(Calendar.DAY_OF_MONTH);
  Calendar noon=Calendar.getInstance();noon.clear();noon.set(y,m,d,12,0,0);
  int n=noon.get(Calendar.DAY_OF_YEAR),days=noon.getActualMaximum(Calendar.DAY_OF_YEAR);
  double gamma=2*Math.PI/days*(n-1.0);
  double eq=229.18*(0.000075+0.001868*Math.cos(gamma)-0.032077*Math.sin(gamma)-0.014615*Math.cos(2*gamma)-0.040849*Math.sin(2*gamma));
  double dec=0.006918-0.399912*Math.cos(gamma)+0.070257*Math.sin(gamma)-0.006758*Math.cos(2*gamma)+0.000907*Math.sin(2*gamma)-0.002697*Math.cos(3*gamma)+0.00148*Math.sin(3*gamma);
  double tz=TimeZone.getDefault().getOffset(noon.getTimeInMillis())/60000.0;
  double mid=720-4*lon-eq+tz;
  double sunset=mid+solar(lat,dec,-0.833),fa=18,ia=17;int ishaGap=0;
  switch(method){case "ISNA":fa=15;ia=15;break;case "Egypt":fa=19.5;ia=17.5;break;case "Umm al-Qura":fa=18.5;ishaGap=90;break;case "Karachi":fa=18;ia=18;break;default:break;}
  double asrAlt=Math.toDegrees(Math.atan(1.0/((hanafi?2:1)+Math.tan(Math.abs(Math.toRadians(lat)-dec)))));
  double[] minutes={mid-solar(lat,dec,-fa),mid+1,mid+solar(lat,dec,asrAlt),sunset,ishaGap>0?sunset+ishaGap:mid+solar(lat,dec,-ia)};
  Map<String,Long> out=new LinkedHashMap<>();
  Calendar start=Calendar.getInstance();start.clear();start.set(y,m,d,0,0,0);
  for(int i=0;i<5;i++){if(!Double.isFinite(minutes[i]))continue;Calendar at=(Calendar)start.clone();at.add(Calendar.MINUTE,(int)Math.round(minutes[i])+offsets[i]);out.put(NAMES[i],at.getTimeInMillis());}
  return out;
 }
 private static double solar(double lat,double dec,double altitude){double phi=Math.toRadians(lat),a=Math.toRadians(altitude);double x=(Math.sin(a)-Math.sin(phi)*Math.sin(dec))/(Math.cos(phi)*Math.cos(dec));return x < -1 || x>1 ? Double.NaN:Math.toDegrees(Math.acos(x))*4;}
}