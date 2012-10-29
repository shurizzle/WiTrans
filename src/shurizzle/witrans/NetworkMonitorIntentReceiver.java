package shurizzle.witrans;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkMonitorIntentReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive(Context context, Intent intent)
  {
    Intent serviceIntent = new Intent();
    serviceIntent.setAction("shurizzle.witrans.net.NetworkMonitor");
    context.startService(serviceIntent);
  }
}
