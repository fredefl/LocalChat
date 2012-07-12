package dk.illution.localChat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity implements OnClickListener {
	// Variables
	Button sendButton;
	public TextView log;
	TextView messageBox;
	DatagramSocket senderSocket;
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
		messageBox.setOnEditorActionListener(textViewActionListener);
		sendButton.setOnClickListener(this);
		// Set up sender socket
		initialize();
		// Set up receiver socket
		receive();
		// Get the GUI handler
		handler = new Handler(getApplicationContext().getMainLooper());
	}


	TextView.OnEditorActionListener textViewActionListener = new TextView.OnEditorActionListener(){

		public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
			sendMessage();
			return true;
		}
	};

	// On Click
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sendButton:
				// Send the message
				sendMessage();
			break;
		}
	}


	public void initialize () {
		try {
			// Set up socket and send from port 6464
			senderSocket = new DatagramSocket(6464);
			// Make sure broadcast is enabled
			senderSocket.setBroadcast(true);
		} catch (SocketException e) {
			Log.e("LocalChat", "Error setting up sender socket", e);
		}
	}

	public void sendMessage () {
		// Get text from TextView/messageBox
		String text = (String) messageBox.getText().toString();
		try {
			// Create the UDP packet
			DatagramPacket packet = new DatagramPacket(
				text.getBytes(),
				text.length(),
				InetAddress.getByName("255.255.255.255"),
				2700
			);
			// Send the packet!
			senderSocket.send(packet);
			// Free some memory
			packet = null;
			text = null;
			messageBox.setText("");
		} catch (SocketException e) {
			Log.e("LocalChat", "Socket error", e);
		} catch (IOException e) {
			Log.e("LocalChat", "IO Error", e);
		}

	}

	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						en = null;
						intf = null;
						enumIpAddr = null;
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			// Darn it
			Log.e("LocalChat", "Error getting local IP address", e);
		}
		return null;
	}

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
					DatagramSocket receiverSocket = new DatagramSocket(2700);
					byte[] receivedData = new byte[1024];
					while (true) {
						DatagramPacket packet = new DatagramPacket(receivedData, receivedData.length);
						try {
							receiverSocket.receive(packet);
							String senderName = "";
							if(packet.getAddress().toString().replace("/", "").equals(getLocalIpAddress())) {
								senderName = "You";
							} else {
								senderName = "Someone";
							}
							logMessage(senderName + ": " + new String(receivedData, 0, packet.getLength()) + "\r\n");
							senderName = null;
						} catch (Exception e) {
							Log.e("LocalChat", "Recieve error", e);
						}
						packet = null;
					}
				} catch (Exception e) {
					Log.e("LocalChat", "Can't create receiver socket", e);
					log.append("Error");
				} finally {
					log = null;
				}
			}
		}).start();
	}
};