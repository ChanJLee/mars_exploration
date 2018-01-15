package com.chan.mars.misc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.chan.mars.R;
import com.chan.mars.mars.MarsActivity;

public class DispatchActivity extends AppCompatActivity implements View.OnClickListener {

	private Button mBtnCheckHardware;
	private Button mBtnStart;
	private Button mBtnBlueTooth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dispatch);

		mBtnCheckHardware = findViewById(R.id.check_hardware);
		mBtnCheckHardware.setOnClickListener(this);

		mBtnStart = findViewById(R.id.start);
		mBtnStart.setOnClickListener(this);

		mBtnBlueTooth = findViewById(R.id.blue_tooth);
		mBtnBlueTooth.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnCheckHardware) {
			startActivity(CheckActivity.createIntent(this));
		} else if (v == mBtnStart) {
			startActivity(MarsActivity.createIntent(this));
		} else {
			startActivity(BlueToothActivity.createIntent(this));
		}
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, DispatchActivity.class);
	}
}
