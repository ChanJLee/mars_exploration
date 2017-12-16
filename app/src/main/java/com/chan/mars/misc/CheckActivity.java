package com.chan.mars.misc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.chan.mars.R;

public class CheckActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pretest);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, CheckActivity.class);
	}
}
