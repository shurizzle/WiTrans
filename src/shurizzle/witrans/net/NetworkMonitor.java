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
      String action = intent.getAction();
      String ifname = (String) intent.getExtras().get("interface");
      if (action.equals("shurizzle.witrans.net.INTERFACE_UP")) {
        Toast.makeText(NetworkMonitor.this, ifname + " connected", Toast.LENGTH_SHORT).show();
      } else if (action.equals("shurizzle.witrans.net.INTERFACE_UPDATE")) {
        Toast.makeText(NetworkMonitor.this, ifname + " updated", Toast.LENGTH_SHORT).show();
      } else if (action.equals("shurizzle.witrans.net.INTERFACE_DOWN")) {
        Toast.makeText(NetworkMonitor.this, ifname + " disconnected", Toast.LENGTH_SHORT).show();
      }
    }
  };

  Network network;

  private class ArpReaderThread extends Thread
  {
    private boolean mStopped = false;

    @Override
    public void run()
    {
      mStopped = false;
    }

    public synchronized void exit()
    {
      mStopped = true;

      String iface = network.getInterface().getDisplayName();
    }

    public synchronized boolean isExiting()
    {
      return mStopped;
    }
  }

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
    IntentFilter filter = new IntentFilter();
    filter.addAction("shurizzle.witrans.net.INTERFACE_UP");
    filter.addAction("shurizzle.witrans.net.INTERFACE_UPDATE");
    filter.addAction("shurizzle.witrans.net.INTERFACE_DOWN");

    registerReceiver(mConnReceiver, filter);
        //new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
  }
}
