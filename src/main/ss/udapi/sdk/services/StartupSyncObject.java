package ss.udapi.sdk.services;

public class StartupSyncObject
{
  private static StartupSyncObject syncObject = null;
  private StartupSyncObject()
  {}
  
  public static StartupSyncObject getSyncObject()
  {
    if (syncObject == null) {
      syncObject = new StartupSyncObject();
    }
    return syncObject;
  }

}
