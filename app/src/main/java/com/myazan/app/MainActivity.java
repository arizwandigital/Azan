package com.myazan.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** My Azan dashboard; all settings and audio are stored locally. */
public class MainActivity extends Activity {
 private static final int GREEN=0xFF103F35,DARK=0xFF092E29,LIGHT=0xFFF3F7F4,GOLD=0xFFD9B978,MUTED=0xFF667A72,LINE=0xFFE5EBE7;
 private static final int REQ_AUDIO=10,REQ_LOCATION=11,REQ_NOTIFICATION=12,REQ_IMPORT=13;
 private final String[] METHODS={"Karachi","MWL","ISNA","Egypt","Umm al-Qura"};
 private final String[] ICONS={"☀","☀","◉","☾","★"};
 private LinearLayout root;
 private TextView audioStatus;
 private Button recordButton;
 private MediaRecorder recorder;
 private File recordingFile;
 private boolean recording=false;
 private final SimpleDateFormat timeFormat=new SimpleDateFormat("hh:mm a",Locale.getDefault());
 private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);
  getWindow().setStatusBarColor(DARK);getWindow().setNavigationBarColor(LIGHT);
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  render();
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},REQ_NOTIFICATION);
 }
 private GradientDrawable shape(int color,int radius){
  GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(radius));return bg;
 }
 private TextView label(String s,int size,int color,boolean bold){
  TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);
  if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;
 }
 private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
 private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private void space(LinearLayout parent,int height){View v=new View(this);parent.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
 private LinearLayout card(){
  LinearLayout l=vertical();l.setPadding(dp(18),dp(18),dp(18),dp(18));l.setBackground(shape(Color.WHITE,22));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(16));root.addView(l,p);return l;
 }
 private Button action(String title,LinearLayout parent,Runnable r,boolean filled){
  Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextSize(13);
  b.setTextColor(filled?Color.WHITE:GREEN);b.setBackgroundTintList(ColorStateList.valueOf(filled?GREEN:0xFFEAF2EC));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));p.setMargins(0,dp(7),0,0);parent.addView(b,p);b.setOnClickListener(v->r.run());return b;
 }
 private void render(){
  ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(LIGHT);scroll.setFillViewport(true);
  root=vertical();root.setPadding(dp(18),dp(22),dp(18),dp(28));scroll.addView(root);setContentView(scroll);
  LinearLayout heading=horizontal();
  LinearLayout titles=vertical();titles.addView(label("MY AZAN",13,GOLD,true));space(titles,4);
  titles.addView(label("Assalamu Alaikum",26,GREEN,true));
  heading.addView(titles,new LinearLayout.LayoutParams(0,-2,1));
  Button settings=new Button(this);settings.setText("⚙");settings.setTextSize(23);settings.setAllCaps(false);
  settings.setTextColor(GREEN);settings.setBackgroundTintList(ColorStateList.valueOf(0xFFE1ECE5));
  heading.addView(settings,new LinearLayout.LayoutParams(dp(55),dp(54)));
  settings.setOnClickListener(v->showLocationSettings());root.addView(heading);space(root,8);
  root.addView(label(new SimpleDateFormat("EEEE, dd MMMM yyyy",Locale.getDefault()).format(new Date())+"  •  Your prayer companion",13,MUTED,false));space(root,20);
  Map<String,Long> today=Prefs.today(this);
  long now=System.currentTimeMillis();String nextName=null;Long nextAt=null;
  for(String name:PrayerTimes.NAMES){Long at=today.get(name);if(at!=null&&at>now&&(nextAt==null||at<nextAt)){nextName=name;nextAt=at;}}
  if(nextAt==null){
   Calendar tomorrow=Calendar.getInstance();tomorrow.add(Calendar.DATE,1);
   Map<String,Long> nextDay=Prefs.times(this,tomorrow.getTime());
   for(String name:PrayerTimes.NAMES){Long at=nextDay.get(name);if(at!=null&&(nextAt==null||at<nextAt)){nextAt=at;nextName=name;}}
  }
  LinearLayout hero=vertical();hero.setPadding(dp(23),dp(22),dp(23),dp(24));
  GradientDrawable heroBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{GREEN,DARK});
  heroBg.setCornerRadius(dp(24));hero.setBackground(heroBg);root.addView(hero);
  hero.addView(label("☪   NEXT PRAYER",12,0xFFD4DCCF,true));space(hero,10);
  hero.addView(label(nextName==null?"Prayer timings":nextName,31,Color.WHITE,true));
  space(hero,5);hero.addView(label(nextAt==null?"Unavailable":timeFormat.format(new Date(nextAt)),24,GOLD,true));space(hero,14);
  String remaining=nextAt==null?"Review your location and calculation settings":timeUntil(nextAt-now)+" remaining";
  hero.addView(label(remaining,14,0xFFE3EFE8,false));
  space(root,22);
  LinearLayout sectionHeader=horizontal();sectionHeader.addView(label("Today's Prayers",20,GREEN,true),new LinearLayout.LayoutParams(0,-2,1));
  TextView caption=label("Tap time to edit",12,MUTED,false);sectionHeader.addView(caption);root.addView(sectionHeader);space(root,12);
  LinearLayout prayers=card();prayers.setPadding(dp(12),dp(7),dp(12),dp(7));
  int idx=0;
  for(String prayer:PrayerTimes.NAMES){
   final String name=prayer;Long at=today.get(prayer);
   LinearLayout line=horizontal();line.setPadding(dp(7),dp(11),dp(7),dp(11));
   TextView symbol=label(ICONS[idx],21,GREEN,true);symbol.setGravity(Gravity.CENTER);
   symbol.setBackground(shape(0xFFF0F5EF,16));line.addView(symbol,new LinearLayout.LayoutParams(dp(46),dp(46)));
   LinearLayout details=vertical();details.setPadding(dp(13),0,0,0);details.addView(label(prayer,16,GREEN,true));
   String mode=Prefs.fixedTime(this,prayer).isEmpty()?(Prefs.offset(this,prayer)==0?"Calculated":(Prefs.offset(this,prayer)>0?"+":"")+Prefs.offset(this,prayer)+" min adjustment"):"Fixed daily time";
   details.addView(label(mode,11,MUTED,false));line.addView(details,new LinearLayout.LayoutParams(0,-2,1));
   TextView time=label(at==null?"--:--":timeFormat.format(new Date(at)),14,GREEN,true);
   time.setPadding(dp(6),dp(8),dp(6),dp(8));line.addView(time);time.setOnClickListener(v->editPrayer(name));
   details.setOnClickListener(v->editPrayer(name));
   Switch toggle=new Switch(this);toggle.setShowText(false);toggle.setChecked(Prefs.enabled(this,prayer));
   toggle.setThumbTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{GREEN,0xFFCFD7D1}));
   toggle.setContentDescription("Enable "+prayer+" Azan");
   toggle.setOnCheckedChangeListener((v,enabled)->{
    Prefs.get(this).edit().putBoolean("enabled_"+name,enabled).apply();AzanScheduler.schedule(this);
   });line.addView(toggle);prayers.addView(line);
   if(idx<4){View divider=new View(this);divider.setBackgroundColor(LINE);prayers.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
   idx++;
  }
  root.addView(label("Adjust each prayer time to match your local mosque. Fixed times repeat daily.",12,MUTED,false));space(root,18);
  LinearLayout voice=card();voice.addView(label("My Azan Voice",20,GREEN,true));space(voice,8);
  audioStatus=label(Prefs.audioPath(this).isEmpty()?"No audio selected yet":"✓  Your Azan audio is ready",13,MUTED,false);voice.addView(audioStatus);
  recordButton=action("●  Record my Azan",voice,()->toggleRecording(),true);
  action("⇧  Import MP3 or M4A",voice,()->{
   Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("audio/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,REQ_IMPORT);
  },false);
  action("▶  Test Azan",voice,()->playTest(),false);
  action("■  Stop Playback",voice,()->startService(new Intent(this,AzanPlaybackService.class).setAction(AzanPlaybackService.ACTION_STOP)),false);
  LinearLayout status=card();status.addView(label("Prayer Settings",19,GREEN,true));space(status,5);
  status.addView(label(String.format(Locale.US,"Location: %.3f, %.3f",Prefs.latitude(this),Prefs.longitude(this))+"  •  "+Prefs.method(this),13,MUTED,false));space(status,5);
  status.addView(label(AzanScheduler.exactAllowed(this)?"✓ Exact alarms enabled":"⚠ Exact alarm permission needed",13,AzanScheduler.exactAllowed(this)?GREEN:0xFF9C6320,true));
  action("Location & calculation method",status,()->showLocationSettings(),false);
  action("Alarm permissions",status,()->openAlarmPermission(),false);
  root.addView(label("Calculated times are estimates. Check against your local mosque, especially at high latitudes.",11,MUTED,false));
 }
 private String timeUntil(long ms){long m=Math.max(0,ms/60000);return m>=60?(m/60)+"h "+(m%60)+"m":m+" min";}
 private void editPrayer(String name){
  LinearLayout layout=vertical();layout.setPadding(dp(20),dp(8),dp(20),0);
  layout.addView(label("Choose timing mode",14,MUTED,false));
  RadioGroup modes=new RadioGroup(this);modes.setOrientation(RadioGroup.VERTICAL);
  String[] labels={"Calculated automatically","Adjust calculated time (minutes)","Use fixed daily time"};
  for(int i=0;i<3;i++){RadioButton r=new RadioButton(this);r.setId(500+i);r.setText(labels[i]);r.setTextSize(14);modes.addView(r);}
  String fixed=Prefs.fixedTime(this,name);int offset=Prefs.offset(this,name);
  int selected=!fixed.isEmpty()?2:offset!=0?1:0;modes.check(500+selected);layout.addView(modes);
  EditText minutes=new EditText(this);minutes.setHint("Minutes, e.g. -5 or 10");minutes.setSingleLine(true);
  minutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
  minutes.setText(String.valueOf(offset));layout.addView(minutes);
  TextView fixedPreview=label("Fixed time: "+(fixed.isEmpty()?"Not set":fixed),15,GREEN,true);layout.addView(fixedPreview);
  final int[] hm={5,0};
  if(!fixed.isEmpty()){try{String[] parts=fixed.split(":");hm[0]=Integer.parseInt(parts[0]);hm[1]=Integer.parseInt(parts[1]);}catch(Exception ignored){}}
  else {Long at=Prefs.today(this).get(name);if(at!=null){Calendar c=Calendar.getInstance();c.setTimeInMillis(at);hm[0]=c.get(Calendar.HOUR_OF_DAY);hm[1]=c.get(Calendar.MINUTE);}}
  Button choose=new Button(this);choose.setText("Choose fixed time");layout.addView(choose);
  choose.setOnClickListener(v->new TimePickerDialog(this,(picker,h,m)->{
   hm[0]=h;hm[1]=m;fixedPreview.setText(String.format(Locale.US,"Fixed time: %02d:%02d",h,m));modes.check(502);
  },hm[0],hm[1],false).show());
  Runnable state=()->{minutes.setVisibility(modes.getCheckedRadioButtonId()==501?View.VISIBLE:View.GONE);
   fixedPreview.setVisibility(modes.getCheckedRadioButtonId()==502?View.VISIBLE:View.GONE);
   choose.setVisibility(modes.getCheckedRadioButtonId()==502?View.VISIBLE:View.GONE);};
  modes.setOnCheckedChangeListener((g,id)->state.run());state.run();
  new AlertDialog.Builder(this).setTitle(name+" timing").setView(layout)
   .setNegativeButton("Cancel",null).setNeutralButton("Reset",(d,w)->{
    Prefs.get(this).edit().remove("fixed_"+name).putInt("offset_"+name,0).apply();
    AzanScheduler.schedule(this);render();
   }).setPositiveButton("Save",(d,w)->{
    int mode=modes.getCheckedRadioButtonId();
    if(mode==500)Prefs.get(this).edit().remove("fixed_"+name).putInt("offset_"+name,0).apply();
    else if(mode==501){
     int amount=0;try{amount=Integer.parseInt(minutes.getText().toString().trim());}catch(Exception ignored){}
     amount=Math.max(-180,Math.min(180,amount));
     Prefs.get(this).edit().remove("fixed_"+name).putInt("offset_"+name,amount).apply();
    }else Prefs.get(this).edit().putString("fixed_"+name,String.format(Locale.US,"%02d:%02d",hm[0],hm[1])).putInt("offset_"+name,0).apply();
    AzanScheduler.schedule(this);render();toast(name+" timing saved");
   }).show();
 }
 private EditText input(String title,String value,LinearLayout parent){
  parent.addView(label(title,13,MUTED,false));
  EditText e=new EditText(this);e.setSingleLine(true);e.setText(value);parent.addView(e);return e;
 }
 private void showLocationSettings(){
  LinearLayout panel=vertical();panel.setPadding(dp(20),dp(5),dp(20),0);
  EditText lat=input("Latitude",String.format(Locale.US,"%.5f",Prefs.latitude(this)),panel);
  EditText lon=input("Longitude",String.format(Locale.US,"%.5f",Prefs.longitude(this)),panel);
  lat.setInputType(8194);lon.setInputType(8194);
  Button gps=new Button(this);gps.setText("Use recent phone location");panel.addView(gps);
  gps.setOnClickListener(v->{
   if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);return;}
   try{
    LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location best=null;
    for(String provider:lm.getProviders(true)){Location l=lm.getLastKnownLocation(provider);if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}
    if(best!=null){lat.setText(String.valueOf(best.getLatitude()));lon.setText(String.valueOf(best.getLongitude()));}
    else toast("No recent location. Enter coordinates manually.");
   }catch(SecurityException e){toast("Location permission required");}
  });
  panel.addView(label("Calculation method",13,MUTED,false));
  Spinner method=new Spinner(this);method.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,METHODS));
  int selected=Arrays.asList(METHODS).indexOf(Prefs.method(this));method.setSelection(Math.max(0,selected));panel.addView(method);
  panel.addView(label("Asr school",13,MUTED,false));
  Spinner school=new Spinner(this);school.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Hanafi","Standard"}));
  school.setSelection(Prefs.hanafi(this)?0:1);panel.addView(school);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Prayer Settings").setView(panel).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
  dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(w->{
   try{
    double x=Double.parseDouble(lat.getText().toString()),y=Double.parseDouble(lon.getText().toString());
    if(x < -90||x>90||y < -180||y>180){toast("Invalid location coordinates");return;}
    Prefs.setCoordinates(this,x,y);
    Prefs.get(this).edit().putString("method",METHODS[method.getSelectedItemPosition()]).putBoolean("hanafi",school.getSelectedItemPosition()==0).apply();
    AzanScheduler.schedule(this);dialog.dismiss();render();toast("Prayer times updated");
   }catch(Exception e){toast("Enter valid latitude and longitude");}
  }));dialog.show();
 }
 private void openAlarmPermission(){
  if(Build.VERSION.SDK_INT>=31&&!AzanScheduler.exactAllowed(this)){
   try{startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));}
   catch(Exception e){toast("Open Settings → Apps → My Azan → Alarms");}
  }else toast("Exact alarms are already available");
 }
 private void playTest(){
  if(Prefs.audioPath(this).isEmpty()){toast("Record or import your Azan first");return;}
  Intent i=new Intent(this,AzanPlaybackService.class).setAction(AzanPlaybackService.ACTION_PLAY).putExtra("prayer","Preview");
  try{if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}
  catch(Exception e){toast("Playback unavailable: "+e.getMessage());}
 }
 private void toggleRecording(){
  if(recording){
   try{recorder.stop();recorder.release();recorder=null;recording=false;
    Prefs.get(this).edit().putString("audio_path",recordingFile.getAbsolutePath()).apply();audioStatus.setText("✓  Recording saved");
    recordButton.setText("●  Record my Azan");toast("Azan recording saved");
   }catch(Exception e){if(recorder!=null){recorder.release();recorder=null;}recording=false;recordButton.setText("●  Record my Azan");toast("Recording was too short or failed");}return;
  }
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
   requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_AUDIO);return;
  }
  try{
   recordingFile=new File(getFilesDir(),"my_azan_recorded.m4a");recorder=new MediaRecorder();
   recorder.setAudioSource(MediaRecorder.AudioSource.MIC);recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
   recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);recorder.setOutputFile(recordingFile.getAbsolutePath());
   recorder.prepare();recorder.start();recording=true;audioStatus.setText("● Recording your Azan…");recordButton.setText("■  Stop recording");
  }catch(Exception e){if(recorder!=null){recorder.release();recorder=null;}recording=false;toast("Microphone error: "+e.getMessage());}
 }
 @Override protected void onActivityResult(int req,int res,Intent data){
  super.onActivityResult(req,res,data);
  if(req==REQ_IMPORT&&res==RESULT_OK&&data!=null&&data.getData()!=null){
   try(InputStream in=getContentResolver().openInputStream(data.getData())){
    File out=new File(getFilesDir(),"my_azan_imported_audio");
    try(OutputStream os=new FileOutputStream(out)){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)os.write(buffer,0,n);}
    Prefs.get(this).edit().putString("audio_path",out.getAbsolutePath()).apply();toast("Azan audio imported");render();
   }catch(Exception e){toast("Audio import failed: "+e.getMessage());}
  }
 }
 @Override public void onRequestPermissionsResult(int request,String[] perms,int[] grants){
  super.onRequestPermissionsResult(request,perms,grants);
  if(grants.length>0&&grants[0]==PackageManager.PERMISSION_GRANTED){
   if(request==REQ_AUDIO)toast("Microphone allowed. Tap Record again.");
   if(request==REQ_LOCATION)toast("Location allowed. Open settings and tap phone location again.");
  }
 }
 @Override protected void onResume(){super.onResume();AzanScheduler.schedule(this);}
 @Override protected void onDestroy(){if(recorder!=null){try{recorder.stop();}catch(Exception ignored){}recorder.release();recorder=null;}super.onDestroy();}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}