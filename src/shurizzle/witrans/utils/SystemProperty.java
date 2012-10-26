package shurizzle.witrans.utils;

import java.lang.StringBuilder;
import java.io.InputStreamReader;
import java.lang.Process;
import java.lang.Runtime;

public class SystemProperty
{
  private SystemProperty() {}

  private static String readAll(InputStreamReader read)
  {
    StringBuilder sb = new StringBuilder();

    try {
      char[] cbuf = new char[1024];
      while (read.ready()) {
        int r = read.read(cbuf, 0, 1024);
        sb.append(cbuf, 0, r);
      }
    } catch (Exception e) {}

    return sb.toString();
  }

  public static String get(String key, String def)
  {
    try {
      Process proc = Runtime.getRuntime().exec(new String[] {"getprop", key});
      proc.waitFor();

      String res = readAll(new InputStreamReader(proc.getInputStream()));
      if (res.charAt(res.length() - 1) == '\n') {
        if (res.charAt(res.length() - 2) == '\r') {
          res = res.substring(0, res.length() - 2);
        } else {
          res = res.substring(0, res.length() - 1);
        }
      }

      return res;
    } catch (Exception e) {
      return null;
    }
  }
}
