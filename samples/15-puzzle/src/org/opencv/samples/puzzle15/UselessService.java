package org.opencv.samples.puzzle15;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UselessService extends Service {
  
	 private Timer timer;
	   
	  private TimerTask updateTask = new TimerTask() {
	    @Override
	    public void run() {
	      Log.i("MOO", "Timer task doing work");
	    }
	  };
	
  @Override
  public void onCreate() {
    super.onCreate();
    Log.i("MOO", "Service creating");
    
    timer = new Timer("TweetCollectorTimer");
    timer.schedule(updateTask, 1000L, 60 * 1000L);
     
  }
 
  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.i("MOO", "Service destroying");
     
  }

@Override
public IBinder onBind(Intent intent) {
	// TODO Auto-generated method stub
	return null;
}
}