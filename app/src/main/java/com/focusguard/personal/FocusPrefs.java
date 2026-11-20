package com.focusguard.personal;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public final class FocusPrefs {
    private static final String PREFS="focusguard_local_v1";
    private FocusPrefs(){}
    public static SharedPreferences get(Context c){return c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    public static boolean master(Context c){return get(c).getBoolean("master",true);}
    public static void master(Context c,boolean enabled){get(c).edit().putBoolean("master",enabled).apply();}
    public static Set<String> selectedApps(Context c){
        return new HashSet<>(get(c).getStringSet("selected_apps",new HashSet<>()));
    }
    public static void selectedApps(Context c,Set<String> apps){
        get(c).edit().putStringSet("selected_apps",new HashSet<>(apps)).apply();
    }
    public static boolean appEnabled(Context c,String pkg){return get(c).getBoolean("app_enabled_"+pkg,true);}
    public static void appEnabled(Context c,String pkg,boolean value){
        get(c).edit().putBoolean("app_enabled_"+pkg,value).apply();
    }
    public static boolean reelsEnabled(Context c,String platform){
        return get(c).getBoolean("reel_enabled_"+platform,false);
    }
    public static void reelsEnabled(Context c,String platform,boolean value){
        get(c).edit().putBoolean("reel_enabled_"+platform,value).apply();
    }
    public static int start(Context c,String key){return get(c).getInt("start_"+key,9*60);}
    public static int end(Context c,String key){return get(c).getInt("end_"+key,18*60);}
    public static void schedule(Context c,String key,int start,int end){
        get(c).edit().putInt("start_"+key,start).putInt("end_"+key,end).apply();
    }
    public static boolean during(Context c,String key){
        Calendar now=Calendar.getInstance();
        int min=now.get(Calendar.HOUR_OF_DAY)*60+now.get(Calendar.MINUTE);
        int s=start(c,key),e=end(c,key);
        if(s==e)return true; // equal start/end means all day
        if(s<e)return min>=s && min<e;
        return min>=s || min<e; // crosses midnight
    }
    public static String time(int minutes){
        int hour=(minutes/60)%24;
        return String.format(java.util.Locale.getDefault(),"%02d:%02d",hour,minutes%60);
    }
    public static String summary(Context c,String key){
        int s=start(c,key),e=end(c,key);
        return s==e?"All day":time(s)+" – "+time(e);
    }
    public static boolean blockedApp(Context c,String pkg){
        return master(c)&&selectedApps(c).contains(pkg)&&appEnabled(c,pkg)&&during(c,"app_"+pkg);
    }
    public static boolean blockedReel(Context c,String platform){
        return master(c)&&reelsEnabled(c,platform)&&during(c,"reel_"+platform);
    }
}