package shurizzle.witrans.net;

import java.util.Iterator;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.regex.Pattern;
import java.net.InterfaceAddress;
import java.lang.StringBuilder;

public class Helpers
{
  private static Pattern ipv4Pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){0,3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

  private Helpers() {}

  public static String MACBytesToString(byte[] mac)
  {
    if (mac == null)
      return null;

    StringBuilder sb = new StringBuilder(18);
    for (byte b : mac) {
      if (sb.length() > 0)
        sb.append(':');
      sb.append(String.format("%02x", b));
    }

    return sb.toString();
  }

  public static boolean isIpv4(InetAddress addr)
  {
    return ipv4Pattern.matcher(addr.getHostAddress().toString()).matches();
  }

  public static InterfaceAddress getInterfaceAddress(NetworkInterface intf)
  {
    try {
      InterfaceAddress res = null;
      for (Iterator<InterfaceAddress> iterIntAddr = intf.getInterfaceAddresses().iterator(); iterIntAddr.hasNext();) {
        InterfaceAddress intAddress = iterIntAddr.next();
        if (!intAddress.getAddress().isLoopbackAddress()) {
          if (isIpv4(intAddress.getAddress()))
            return intAddress;
          else
            res = intAddress;
        }
      }
      if (res != null)
        return res;
    } catch (Exception e) {}

    return null;
  }

  public static InetAddress getInetAddress(NetworkInterface intf)
  {
    InterfaceAddress intAddr = getInterfaceAddress(intf);
    if (intAddr == null)
      return null;
    return intAddr.getAddress();
  }
}
