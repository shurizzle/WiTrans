package shurizzle.witrans;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.content.Intent;

import shurizzle.witrans.net.Network;
import shurizzle.witrans.net.ArpParser;
import shurizzle.witrans.net.NetworkMonitor;
import shurizzle.witrans.net.subnet.IPv4Subnet;

import java.net.NetworkInterface;
import java.net.InetAddress;

public class MainActivity extends Activity
{
  TextView text;
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    text = (TextView) findViewById(R.id.text);

    if (!NetworkMonitor.isInstanceCreated())
      startService(new Intent(this, NetworkMonitor.class));

    Network nw = new Network(getApplicationContext());

    text.setText("");

    if (nw.isConnected()) {
      text.append((nw.isApConnected() ? "AP: " : "WIFI: ") + nw.getSubnet());
    } else {
      text.append("DISCONNECTED");
    }
    text.append("\n");

    try {
      ArpParser.Iterator arp = new ArpParser().iterator();

      while (arp.hasNext()) {
        ArpParser.Entity ent = arp.next();
        text.append(ent.address.getHostAddress());
        text.append("\n");
      }
    } catch (Exception e) {}
  }
}
