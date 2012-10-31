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

    if (hasWiFi()) {
      Class[] paramTypes = new Class[0];
      try {
        IS_WIFI_AP_ENABLED = wifi.getClass().getMethod("isWifiApEnabled", paramTypes);
      } catch (Exception e) {}

      try {
        GET_WIFI_AP_CONFIGURATION = wifi.getClass().getMethod("getWifiApConfiguration", paramTypes);
      } catch (Exception e) {}
    }
  }

  public boolean hasWiFi()
  {
    return wifi != null;
  }

  public boolean isApConnected()
  {
    if (!hasWiFi())
      return false;

    boolean iwae = false;

    try {
      if (IS_WIFI_AP_ENABLED != null)
        iwae = Boolean.valueOf(IS_WIFI_AP_ENABLED.invoke(wifi, emptyOArray).toString());
    } catch (Exception e) {}

    return iwae;
  }

  public boolean isWiFiConnected()
  {
    if (!hasWiFi())
      return false;
    return ((ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
  }

  public boolean isConnected()
  {
    return isApConnected() || isWiFiConnected();
  }

  public WifiConfiguration getApConfiguration()
  {
    if (!hasWiFi())
      return null;

    WifiConfiguration conf = null;

    try {
      if (GET_WIFI_AP_CONFIGURATION != null)
        conf = (WifiConfiguration) GET_WIFI_AP_CONFIGURATION.invoke(wifi, emptyOArray);
    } catch (Exception e) {}

    return conf;
  }

  public NetworkInterface getWiFiInterface()
  {
    if (!hasWiFi())
      return null;

    String ifname = SystemProperty.get("wifi.interface", null);

    try {
      return NetworkInterface.getByName(ifname);
    } catch (Exception e) {
      return null;
    }
  }

  public NetworkInterface getApInterface()
  {
    if (!hasWiFi())
      return null;

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

  public InterfaceAddress getInterfaceAddress(String ifname)
  {
    try {
      return getInterfaceAddress(NetworkInterface.getByName(ifname));
    } catch (Exception e) {
      return null;
    }
  }

  public InterfaceAddress getInterfaceAddress(NetworkInterface iface)
  {
    if (iface == null)
      return null;

    return Helpers.getInterfaceAddress(iface);
  }

  public InterfaceAddress getInterfaceAddress()
  {
    return getInterfaceAddress(getInterface());
  }

  public InterfaceAddress getApInterfaceAddress()
  {
    return getInterfaceAddress(getApInterface());
  }

  public InterfaceAddress getWiFiInterfaceAddress()
  {
    return getInterfaceAddress(getWiFiInterface());
  }

  public InetAddress getAddress(String ifname)
  {
    return getAddress(getInterfaceAddress(ifname));
  }

  public InetAddress getAddress(NetworkInterface iface)
  {
    return getAddress(getInterfaceAddress(iface));
  }

  public InetAddress getAddress(InterfaceAddress ifaddr)
  {
    if (ifaddr == null)
      return null;

    return ifaddr.getAddress();
  }

  public InetAddress getAddress()
  {
    return getAddress(getInterfaceAddress());
  }

  public InetAddress getApAddress()
  {
    return getAddress(getApInterface());
  }

  public InetAddress getWiFiAddress()
  {
    return getAddress(getWiFiInterface());
  }

  public short getNetmask(String ifname)
  {
    return getNetmask(getInterfaceAddress(ifname));
  }

  public short getNetmask(NetworkInterface iface)
  {
    return getNetmask(getInterfaceAddress(iface));
  }

  public short getNetmask(InterfaceAddress ifaddr)
  {
    if (ifaddr == null)
      return -1;

    return ifaddr.getNetworkPrefixLength();
  }

  public short getNetmask()
  {
    return getNetmask(getInterfaceAddress());
  }

  public short getApNetmask()
  {
    return getNetmask(getApInterface());
  }

  public short getWiFiNetmask()
  {
    return getNetmask(getWiFiInterface());
  }

  public Subnet getSubnet(String ifname)
  {
    return getSubnet(getInterfaceAddress(ifname));
  }

  public Subnet getSubnet(NetworkInterface iface)
  {
    return getSubnet(getInterfaceAddress(iface));
  }

  public Subnet getSubnet(InterfaceAddress ifaddr)
  {
    return Subnet.fromInetAddress(getAddress(ifaddr), getNetmask(ifaddr));
  }

  public Subnet getSubnet()
  {
    return getSubnet(getInterfaceAddress());
  }

  public Subnet getApSubnet()
  {
    return getSubnet(getApInterface());
  }

  public Subnet getWiFiSubnet()
  {
    return getSubnet(getWiFiInterface());
  }
}
