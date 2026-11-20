package com.focusguard.personal;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.view.accessibility.AccessibilityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Native UI; no Internet, analytics, or login. */
public class MainActivity extends Activity {
    private static final int DARK=0xFF101D30,BLUE=0xFF243958,ACCENT=0xFF6CE0B8;
    private static final int INK=0xFF1B2938,SUB=0xFF64748B,BG=0xFFF3F6FA,LINE=0xFFE7EBF0;
    private LinearLayout root,appsContainer;
    private TextView accessStatus;
    private final String[] platforms={"instagram","facebook","youtube"};
    private final String[] platformNames={"Instagram Reels","Facebook Reels","YouTube Shorts"};
    private int px(float dp){return (int)(getResources().getDisplayMetrics().density*dp+.5f);}
    @Override public void onCreate(Bundle bundle){
        super.onCreate(bundle);getWindow().setStatusBarColor(DARK);getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        showDashboard();
    }
    private GradientDrawable bg(int color,int radius){
        GradientDrawable drawable=new GradientDrawable();drawable.setColor(color);drawable.setCornerRadius(px(radius));return drawable;
    }
    private TextView text(String value,int size,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        t.setGravity(Gravity.CENTER_VERTICAL);return t;
    }
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private void spacer(LinearLayout l,int h){l.addView(new View(this),new LinearLayout.LayoutParams(1,px(h)));}
    private LinearLayout card(){
        LinearLayout v=col();v.setBackground(bg(Color.WHITE,18));v.setPadding(px(17),px(17),px(17),px(17));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=px(16);root.addView(v,lp);return v;
    }
    private void line(LinearLayout p){View line=new View(this);line.setBackgroundColor(LINE);
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,px(1));params.topMargin=px(11);params.bottomMargin=px(11);p.addView(line,params);
    }
    private Button button(String title,LinearLayout dest,Runnable click,boolean dark){
        Button b=new Button(this);b.setAllCaps(false);b.setText(title);b.setTextSize(13);
        b.setTextColor(dark?Color.WHITE:INK);b.setBackgroundTintList(ColorStateList.valueOf(dark?DARK:0xFFEAF0F7));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,px(46));params.topMargin=px(6);dest.addView(b,params);
        b.setOnClickListener(view->click.run());return b;
    }
    private Switch toggle(boolean checked,java.util.function.Consumer<Boolean> action){
        Switch s=new Switch(this);s.setChecked(checked);s.setShowText(false);
        s.setThumbTintList(new ColorStateList(
            new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},
            new int[]{0xFF128768,0xFFB7C4CD}));
        s.setOnCheckedChangeListener((button,value)->action.accept(value));return s;
    }
    private void showDashboard(){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);
        root=col();root.setPadding(px(18),px(24),px(18),px(26));scroll.addView(root);setContentView(scroll);
        LinearLayout intro=row();LinearLayout introText=col();introText.addView(text("FOCUSGUARD",12,0xFF228A75,true));
        spacer(introText,5);introText.addView(text("Own your time.",27,DARK,true));
        intro.addView(introText,new LinearLayout.LayoutParams(0,-2,1));
        TextView shield=text("◈",31,0xFF168767,true);shield.setGravity(Gravity.CENTER);shield.setBackground(bg(0xFFE0F5ED,18));
        intro.addView(shield,new LinearLayout.LayoutParams(px(58),px(58)));
        root.addView(intro);spacer(root,10);
        root.addView(text("Scheduled app limits and short-video breaks",13,SUB,false));spacer(root,19);
        LinearLayout hero=col();hero.setBackground(bg(DARK,23));hero.setPadding(px(20),px(18),px(20),px(19));
        LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.bottomMargin=px(18);root.addView(hero,hp);
        hero.addView(text("FOCUS PROTECTION",12,ACCENT,true));spacer(hero,9);
        LinearLayout heroRow=row();
        heroRow.addView(text(FocusPrefs.master(this)?"Protection enabled":"Protection paused",19,Color.WHITE,true),
            new LinearLayout.LayoutParams(0,-2,1));
        Switch master=toggle(FocusPrefs.master(this),enabled->{FocusPrefs.master(this,enabled);showDashboard();});
        heroRow.addView(master);hero.addView(heroRow);spacer(hero,12);
        hero.addView(text("Rules are checked on device when the selected app or screen becomes visible.",12,0xFFB5C5D6,false));
        LinearLayout permission=card();permission.addView(text("Required permission",19,INK,true));spacer(permission,7);
        accessStatus=text(accessEnabled()?"✓ Accessibility service enabled":"⚠ Accessibility service is off",14,
            accessEnabled()?0xFF168767:0xFFAE6421,true);permission.addView(accessStatus);
        spacer(permission,8);
        permission.addView(text("FocusGuard needs Accessibility access to detect your foreground app and certain short-video screen indicators, then navigate Home during a block. It checks visible labels locally; no text leaves your device. You can turn this off anytime.",12,SUB,false));
        spacer(permission,8);
        button("Open Accessibility Settings",permission,()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)),true);
        LinearLayout shortVideos=card();shortVideos.addView(text("Reels & Shorts blocking",19,INK,true));spacer(shortVideos,6);
        shortVideos.addView(text("Keep other features available while limiting short videos. Detection is experimental and depends on each app's screen labels.",12,SUB,false));
        for(int i=0;i<platforms.length;i++){
            line(shortVideos);
            final String key=platforms[i];LinearLayout item=row();
            LinearLayout info=col();info.addView(text(platformNames[i],15,INK,true));
            info.addView(text(FocusPrefs.summary(this,"reel_"+key),12,SUB,false));
            item.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            Button clock=new Button(this);clock.setText("Edit");clock.setTextSize(11);clock.setAllCaps(false);
            clock.setTextColor(DARK);clock.setBackgroundTintList(ColorStateList.valueOf(0xFFEAF0F7));
            item.addView(clock,new LinearLayout.LayoutParams(px(60),px(43)));
            clock.setOnClickListener(v->editSchedule("reel_"+key,platformNames[indexOf(platforms,key)]));
            Switch s=toggle(FocusPrefs.reelsEnabled(this,key),v->FocusPrefs.reelsEnabled(this,key,v));
            item.addView(s);shortVideos.addView(item);
        }
        LinearLayout apps=card();
        LinearLayout appsTitle=row();
        appsTitle.addView(text("Full-app schedules",19,INK,true),new LinearLayout.LayoutParams(0,-2,1));
        apps.addView(appsTitle);spacer(apps,6);
        apps.addView(text("Blocked apps return to Home during their individual schedules. Android doesn't allow a regular app to force-stop other apps.",12,SUB,false));
        spacer(apps,10);
        button("+  Select installed apps",apps,()->showAppChooser(),true);
        appsContainer=col();apps.addView(appsContainer);populateApps();
        LinearLayout footer=card();footer.addView(text("How the schedules work",17,INK,true));spacer(footer,8);
        footer.addView(text("Set the same start and end time for all-day blocking. Overnight schedules (such as 22:00–07:00) are supported. Each rule runs only while protection and Android Accessibility are enabled.",12,SUB,false));
        spacer(footer,8);footer.addView(text("This is a personal productivity tool, not an unbreakable parental-control lock. Screen detection may briefly show content before returning Home.",12,SUB,false));
    }
    private int indexOf(String[] a,String s){for(int i=0;i<a.length;i++)if(a[i].equals(s))return i;return 0;}
    private String appName(String pkg){
        try{return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg,0)).toString();}
        catch(Exception e){return pkg;}
    }
    private void populateApps(){
        appsContainer.removeAllViews();
        Set<String> selected=FocusPrefs.selectedApps(this);
        if(selected.isEmpty()){
            spacer(appsContainer,12);
            appsContainer.addView(text("No apps selected. Tap the button to add apps.",13,SUB,false));return;
        }
        List<String> packages=new ArrayList<>(selected);Collections.sort(packages,(a,b)->appName(a).compareToIgnoreCase(appName(b)));
        for(String pkg:packages){
            line(appsContainer);LinearLayout item=row();
            LinearLayout title=col();title.addView(text(appName(pkg),14,INK,true));
            title.addView(text(FocusPrefs.summary(this,"app_"+pkg),12,SUB,false));
            item.addView(title,new LinearLayout.LayoutParams(0,-2,1));
            Button time=new Button(this);time.setText("Edit");time.setAllCaps(false);time.setTextSize(11);
            time.setTextColor(DARK);time.setBackgroundTintList(ColorStateList.valueOf(0xFFEAF0F7));
            item.addView(time,new LinearLayout.LayoutParams(px(60),px(43)));
            time.setOnClickListener(v->editSchedule("app_"+pkg,appName(pkg)));
            Switch s=toggle(FocusPrefs.appEnabled(this,pkg),enabled->FocusPrefs.appEnabled(this,pkg,enabled));
            item.addView(s);appsContainer.addView(item);
        }
    }
    private void showAppChooser(){
        PackageManager pm=getPackageManager();
        Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> found=pm.queryIntentActivities(intent,0);
        Map<String,String> candidates=new HashMap<>();
        for(ResolveInfo item:found){
            if(item.activityInfo==null)continue;
            String pkg=item.activityInfo.packageName;
            if(pkg.equals(getPackageName())||pkg.startsWith("com.android.")||pkg.equals("android"))continue;
            candidates.put(pkg,item.loadLabel(pm).toString());
        }
        List<String> packages=new ArrayList<>(candidates.keySet());
        Collections.sort(packages,Comparator.comparing(x->candidates.get(x).toLowerCase(Locale.ROOT)));
        if(packages.isEmpty()){message("No launchable apps were found on this device.");return;}
        String[] names=new String[packages.size()];boolean[] checks=new boolean[packages.size()];
        Set<String> chosen=FocusPrefs.selectedApps(this);
        for(int i=0;i<packages.size();i++){names[i]=candidates.get(packages.get(i));checks[i]=chosen.contains(packages.get(i));}
        new AlertDialog.Builder(this).setTitle("Apps to block")
          .setMultiChoiceItems(names,checks,(dialog,index,checked)->checks[index]=checked)
          .setNegativeButton("Cancel",null)
          .setPositiveButton("Save",(dialog,which)->{
              Set<String> updated=new HashSet<>();
              for(int i=0;i<packages.size();i++)if(checks[i])updated.add(packages.get(i));
              FocusPrefs.selectedApps(this,updated);populateApps();
          }).show();
    }
    private void editSchedule(String key,String heading){
        final int[] times={FocusPrefs.start(this,key),FocusPrefs.end(this,key)};
        LinearLayout layout=col();layout.setPadding(px(22),px(12),px(22),px(3));
        layout.addView(text("Blocking is active from the start time until the end time every day.",13,SUB,false));
        spacer(layout,14);
        Button start=new Button(this);start.setAllCaps(false);
        Button end=new Button(this);end.setAllCaps(false);
        start.setText("Start: "+FocusPrefs.time(times[0]));end.setText("End: "+FocusPrefs.time(times[1]));
        layout.addView(start);layout.addView(end);
        start.setOnClickListener(v->chooseTime(times,0,start,"Start"));
        end.setOnClickListener(v->chooseTime(times,1,end,"End"));
        spacer(layout,10);layout.addView(text("Tip: 22:00 to 07:00 blocks overnight. Equal times block all day.",12,SUB,false));
        new AlertDialog.Builder(this).setTitle(heading+" schedule").setView(layout)
           .setNegativeButton("Cancel",null)
           .setPositiveButton("All day",(d,w)->{FocusPrefs.schedule(this,key,0,0);showDashboard();})
           .setNeutralButton("Save",(d,w)->{FocusPrefs.schedule(this,key,times[0],times[1]);showDashboard();}).show();
    }
    private void chooseTime(int[] times,int index,Button button,String title){
        int minutes=times[index];
        new TimePickerDialog(this,(picker,hour,min)->{
            times[index]=hour*60+min;button.setText(title+": "+FocusPrefs.time(times[index]));
        },minutes/60,minutes%60,true).show();
    }
    private boolean accessEnabled(){
        AccessibilityManager manager=(AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if(manager==null)return false;
        List<AccessibilityServiceInfo> enabled=manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String own=new ComponentName(this,FocusAccessibilityService.class).flattenToString();
        for(AccessibilityServiceInfo service:enabled){
            if(service.getId()!=null&&service.getId().equalsIgnoreCase(own))return true;
            if(service.getResolveInfo()!=null&&service.getResolveInfo().serviceInfo!=null){
                if(service.getResolveInfo().serviceInfo.packageName.equals(getPackageName())&&
                   service.getResolveInfo().serviceInfo.name.equals(FocusAccessibilityService.class.getName()))return true;
            }
        }
        return false;
    }
    @Override protected void onResume(){super.onResume();if(accessStatus!=null)accessStatus.setText(accessEnabled()?"✓ Accessibility service enabled":"⚠ Accessibility service is off");}
    private void message(String message){new AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK",null).show();}
}