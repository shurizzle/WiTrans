package shurizzle.witrans.utils;

import java.lang.ClassLoader;
import java.lang.Class;
import java.lang.reflect.Method;

public class SystemProperty
{
  private static Class SYSPROP;
  private static Method GET_METHOD;

  static {
    ClassLoader classLoader = SystemProperty.class.getClassLoader();
    try {
      SYSPROP = classLoader.loadClass("android.os.SystemProperties");
      GET_METHOD = SYSPROP.getMethod("get", String.class, String.class);
    } catch (Exception e) {}
  }

  private SystemProperty() {}

  public static String get(String key, String def)
  {
    String res;

    try {
      res = (String) GET_METHOD.invoke(SYSPROP, key, def);
    } catch (Exception e) {
      res = def;
    }

    return res;
  }
}
