package shurizzle.witrans;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import shurizzle.witrans.net.Network;

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

    if (nw.isConnected()) {
      text.setText((nw.isApConnected() ? "AP: " : "WIFI: ") + nw.getSubnet());
    } else {
      text.setText("DISCONNECTED");
    }
  }
}
