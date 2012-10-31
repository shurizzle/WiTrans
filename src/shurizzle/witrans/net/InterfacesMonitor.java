package shurizzle.witrans.net;

import java.net.NetworkInterface;
import java.net.NetworkInterface;
import java.net.InterfaceAddress;
import java.util.HashSet;
import java.util.Enumeration;
import java.lang.Thread;
import android.content.Intent;
import android.content.Context;
import android.app.Service;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import shurizzle.witrans.net.Network;
import shurizzle.witrans.net.Helpers;
import shurizzle.witrans.utils.SystemProperty;

public class InterfacesMonitor extends Service
{
  private static InterfacesMonitor instance = null;

  public static boolean isInstanceCreated()
  {
    return instance != null;
  }

  private ApMonitorThread monitorThread;
  private Network network;
  private BroadcastReceiver wifiReceiver;

  private class WiFiReceiver extends BroadcastReceiver
  {
    private InterfaceAddress oIfAddr;
    private boolean isUp;

    WiFiReceiver()
    {
      super();
      oIfAddr = network.getWiFiInterfaceAddress();
      isUp = network.isWiFiConnected();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
      NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
      if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
        updateState();
    }

    private void down()
    {
      if (isUp)
        interfaceDown("WIFI");
      isUp = false;
    }

    private void up()
    {
      if (!isUp)
        interfaceUp("WIFI");
      isUp = true;
      oIfAddr = network.getWiFiInterfaceAddress();
    }

    private void update()
    {
      if (isUp) {
        interfaceUpdate("WIFI");
        oIfAddr = network.getWiFiInterfaceAddress();
      }
    }

    private void updateState()
    {
      if (isUp) {
        if (network.isWiFiConnected()) {
          if (oIfAddr == null || !network.getWiFiInterfaceAddress().equals(oIfAddr))
            update();
        } else {
          down();
        }
      } else {
        if (network.isWiFiConnected()) {
          up();
        }
      }
    }
  };

  private class ApMonitorThread extends Thread
  {
    InterfaceAddress oIfAddr;
    boolean isUp;
    boolean exit;

    public ApMonitorThread()
    {
      super();
      exit = false;
      isUp = network.isApConnected();
      oIfAddr = network.getApInterfaceAddress();
    }

    private void down()
    {
      if (isUp)
        interfaceDown("AP");
      isUp = false;
      oIfAddr = null;
    }

    private void up(InterfaceAddress ifaddr)
    {
      if (!isUp)
        interfaceUp("AP");
      isUp = true;
      oIfAddr = ifaddr;
    }

    private void update(InterfaceAddress ifaddr)
    {
      if (isUp) {
        interfaceUpdate("AP");
        oIfAddr = ifaddr;
      }
    }

    @Override
    public void run()
    {
      while (!isExiting()) {
        try {
          NetworkInterface iface = network.getApInterface();
          if (iface == null) {
            down();
          } else {
            if (iface.isUp()) {
              InterfaceAddress ifaddr = Helpers.getInterfaceAddress(iface);
              if (ifaddr != null) {
                if (isUp) {
                  if (oIfAddr == null || !ifaddr.equals(oIfAddr))
                    update(ifaddr);
                } else {
                  up(ifaddr);
                }
              } else {
                down();
              }
            } else {
              down();
            }
          }
        } catch (Exception e) {}
        try {
          Thread.sleep(500);
        } catch (Exception e) {}
      }
    }

    public synchronized void exit()
    {
      exit = true;
    }

    public synchronized boolean isExiting()
    {
      return exit;
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
    wifiReceiver = new WiFiReceiver();
  }

  @Override
  public void onStart(Intent intent, int startId)
  {
    super.onStart(intent, startId);

    registerReceiver(wifiReceiver,
        new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    monitorThread = new ApMonitorThread();
    monitorThread.start();
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();

    unregisterReceiver(wifiReceiver);
    monitorThread.exit();
    instance = null;
  }

  private void interfaceUp(String iface)
  {
    Intent i = new Intent("shurizzle.witrans.net.INTERFACE_UP");
    i.putExtra("interface", iface);
    sendBroadcast(i);
  }

  private void interfaceUpdate(String iface)
  {
    Intent i = new Intent("shurizzle.witrans.net.INTERFACE_UPDATE");
    i.putExtra("interface", iface);
    sendBroadcast(i);
  }

  private void interfaceDown(String iface)
  {
    Intent i = new Intent("shurizzle.witrans.net.INTERFACE_DOWN");
    i.putExtra("interface", iface);
    sendBroadcast(i);
  }
}
