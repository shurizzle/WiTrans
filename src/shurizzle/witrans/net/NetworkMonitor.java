package shurizzle.witrans.net;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class NetworkMonitor extends Service
{
  private static NetworkMonitor instance = null;

  public static boolean isInstanceCreated()
  {
    return instance != null;
  }

  BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent)
    {
      Toast.makeText(NetworkMonitor.this, "Connectivity Changed", Toast.LENGTH_SHORT).show();
    }
  };

  Network network;

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  @Override
  public void onCreate()
  {
    super.onCreate();

    instance = this;
    network = new Network(this);

    Toast.makeText(this, "Service Created", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();

    instance = null;

    unregisterReceiver(mConnReceiver);

    Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
  }

  @Override
  public void onStart(Intent intent, int startId)
  {
    super.onStart(intent, startId);

    registerReceiver(mConnReceiver,
        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
  }
}
