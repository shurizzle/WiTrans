package shurizzle.witrans;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import shurizzle.witrans.net.Network;
import shurizzle.witrans.net.ArpParser;

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

    Network nw = new Network(getApplicationContext());

    text.setText("");

    if (nw.isConnected()) {
      text.setText((nw.isApConnected() ? "AP: " : "WIFI: ") + nw.getSubnet());
    } else {
      text.setText("DISCONNECTED");
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
