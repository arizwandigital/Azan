package com.myazan.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
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

/** Simple native app without external services. */
public class MainActivity extends Activity {
 private final int GREEN=Color.rgb(15,65,54), GOLD=Color.rgb(229,190,115);
 private final int AUDIO_PERMISSION=10,LOCATION_PERMISSION=11,NOTIFICATION_PERMISSION=12,IMPORT_AUDIO=13;
 private LinearLayout root; private EditText latitude,longitude;
 private Spinner method,asr; private TextView recordingStatus;
 private MediaRecorder recorder; private File recordingFile; private boolean isRecording=false;
 private final String[] methods={"Karachi","MWL","ISNA","Egypt","Umm al-Qura"};
 @Override public void onCreate(Bundle b){
  super.onCreate(b);render();
  if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
   requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},NOTIFICATION_PERMISSION);
 }
 private TextView text(String msg,int size,boolean bold) {
  TextView v=new TextView(this);v.setText(msg);v.setTextColor(GREEN);v.setTextSize(size);
  if(bold)v.setTypeface(null,Typeface.BOLD);v.setPadding(0,8,0,8);return v;
 }
 private LinearLayout section(){
  LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(28,20,28,20);
  GradientDrawable bg=new GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(30);box.setBackground(bg);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,18);root.addView(box,p);return box;
 }
 private Button button(String label,LinearLayout dest,Runnable action){
  Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(Color.WHITE);
  b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(GREEN));dest.addView(b,new LinearLayout.LayoutParams(-1,-2));
  b.setOnClickListener(v->action.run());return b;
 }
 private EditText numeric(String label,double value,LinearLayout parent) {
  EditText e=new EditText(this);e.setText(String.format(Locale.US,"%.5f",value));
  e.setInputType(android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL|android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
  e.setSingleLine(true);e.setHint(label);parent.addView(text(label,13,false));parent.addView(e);return e;
 }
 private Spinner spinner(String[] values,int selected,LinearLayout parent){
  Spinner s=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values);
  s.setAdapter(adapter);s.setSelection(Math.max(0,selected));parent.addView(s);return s;
 }
 private void render(){
  ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(Color.rgb(244,247,242));
  root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(28,30,28,50);sc.addView(root);setContentView(sc);
  TextView header=text("MY AZAN",30,true);header.setTextColor(GREEN);root.addView(header);
  TextView tagline=text("Your voice • Your prayer times",16,false);root.addView(tagline);
  LinearLayout prayers=section();prayers.addView(text("Today's prayer times",22,true));
  Map<String,Long> times=Prefs.today(this);
  SimpleDateFormat fmt=new SimpleDateFormat("hh:mm a",Locale.getDefault());
  for(String name:PrayerTimes.NAMES){
   LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);line.setOrientation(LinearLayout.HORIZONTAL);
   Long when=times.get(name);
   TextView label=text(name+"    "+(when==null?"Unavailable":fmt.format(new Date(when))),16,true);
   line.addView(label,new LinearLayout.LayoutParams(0,-2,1));
   CheckBox toggle=new CheckBox(this);toggle.setChecked(Prefs.enabled(this,name));toggle.setContentDescription("Enable "+name+" Azan");
   toggle.setOnCheckedChangeListener((v,enabled)->{Prefs.get(this).edit().putBoolean("enabled_"+name,enabled).apply();AzanScheduler.schedule(this);});
   line.addView(toggle);prayers.addView(line);
  }
  LinearLayout voice=section();voice.addView(text("My Azan recording",21,true));
  recordingStatus=text(Prefs.audioPath(this).isEmpty()?"No recording selected": "Azan audio ready",14,false);voice.addView(recordingStatus);
  Button record=button("Record my Azan",voice,()->toggleRecording());
  record.setOnClickListener(v->{toggleRecording();record.setText(isRecording?"Stop recording":"Record my Azan");});
  button("Import existing MP3 / M4A",voice,()->{
   Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("audio/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMPORT_AUDIO);
  });
  button("Test Azan playback",voice,()->{
   Intent i=new Intent(this,AzanPlaybackService.class).setAction(AzanPlaybackService.ACTION_PLAY).putExtra("prayer","Test");
   try{if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);}catch(Exception e){toast("Cannot start playback: "+e.getMessage());}
  });
  button("Stop Azan",voice,()->startService(new Intent(this,AzanPlaybackService.class).setAction(AzanPlaybackService.ACTION_STOP)));
  LinearLayout location=section();location.addView(text("Prayer time settings",21,true));
  location.addView(text("Default: Lucknow, India. Set your own location for accurate times.",13,false));
  latitude=numeric("Latitude",Prefs.latitude(this),location);longitude=numeric("Longitude",Prefs.longitude(this),location);
  button("Use phone location",location,()->{
   if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
     requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION);
   else useLocation();
  });
  location.addView(text("Calculation method",14,true));
  method=spinner(methods,Arrays.asList(methods).indexOf(Prefs.method(this)),location);
  location.addView(text("Asr school",14,true));
  asr=spinner(new String[]{"Hanafi","Standard"},Prefs.hanafi(this)?0:1,location);
  button("Save times and schedule Azan",location,()->save());
  LinearLayout permissions=section();permissions.addView(text("Alarm permissions",20,true));
  permissions.addView(text("For timely playback, allow Alarms & reminders and notifications. Some phones require disabling battery restrictions for this app.",13,false));
  button("Open exact alarm permission",permissions,()->{
   if(Build.VERSION.SDK_INT>=31){
    Intent i=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()));
    try{startActivity(i);}catch(Exception e){toast("Open Settings → Apps → My Azan → Alarms");}
   }else toast("No additional alarm permission needed");
  });
  root.addView(text("Prayer times are approximate. Compare with your mosque and local calculation method.",12,false));
 }
 private void save(){
  try{
   double lat=Double.parseDouble(latitude.getText().toString()),lon=Double.parseDouble(longitude.getText().toString());
   if(lat < -90||lat>90||lon < -180||lon>180){toast("Invalid coordinates");return;}
   Prefs.setCoordinates(this,lat,lon);
   Prefs.get(this).edit().putString("method",methods[method.getSelectedItemPosition()]).putBoolean("hanafi",asr.getSelectedItemPosition()==0).apply();
   AzanScheduler.schedule(this);toast("Prayer alarms scheduled");render();
  }catch(Exception e){toast("Please enter valid coordinates");}
 }
 private void toggleRecording(){
  if(isRecording){try{recorder.stop();recorder.release();recorder=null;isRecording=false;
   Prefs.get(this).edit().putString("audio_path",recordingFile.getAbsolutePath()).apply();
   recordingStatus.setText("Your Azan recording is saved");toast("Recording saved");
  }catch(Exception e){isRecording=false;if(recorder!=null){recorder.release();recorder=null;}toast("Recording failed");}return;}
  if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
   requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},AUDIO_PERMISSION);return;
  }
  try{
   recordingFile=new File(getFilesDir(),"my_azan_recorded.m4a");
   recorder=new MediaRecorder();
   recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
   recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
   recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
   recorder.setOutputFile(recordingFile.getAbsolutePath());
   recorder.prepare();recorder.start();isRecording=true;
   recordingStatus.setText("Recording… tap Stop recording when finished");
  }catch(Exception e){if(recorder!=null){recorder.release();recorder=null;}toast("Cannot record: "+e.getMessage());}
 }
 private void useLocation(){
  try{
   LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);
   Location best=null;
   for(String provider:lm.getProviders(true)){
    Location l=lm.getLastKnownLocation(provider);
    if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;
   }
   if(best!=null){latitude.setText(String.valueOf(best.getLatitude()));longitude.setText(String.valueOf(best.getLongitude()));toast("Location found. Tap Save times.");}
   else toast("No recent location. Enter coordinates manually.");
  }catch(SecurityException e){toast("Location permission required");}
 }
 @Override protected void onActivityResult(int req,int res,Intent data){
  super.onActivityResult(req,res,data);
  if(req==IMPORT_AUDIO&&res==RESULT_OK&&data!=null&&data.getData()!=null){
   try(InputStream in=getContentResolver().openInputStream(data.getData())){
    File out=new File(getFilesDir(),"my_azan_imported_audio");
    try(OutputStream os=new FileOutputStream(out)){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)os.write(buf,0,n);}
    Prefs.get(this).edit().putString("audio_path",out.getAbsolutePath()).apply();toast("Azan audio imported");render();
   }catch(Exception e){toast("Cannot import audio: "+e.getMessage());}
  }
 }
 @Override public void onRequestPermissionsResult(int req,String[] p,int[] granted){
  super.onRequestPermissionsResult(req,p,granted);
  if(granted.length>0&&granted[0]==PackageManager.PERMISSION_GRANTED){
   if(req==LOCATION_PERMISSION)useLocation();
   if(req==AUDIO_PERMISSION)toast("Microphone enabled. Tap Record again.");
  }
 }
 @Override protected void onResume(){super.onResume();AzanScheduler.schedule(this);}
 @Override protected void onDestroy(){if(recorder!=null){try{recorder.stop();}catch(Exception ignored){}recorder.release();recorder=null;}super.onDestroy();}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}