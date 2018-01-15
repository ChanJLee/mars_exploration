package com.chan.mars.mars;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chan.mars.R;
import com.chan.protocol.MarsServer;
import com.chan.vision.Vision;

public class MarsActivity extends AppCompatActivity implements View.OnClickListener {

	private EditText mEtAddress;
	private Button mBtnLive;
	private Vision mVision;
	private MarsServer mSender;
	private SurfaceView mSurfaceView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mars);

		mBtnLive = findViewById(R.id.start_live);
		mEtAddress = findViewById(R.id.address);
		mBtnLive.setOnClickListener(this);

		mSurfaceView = findViewById(R.id.camera);

		mSender = new MarsServer("192.168.0.102", 8765);
		mSender.setListener(new MarsServer.Listener() {
			@Override
			public void onConnected() {
			}

			@Override
			public void onReceiveAction(int action) {

			}

			@Override
			public void onError(Throwable throwable) {
				d("send message error");
			}
		});
		mSender.start();
		mVision = new Vision(mSurfaceView.getHolder(), 320, 160);
		mVision.setListener(new Vision.Listener() {
			@Override
			public void onError(Throwable error) {
				d("error");
			}

			@Override
			public void onStart() {
				d("start");
			}

			@Override
			public void onWindowSizeChanged(int width, int height) {
				if (mSender != null) {
					mSender.sendWindowSize(width, height);
				}
			}

			@Override
			public void onPreview(byte[] data, int offset, int len) {
				if (mSender != null) {
					mSender.sendImage(data, offset, len);
				}
			}

			@Override
			public void onRelease() {
				d("release");
				if (mSender != null) {
					mSender.release();
					mSender = null;
				}
			}
		});

		mVision.start();
	}

	@Override
	protected void onDestroy() {
		if (mVision != null) {
			mVision.release();
			mVision = null;
		}

		if (mSender != null) {
			mSender.release();
			mSender = null;
		}
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

	private static void d(String msg) {
		Log.d("Mars", msg);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, MarsActivity.class);
	}
}
