//--------------------------------------------------------------------------------------------------------
// HeartTask.java
//--------------------------------------------------------------------------------------------------------

package gravel.utils;

//--------------------------------------------------------------------------------------------------------
// HeartTask
//--------------------------------------------------------------------------------------------------------

public abstract class HeartTask implements Constants {

//--------------------------------------------------------------------------------------------------------
// HeartTask member vars
//--------------------------------------------------------------------------------------------------------

  private String    mName;
  private long      mInterval;
  private boolean   mSteady;
  private long      mLastBeatTime;

//--------------------------------------------------------------------------------------------------------
// HeartTask
//--------------------------------------------------------------------------------------------------------

  public HeartTask(String inName, long inInterval, boolean inSteady) {
    mName=inName;
    mInterval=inInterval;
    mSteady=inSteady;
    mLastBeatTime=System.currentTimeMillis()-RandomUtils.randomIndex(inInterval);
  }

//--------------------------------------------------------------------------------------------------------
// gets
//--------------------------------------------------------------------------------------------------------

  public String getName() { return mName; }
  public long getInterval() { return mInterval; }
  public boolean getSteady() { return mSteady; }
  public long getLastBeatTime() { return mLastBeatTime; }

//--------------------------------------------------------------------------------------------------------
// beat
//--------------------------------------------------------------------------------------------------------

  public abstract void beat();

//--------------------------------------------------------------------------------------------------------
// checkBeat
//
// Strategy where beats are skipped when app idle (few log msgs)
//--------------------------------------------------------------------------------------------------------

  void checkBeat() {
    long theTime=System.currentTimeMillis();
    boolean theDoBeat=(theTime-mLastBeatTime>=mInterval);
    if (theDoBeat) {
      beat();
      mLastBeatTime+=mInterval;
    }
  }
  
}