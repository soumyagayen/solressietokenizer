//--------------------------------------------------------------------------------------------------------
// Heart.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

import java.util.*;

//--------------------------------------------------------------------------------------------------------
// Heart
//--------------------------------------------------------------------------------------------------------

public class Heart implements Constants {

//--------------------------------------------------------------------------------------------------------
// Heart constants
//--------------------------------------------------------------------------------------------------------

  // Heart beats every 5 secs.  Don't change
  // Note that heart beat is a fixed delay approach, not fixed frequency
  // Therefore, heart never trys to catch up after tasks that take a long time
  public static final long     kHeartBeatInterval=5*k1Sec;  
  
  // Thresholds to log warnings about slow heart tasks
  public static final long     kSlowHeartTime=3*k1Sec;  
  public static final long     kSlowHeartTaskTime=2*k1Sec;  
  
  // Steady beat - used by log monitor to show server is alive
  public static final long     kLogBeatInterval=35*k1Sec;  
  public static final long     kLogFullStatsInterval=5*k1Min;

//--------------------------------------------------------------------------------------------------------
// Heart class vars
//--------------------------------------------------------------------------------------------------------

  private static Timer              gTimer;
  private static long               gStartTime;

  private static ArrayList          gTasks;

//--------------------------------------------------------------------------------------------------------
// Heart class init
//--------------------------------------------------------------------------------------------------------
  
  static {
    try {
      gTasks=new ArrayList();
      gStartTime=System.currentTimeMillis();
      gTimer=new Timer("HeartBeat",true);
      gTimer.scheduleAtFixedRate(new TimerTask() { 
        public void run() { heartBeat(); }},kHeartBeatInterval,kHeartBeatInterval);
      
      // By default, log a heart beat and system info
    } catch (Throwable e) {
      System.err.println(FormatUtils.formatException("Cannot init Heart",e));
      throw e;
    }
  }
  
//--------------------------------------------------------------------------------------------------------
// get routines
//--------------------------------------------------------------------------------------------------------
  
  public static long getStartTime() { return gStartTime; }
  public static String getStartTimeStamp() { return FormatUtils.formatTime(gStartTime); }
  public static long getUpTime() { return (System.currentTimeMillis()-gStartTime); }
  public static String getUpTimeStamp() { return FormatUtils.formatDuration(getUpTime()); }

//--------------------------------------------------------------------------------------------------------
// addDelayedTask
//--------------------------------------------------------------------------------------------------------

  public static void addDelayedTask(long inDelay, final Runnable inTask) { 
    gTimer.schedule(new TimerTask() {
      public void run() { 
        try { 
          inTask.run(); 
        } catch (Exception e) { 

        } 
      }
    },inDelay);
  }

//--------------------------------------------------------------------------------------------------------
// addHeartTask
//--------------------------------------------------------------------------------------------------------

  public static void addHeartTask(HeartTask inTask, boolean inUnique) { 
    synchronized(gTasks) {
      if (inUnique) 
        removeHeartTask(inTask.getName());
      gTasks.add(inTask); 
    }
  }

//--------------------------------------------------------------------------------------------------------
// findHeartTask
//--------------------------------------------------------------------------------------------------------

  public static HeartTask findHeartTask(String inTaskName) { 
    synchronized(gTasks) {
      for (int i=0; i<gTasks.size(); i++) {
        HeartTask theTask=(HeartTask) gTasks.get(i);
        if (theTask.getName().equals(inTaskName))
          return theTask;
      }
      return null;
    }
  }

//--------------------------------------------------------------------------------------------------------
// removeHeartTask
//--------------------------------------------------------------------------------------------------------

  public static void removeHeartTask(HeartTask inTask) {     
    synchronized(gTasks) {
      gTasks.remove(inTask); 
    }
  }

  public static void removeHeartTask(String inTaskName) { 
    HeartTask theTask=findHeartTask(inTaskName);
    if (theTask!=null)
      removeHeartTask(theTask);
  }

//--------------------------------------------------------------------------------------------------------
// clearHeartTasks
//--------------------------------------------------------------------------------------------------------

  public static void clearHeartTasks() { 
    synchronized(gTasks) {
      gTasks.clear(); 
    }
  }

//--------------------------------------------------------------------------------------------------------
// heartBeat
//--------------------------------------------------------------------------------------------------------

  public static void heartBeat() {
    if (gTasks!=null)
      try {
        HeartTask[] theTasks;
        synchronized(gTasks) {
          theTasks=(HeartTask[]) gTasks.toArray(new HeartTask[gTasks.size()]);
        }
        for (int i=0; i<theTasks.length; i++) {
          HeartTask theTask=theTasks[i];
          if (theTask!=null) {
            boolean theOK;
            synchronized(gTasks) {
              theOK=(gTasks.indexOf(theTask)>=0);
            }
            if (theOK) {
              theTask.checkBeat(); 
            }
          }
        }
      } catch (Exception e) {

      }
  }

}


