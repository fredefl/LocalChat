package dk.illution.localChat.fragments;

import com.actionbarsherlock.app.SherlockFragment;

import dk.illution.localChat.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("ValidFragment")
public class ColorFragment extends SherlockFragment {

	String conversationName;
	int conversationId;
	private SharedPreferences mPrefs;

	public ColorFragment() {
	}

	public ColorFragment(String conversationName, int conversationId) {
		this.conversationName = conversationName;
		this.conversationId = conversationId;
		setRetainInstance(true);
	}

	public void onPause() {
		super.onPause();
		SharedPreferences.Editor ed = mPrefs.edit();
		ed.putString("conversationName", this.conversationName);
		ed.putInt("conversationId", this.conversationId);
		ed.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/*if (savedInstanceState != null)
			mColorRes = savedInstanceState.getInt("mColorRes");
		int color = getResources().getColor(mColorRes);*/
		mPrefs = getSherlockActivity().getPreferences(1);
		// construct the RelativeLayout
		if (this.conversationId == -1) {

			this.conversationName = mPrefs.getString("conversationName", "");
			this.conversationId = mPrefs.getInt("conversationId", -1);
		}
		if (this.conversationId == -1) {
			Context context = getSherlockActivity().getApplicationContext();
			CharSequence text = "No conversation has been started, please initiate one from the side panel";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}

		View v = inflater.inflate(R.layout.conversation, null);
		getSherlockActivity().getSupportActionBar().setTitle(conversationName);
		Button sendButton = (Button) v.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.d("LocalChat", "Clicked button");
				EditText messageBox = (EditText) v.findViewById(R.id.messageBox);

				messageBox.setText("");
			}
		});
		return v;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("mConversationName", conversationName);
		outState.putInt("mConversationId", conversationId);
	}

}
