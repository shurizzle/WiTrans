package shurizzle.witrans.net.subnet;

import java.lang.UnsupportedOperationException;
import java.lang.IllegalStateException;
import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.nio.ByteOrder;

import shurizzle.witrans.net.Subnet;

public class IPv4Subnet extends Subnet
{
  private static final int[] idx = (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ? new int[]{0,1,2,3} : new int[]{3,2,1,0};
  private final byte[] network;
  private final InetAddress networkAddress;
  private final byte[] netmask;
  private final InetAddress netmaskAddress;
  private final short prefix;
  private final byte[] broadcast;
  private final InetAddress broadcastAddress;

  public class Iterator implements java.util.Iterator<InetAddress>
  {
    private byte[] current;
    private final byte[] max;

    public Iterator()
    {
      current = IPv4Subnet.next(network);
      max = prev(broadcast);
    }

    @Override
    public boolean hasNext()
    {
      return lt(current, max);
    }

    @Override
    public InetAddress next()
      throws NoSuchElementException {
      if (!hasNext())
        throw new NoSuchElementException();

      InetAddress res = toInetAddress(current);
      current = IPv4Subnet.next(current);
      return res;
    }

    @Override
    public void remove()
      throws UnsupportedOperationException, IllegalStateException {
      throw new UnsupportedOperationException();
    }
  }

  private IPv4Subnet(int address, short netmask)
  {
    int nm = netmaskShortToInteger(netmask);

    prefix = (short) countBits(nm);
    this.netmask = toArray(nm);
    netmaskAddress = toInetAddress(this.netmask);
    network = toArray(address & nm);
    networkAddress = toInetAddress(network);
    broadcast = toArray(address | ~(nm));
    broadcastAddress = toInetAddress(broadcast);
  }

  public InetAddress getNetwork()
  {
    return networkAddress;
  }

  public InetAddress getNetmask()
  {
    return netmaskAddress;
  }

  public short getPrefix()
  {
    return prefix;
  }

  public InetAddress getBroadcast()
  {
    return broadcastAddress;
  }

  public Iterator iterator()
  {
    return new Iterator();
  }

  @Override
  public String toString()
  {
    return String.format("%s/%d", getNetwork().getHostAddress(), (int) getPrefix());
  }

  public static byte[] next(byte[] addr)
  {
    int i = addr.length - 1;
    for (; i >= 0 && addr[i] == (byte) 0xFF; addr[i] = 0, i--);
    if (i >= 0)
      addr[i]++;

    return addr;
  }

  public static byte[] prev(byte[] addr)
  {
    int i = addr.length - 1;
    for (; i >= 0 && addr[i] == (byte)0x00; addr[i] = (byte) 0xFE, i--);
    if (i >= 0)
      addr[i]--;

    return addr;
  }

  public static boolean lt(byte[] addr1, byte[] addr2)
  {
    if (addr1.length > addr2.length)
      return false;
    else if (addr2.length > addr1.length)
      return true;

    int max = addr1.length - 1;
    for (int i = 0; i < max; i++) {
      int a1 = addr1[i] & 0xFF,
          a2 = addr2[i] & 0xFF;
      if (a1 > a2)
        return false;
      else if (a2 > a1)
        return true;
    }

    return addr1[max] < addr2[max];
  }

  public static InetAddress toInetAddress(byte[] x)
  {
    try {
      return InetAddress.getByAddress(x);
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

  public static int netmaskShortToInteger(short netmask)
  {
    int nm = 0;

    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
      for (int i = 0; i < netmask; i++) {
        int id = 31 - i;
        nm |= 1 << (Math.abs((int) (id / 8) - 3) * 8 + id % 8);
      }
    } else {
      for (int i = 0; i < netmask; i++) {
        nm |= (1 << 31 - i);
      }
    }

    return nm;
  }

  public static byte[] toArray(int x)
  {
    byte[] ret = new byte[4];

    ret[idx[0]] = (byte) (x & 0xFF);
    ret[idx[1]] = (byte) (0xFF & x >>> 8);
    ret[idx[2]] = (byte) (0xFF & x >>> 16);
    ret[idx[3]] = (byte) (0xFF & x >>> 24);

    return ret;
  }

  public static int toInteger(byte[] address)
  {
    return ((address[idx[3]] & 0xFF) << 24) |
      ((address[idx[2]] & 0xFF) << 16) |
      ((address[idx[1]] & 0xFF) << 8) |
      (address[idx[0]] & 0xFF);
  }

  public static int parseIp(String ip)
    throws java.net.UnknownHostException {
    return toInteger(InetAddress.getByName(ip).getAddress());
  }

  public static IPv4Subnet fromIpNetmask(String addr, String netmask)
  {
    try {
      return fromIpNetmask(addr, (short) countBits(parseIp(netmask)));
    } catch (Exception e) {
      return null;
    }
  }

  public static IPv4Subnet fromIpNetmask(String addr, short netmask)
  {
    if (netmask < 0 || netmask > 31)
      return null;

    try {
      return new IPv4Subnet(parseIp(addr), netmask);
    } catch (Exception e) {
      return null;
    }
  }

  public static IPv4Subnet fromInetAddress(InetAddress addr, short netmask)
  {
    if (netmask < 0 || netmask > 31)
      return null;

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
