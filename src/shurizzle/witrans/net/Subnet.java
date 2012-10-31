package shurizzle.witrans.net;

import java.lang.Iterable;
import java.util.regex.Pattern;
import java.net.InetAddress;

import shurizzle.witrans.net.subnet.IPv4Subnet;
import shurizzle.witrans.net.subnet.IPv6Subnet;

public abstract class Subnet implements Iterable<InetAddress>
{
  private static Pattern ipv4Pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){0,3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

  abstract public InetAddress getNetwork();
  abstract public InetAddress getNetmask();
  abstract public short getPrefix();
  abstract public InetAddress getBroadcast();

  @Override
  public boolean equals(Object s)
  {
    if (s instanceof Subnet) {
      Subnet sub = (Subnet) s;
      return toString().equals(sub.toString());
    }
    return false;
  }

  public static boolean isIPv4(String addr)
  {
    return ipv4Pattern.matcher(addr).matches();
  }

  public static Subnet fromIpNetmask(String addr, String netmask)
  {
    if (isIPv4(addr))
      return IPv4Subnet.fromIpNetmask(addr, netmask);
    else
      return IPv6Subnet.fromIpNetmask(addr, netmask);
  }

  public static Subnet fromIpNetmask(String addr, short netmask)
  {
    if (isIPv4(addr))
      return IPv4Subnet.fromIpNetmask(addr, netmask);
    else
      return IPv4Subnet.fromIpNetmask(addr, netmask);
  }

  public static Subnet fromInetAddress(InetAddress addr, short netmask)
  {
    return fromIpNetmask(addr.getHostAddress(), netmask);
  }

  public static Subnet fromString(String subnet)
  {
    int slash = subnet.indexOf('/');

    if (slash == -1)
      return null;

    String ip = subnet.substring(0, slash);
    short nm = (short) Integer.parseInt(subnet.substring(slash + 1));

    return fromIpNetmask(ip, nm);
  }
}
