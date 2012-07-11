package dk.illution.localChat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.R.string;
import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.os.Handler;


public class MainActivity extends Activity implements OnClickListener {
	// Variables
	Button sendButton;
	public TextView log;
	TextView messageBox;
	DatagramSocket socket;
	Handler handler;
	
	// On Create
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // Set on click listener
        sendButton = (Button) findViewById(R.id.sendButton);
        log = (TextView) findViewById(R.id.log);
        messageBox = (TextView) findViewById(R.id.messageBox);
        sendButton.setOnClickListener(this);
        initialize();
        receive();
        log.setFocusable(false);
        handler = new Handler(getApplicationContext().getMainLooper());
    } 
    
    // On Click
    public void onClick(View v) {
    	switch (v.getId()) {
    	    case R.id.sendButton: 
    	    	sendMessage();
    	    break;
    	}
    }
    
    
    public void initialize () {
    	try {
			socket = new DatagramSocket(6464);
			socket.setBroadcast(true);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void sendMessage () {
    	String data = (String) messageBox.getText().toString();
		try {
	    	DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(),
	    	InetAddress.getByName("255.255.255.255"), 2700);
	    	socket.send(packet);
	    	/*
	    	byte[] buffer = new byte[512];
	    	DatagramPacket p = new DatagramPacket(buffer, buffer.length);
	    	socket.receive(p);
	    	log.append(new String(p.getData(), 0, p.getLength()));
	    	*/
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.append("Socket error\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.append("IO Error\r\n");
		}
		
    }
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
        	// Darn it
        }
        return null;
    }
    
    String text = "";
    
    public void logMessage (final String text) {
    	handler.post(new Runnable() {
    	    public void run() {
    	    	log.append(text); 
    	    }
    	});
    	
    }
    
    public void receive() {
	    new Thread(new Runnable() {
			public void run() {
	            log = (TextView) findViewById(R.id.log);
	            try {
	                DatagramSocket clientsocket=new DatagramSocket(2700);
	                byte[] receivedata = new byte[1024];
	                while(true)
	                {
	                  DatagramPacket recv_packet = new DatagramPacket(receivedata, receivedata.length);
	                  try {
	                	  clientsocket.receive(recv_packet);
	                	  String who = "";
	                	  if(recv_packet.getAddress().toString().replace("/", "").equals(getLocalIpAddress())) {
	                		  who = "You";
	                	  } else {
	                		  who = "Someone";
	                	  };
	                	  logMessage(who + ": " + new String(receivedata, 0, recv_packet.getLength()) + "\r\n");
	                  } catch (Exception e) {
	                	  Log.e("UDP", "S: Error", e);
	                  }
	                }
	              } catch (Exception e) {
	                Log.e("UDP", "S: Error", e);
	                log.append("Error");
	              }
	    	} 
    	}
	).start();
}
}