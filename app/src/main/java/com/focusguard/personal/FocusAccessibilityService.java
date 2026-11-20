package com.focusguard.personal;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** On-device accessibility screen detection; never stores or sends screen content. */
public class FocusAccessibilityService extends AccessibilityService {
    private long lastCheck=0L, lastBlock=0L;
    private String lastBlockedPackage="";
    @Override public void onAccessibilityEvent(AccessibilityEvent event){
        if(event==null||!FocusPrefs.master(this))return;
        int type=event.getEventType();
        if(type!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED&&
           type!=AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED&&
           type!=AccessibilityEvent.TYPE_WINDOWS_CHANGED)return;
        CharSequence p=event.getPackageName();
        if(p==null)return;
        String pkg=p.toString();
        if(pkg.equals(getPackageName())||pkg.startsWith("com.android.")||
           pkg.equals("android")||pkg.startsWith("com.google.android.permissioncontroller"))return;
        long now=SystemClock.elapsedRealtime();
        // Whole app blocking is checked independently of content scanning.
        if(FocusPrefs.blockedApp(this,pkg)){
            if(now-lastBlock>1500||!pkg.equals(lastBlockedPackage))block(pkg,now);
            return;
        }
        String platform=platform(pkg);
        if(platform==null||!FocusPrefs.blockedReel(this,platform))return;
        if(now-lastCheck<600||now-lastBlock<1200)return;
        lastCheck=now;
        AccessibilityNodeInfo root=getRootInActiveWindow();
        if(root==null)return;
        try{
            if(isShortVideoScreen(root,platform))block(pkg,now);
        }finally{root.recycle();}
    }
    private String platform(String pkg){
        if(pkg.equals("com.instagram.android"))return "instagram";
        if(pkg.equals("com.facebook.katana"))return "facebook";
        if(pkg.equals("com.google.android.youtube"))return "youtube";
        return null;
    }
    private void block(String pkg,long now){
        lastBlock=now;lastBlockedPackage=pkg;
        boolean navigated=performGlobalAction(GLOBAL_ACTION_HOME);
        if(navigated)Toast.makeText(this,"FocusGuard: blocked during your scheduled time",Toast.LENGTH_SHORT).show();
    }
    /**
     * Experimental heuristics: navigation tabs alone MUST NOT trigger a block.
     * Requires stronger player-specific marker/interaction combinations.
     */
    private boolean isShortVideoScreen(AccessibilityNodeInfo root,String platform){
        Set<String> tokens=new HashSet<>();
        ArrayDeque<AccessibilityNodeInfo> queue=new ArrayDeque<>();
        queue.add(root);
        int visited=0;
        while(!queue.isEmpty()&&visited++<180){
            AccessibilityNodeInfo node=queue.poll();
            try {
                add(tokens,node.getText());
                add(tokens,node.getContentDescription());
                String id=node.getViewIdResourceName();
                if(id!=null)tokens.add("id:"+id.toLowerCase(Locale.ROOT));
                for(int i=0;i<node.getChildCount();i++){
                    AccessibilityNodeInfo child=node.getChild(i);
                    if(child!=null)queue.add(child);
                }
            } finally {
                if(node!=root)node.recycle();
            }
        }
        boolean playerId=containsId(tokens,"reel")||containsId(tokens,"shorts");
        boolean audio=has(tokens,"original audio")||has(tokens,"use audio")||
             has(tokens,"audio page")||has(tokens,"sound page")||has(tokens,"audio by");
        boolean remix=has(tokens,"remix")||has(tokens,"create reel");
        boolean videoActions=has(tokens,"like")&&has(tokens,"comment")&&has(tokens,"share");
        if(platform.equals("youtube")){
            boolean shorts=has(tokens,"shorts")||containsId(tokens,"reel");
            return (playerId&&videoActions)||(shorts&&has(tokens,"remix")&&has(tokens,"dislike"));
        }
        if(platform.equals("instagram")){
            return (playerId&&(audio||remix||videoActions))||
                   (has(tokens,"reels")&&audio&&videoActions);
        }
        return (playerId&&(audio||remix||videoActions))||
               (has(tokens,"reels")&&remix&&videoActions);
    }
    private void add(Set<String> set,CharSequence v){
        if(v!=null&&v.length()>0&&v.length()<150)set.add(v.toString().toLowerCase(Locale.ROOT));
    }
    private boolean has(Set<String> set,String token){
        for(String s:set)if(!s.startsWith("id:")&&(s.equals(token)||s.contains(token)))return true;
        return false;
    }
    private boolean containsId(Set<String> set,String token){
        for(String s:set)if(s.startsWith("id:")&&s.contains(token))return true;
        return false;
    }
    @Override public void onInterrupt(){}
}