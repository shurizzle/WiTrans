package shurizzle.witrans.net;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.DhcpInfo;
import android.net.ConnectivityManager;
import java.lang.reflect.Method;
import java.lang.Class;
import java.lang.Boolean;

import java.util.Enumeration;
import java.net.InterfaceAddress;
import java.net.SocketException;

import java.net.InetAddress;
import java.net.NetworkInterface;

import shurizzle.witrans.utils.SystemProperty;

public class Network
{
  private static Object[] emptyOArray = new Object[0];
  private Method IS_WIFI_AP_ENABLED = null;
  private Method GET_WIFI_AP_CONFIGURATION = null;

  private Context ctx = null;
  private WifiManager wifi = null;

  public Network(Context context)
  {
    ctx = context;

    wifi = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

    Class[] paramTypes = new Class[0];
    try {
      IS_WIFI_AP_ENABLED = wifi.getClass().getMethod("isWifiApEnabled", paramTypes);
    } catch (Exception e) {}

    try {
      GET_WIFI_AP_CONFIGURATION = wifi.getClass().getMethod("getWifiApConfiguration", paramTypes);
    } catch (Exception e) {}
  }

  public boolean isApConnected()
  {
    boolean iwae = false;

    try {
      if (IS_WIFI_AP_ENABLED != null)
        iwae = Boolean.valueOf(IS_WIFI_AP_ENABLED.invoke(wifi, emptyOArray).toString());
    } catch (Exception e) {}

    return iwae;
  }

  public boolean isWiFiConnected()
  {
    return ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
  }

  public boolean isConnected()
  {
    return isApConnected() || isWiFiConnected();
  }

  public WifiConfiguration getApConfiguration()
  {
    WifiConfiguration conf = null;

    try {
      if (GET_WIFI_AP_CONFIGURATION != null)
        conf = (WifiConfiguration) GET_WIFI_AP_CONFIGURATION.invoke(wifi, emptyOArray);
    } catch (Exception e) {}

    return conf;
  }

  public NetworkInterface getWiFiInterface()
  {
    String ifname = SystemProperty.get("wifi.interface", null);

    try {
      return NetworkInterface.getByName(ifname);
    } catch (Exception e) {
      return null;
    }
  }

  public NetworkInterface getApInterface()
  {
    String ifname = SystemProperty.get("wifi.tethering.interface", null);

    try {
      return NetworkInterface.getByName(ifname);
    } catch (Exception e) {
      return null;
    }
  }

  public NetworkInterface getInterface()
  {
    if (isApConnected()) {
      return getApInterface();
    } else if (isWiFiConnected()) {
      return getWiFiInterface();
    } else {
      return null;
    }
  }

  public InterfaceAddress getInterfaceAddress()
  {
    NetworkInterface iface = getInterface();
    if (iface == null)
      return null;

    return Helpers.getInterfaceAddress(iface);
  }

  public InetAddress getAddress()
  {
    InterfaceAddress ifAddr = getInterfaceAddress();
    if (ifAddr == null)
      return null;

    return ifAddr.getAddress();
  }

  public short getNetmask()
  {
    InterfaceAddress ifAddr = getInterfaceAddress();
    if (ifAddr == null)
      return -1;

    return ifAddr.getNetworkPrefixLength();
  }

  public Subnet getSubnet()
  {
    return Subnet.fromInetAddress(getAddress(), getNetmask());
  }
}
