package com.chan.mars.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chan.mars.R;
import com.chan.vision.Vision;

public class MarsActivity extends AppCompatActivity implements View.OnClickListener {

	private EditText mEtAddress;
	private Button mBtnLive;
	private Vision mVision;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mars);

		mBtnLive = findViewById(R.id.start_live);
		mEtAddress = findViewById(R.id.address);
		mBtnLive.setOnClickListener(this);

		mVision = new Vision(this);
		mVision.start();
	}

	@Override
	protected void onDestroy() {
		mVision.release();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnLive) {
			//String url = mEtAddress.getText().toString();
			//TODO
			finish();
		}
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, MarsActivity.class);
	}
}
