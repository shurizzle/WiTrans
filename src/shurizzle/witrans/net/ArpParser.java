package shurizzle.witrans.net;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.FileReader;
import java.io.BufferedReader;
import java.net.InetAddress;

import java.lang.Iterable;

import java.io.FileNotFoundException;
import java.lang.UnsupportedOperationException;
import java.lang.IllegalStateException;
import java.util.NoSuchElementException;

public class ArpParser implements Iterable<ArpParser.Entity>
{
  public static final Pattern linePattern;
  public static final String ARP_TABLE = "/proc/net/arp";

  static {
    // http://forums.dartware.com/viewtopic.php?t=452
    String ipv6RE = "(?:(?:(?:[0-9A-Fa-f]{1,4}:){7}(?:[0-9A-Fa-f]{1,4}|:))|(?:(?:[0-9A-Fa-f]{1,4}:){6}(?::[0-9A-Fa-f]{1,4}|(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(?:(?:[0-9A-Fa-f]{1,4}:){5}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,2})|:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(?:(?:[0-9A-Fa-f]{1,4}:){4}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,3})|(?:(?::[0-9A-Fa-f]{1,4})?:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(?:(?:[0-9A-Fa-f]{1,4}:){3}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,4})|(?:(?::[0-9A-Fa-f]{1,4}){0,2}:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(?:(?:[0-9A-Fa-f]{1,4}:){2}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,5})|(?:(?::[0-9A-Fa-f]{1,4}){0,3}:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(?:(?:[0-9A-Fa-f]{1,4}:){1}(?:(?:(?::[0-9A-Fa-f]{1,4}){1,6})|(?:(?::[0-9A-Fa-f]{1,4}){0,4}:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(?::(?:(?:(?::[0-9A-Fa-f]{1,4}){1,7})|(?:(?::[0-9A-Fa-f]{1,4}){0,5}:(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(?:%.+)?";
    String ipv4RE = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){0,3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    String ipRE = "(?:" + ipv4RE + "|" + ipv6RE + ")";
    String hexRE = "0x[0-9a-fA-F]+";
    String MACRE = "(?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}";

    String lineRE = String.format("^\\s*(%1$s)\\s*(%2$s)\\s*(%2$s)\\s*(%3$s)\\s*([^\\s]*)\\s*([^\\s]+)\\s*$", ipRE, hexRE, MACRE);

    linePattern = Pattern.compile(lineRE);
  }

  BufferedReader reader;

  public class Entity
  {
    public final InetAddress address;
    public final int hwtype;
    public final int flags;
    public final String hwaddr;
    public final String mask;
    public final String device;

    public Entity(String addr, String ht, String fl, String ha, String msk, String dev)
    {
      InetAddress a = null;
      try {
        a = InetAddress.getByName(addr);
      } catch (Exception e) {}
      address = a;
      hwtype = parseHex(ht);
      flags = parseHex(fl);
      hwaddr = ha;
      mask = msk;
      device = dev;
    }

    private int parseHex(String hex)
    {
      if (!hex.startsWith("0x"))
        hex = "0x" + hex;
      return Integer.decode(hex);
    }
  }

  public class Iterator implements java.util.Iterator<Entity>
  {
    private Entity current;

    public Iterator()
    {
      current = null;
    }

    @Override
    public boolean hasNext()
    {
      if (current == null) {
        current = nextEntity();
      }

      return current != null;
    }

    @Override
    public Entity next()
      throws NoSuchElementException {
      if (!hasNext())
        throw new NoSuchElementException();
      Entity tmp = current;
      current = null;
      return tmp;
    }

    @Override
    public void remove()
      throws UnsupportedOperationException, IllegalStateException {
      throw new UnsupportedOperationException();
    }
  }

  public ArpParser()
    throws FileNotFoundException {
    reader = new BufferedReader(new FileReader(ARP_TABLE));
  }

  public Iterator iterator()
  {
    return new Iterator();
  }

  private Entity nextEntity()
  {
    try {
      String line = null;
      Matcher matcher = null;

      while ((line = reader.readLine()) != null) {
        if ((matcher = linePattern.matcher(line)) != null && matcher.find()) {
          return new Entity(matcher.group(1), matcher.group(2),
              matcher.group(3), matcher.group(4), matcher.group(5),
              matcher.group(6));
        }
      }
    } catch (Exception e) {}

    return null;
  }
}
