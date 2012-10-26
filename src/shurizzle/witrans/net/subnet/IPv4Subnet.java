package shurizzle.witrans.net.subnet;

import java.lang.UnsupportedOperationException;
import java.lang.IllegalStateException;
import java.util.NoSuchElementException;
import java.net.InetAddress;

import shurizzle.witrans.net.Subnet;

public class IPv4Subnet extends Subnet
{
  private int network;
  private int netmask;
  private int broadcast;

  public class Iterator implements java.util.Iterator<InetAddress>
  {
    private int current;

    public Iterator()
    {
      current = network + 1;
    }

    @Override
    public boolean hasNext()
    {
      return (current + 1) < broadcast;
    }

    @Override
    public InetAddress next()
      throws NoSuchElementException {
      if (!hasNext())
        throw new NoSuchElementException();
      return toInetAddress(current++);
    }

    @Override
    public void remove()
      throws UnsupportedOperationException, IllegalStateException {
      throw new UnsupportedOperationException();
    }
  }

  private IPv4Subnet(int address, short netmask)
  {
    this.netmask = 0;
    for (int i = 0; i < netmask; i++) {
      this.netmask |= (1 << 31 - i);
    }
    network = address & this.netmask;
    broadcast = address | ~(this.netmask);
  }

  public InetAddress getNetwork()
  {
    return toInetAddress(network);
  }

  public InetAddress getNetmask()
  {
    return toInetAddress(netmask);
  }

  public short getPrefix()
  {
    return (short) countBits(netmask);
  }

  public InetAddress getBroadcast()
  {
    return toInetAddress(broadcast);
  }

  public Iterator iterator()
  {
    return new Iterator();
  }

  @Override
  public String toString()
  {
    return String.format("%s/%d", getNetwork().getHostAddress(), countBits(netmask));
  }

  public static InetAddress toInetAddress(int x)
  {
    try {
      return InetAddress.getByAddress(toArray(x));
    } catch (Exception e) {
      return null;
    }
  }

  public static int countBits(int x)
  {
    int i, bits;

    for (i = 0, bits = (x & 1); i < 32; i++, x >>>= 1, bits += x & 1);

    return bits;
  }

  public static byte[] toArray(int x)
  {
    byte[] ret = new byte[4];
    for (int j = 3; j >= 0; --j)
        ret[j] |= ((x >>> 8 * (3 - j)) & (0xff));
    return ret;
  }

  private static int parseIp(String ip)
  {
    int addr = 0;
    String[] ps = ip.split("\\.");

    for (int i = 0; i < ps.length; i++) {
      addr = (addr << 8) + Integer.parseInt(ps[i]);
    }

    return addr;
  }

  public static int pop(int x)
  {
    x = x - ((x >>> 1) & 0x55555555);
    x = (x & 0x33333333) + ((x >>> 2) & 0x33333333);
    x = (x + (x >>> 4)) & 0x0F0F0F0F;
    x = x + (x >>> 8);
    x = x + (x >>> 16);
    return x & 0x0000003F;
  }

  public static short netmaskDotToShort(String mask)
  {
    return (short) pop(parseIp(mask));
  }

  public static IPv4Subnet fromIpNetmask(String addr, String netmask)
  {
    return fromIpNetmask(addr, netmaskDotToShort(netmask));
  }

  public static IPv4Subnet fromIpNetmask(String addr, short netmask)
  {
    if (netmask < 0 || netmask > 31)
      return null;
    if (!isIPv4(addr))
      return null;

    return new IPv4Subnet(parseIp(addr), netmask);
  }

  public static IPv4Subnet fromInetAddress(InetAddress addr, short netmask)
  {
    return fromIpNetmask(addr.getHostAddress(), netmask);
  }

  public static IPv4Subnet fromString(String subnet)
  {
    int slash = subnet.indexOf('/');

    if (slash == -1)
      return null;

    String ip = subnet.substring(0, slash);
    short nm = (short) Integer.parseInt(subnet.substring(slash + 1));

    return fromIpNetmask(ip, nm);
  }
}
