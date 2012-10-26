package shurizzle.witrans.net.subnet;

import java.lang.UnsupportedOperationException;
import java.lang.IllegalStateException;
import java.util.NoSuchElementException;
import java.net.InetAddress;

import com.googlecode.ipv6.IPv6Network;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6NetworkMask;

import shurizzle.witrans.net.Subnet;

public class IPv6Subnet extends Subnet
{
  private IPv6Network network;

  public class Iterator implements java.util.Iterator<InetAddress>
  {
    private java.util.Iterator<IPv6Address> iterator;

    public Iterator()
    {
      iterator = network.iterator();
    }

    @Override
    public boolean hasNext()
    {
      return iterator.hasNext();
    }

    @Override
    public InetAddress next()
      throws NoSuchElementException {
      return toInetAddress(iterator.next());
    }

    @Override
    public void remove()
      throws UnsupportedOperationException, IllegalStateException {
      throw new UnsupportedOperationException();
    }
  }

  private IPv6Subnet(IPv6Address address, short netmask)
  {
    network = IPv6Network.fromAddressAndMask(address, IPv6NetworkMask.fromPrefixLength((int) netmask));
  }

  public InetAddress getNetwork()
  {
    return toInetAddress(network.getFirst());
  }

  public InetAddress getNetmask()
  {
    return toInetAddress(network.getNetmask().asAddress());
  }

  public short getPrefix()
  {
    return (short) network.getNetmask().asPrefixLength();
  }

  public InetAddress getBroadcast()
  {
    return toInetAddress(network.getLast());
  }

  public Iterator iterator()
  {
    return new Iterator();
  }

  @Override
  public String toString()
  {
    return String.format("%s/%d",
        getNetwork().getHostAddress(),
        network.getNetmask().asPrefixLength());
  }

  public static InetAddress toInetAddress(IPv6Address addr)
  {
    try {
      return addr.toInetAddress();
    } catch (Exception e) {
      return null;
    }
  }

  public static IPv6Subnet fromIpNetmask(String addr, String netmask)
  {
    return new IPv6Subnet(IPv6Address.fromString(addr),
        (short)IPv6NetworkMask.fromAddress(IPv6Address.fromString(netmask)).asPrefixLength());
  }

  public static IPv6Subnet fromIpNetmask(String addr, short netmask)
  {
    try {
      return new IPv6Subnet(IPv6Address.fromString(addr), netmask);
    } catch (Exception e) {
      return null;
    }
  }

  public static IPv6Subnet fromInetAddress(InetAddress addr, short netmask)
  {
    return fromIpNetmask(addr.getHostAddress().split("%")[0], netmask);
  }

  public static IPv6Subnet fromString(String subnet)
  {
    int slash = subnet.indexOf('/');

    if (slash == -1)
      return null;

    String ip = subnet.substring(0, slash);
    short nm = (short) Integer.parseInt(subnet.substring(slash + 1));

    return fromIpNetmask(ip, nm);
  }
}
