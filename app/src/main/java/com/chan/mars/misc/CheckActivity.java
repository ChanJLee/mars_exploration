package com.chan.mars.misc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.chan.mars.R;

import java.io.IOException;

public class CheckActivity extends AppCompatActivity {

	private StringBuilder mStringBuilder;
	private TextView mTvConsole;
	private SurfaceView mSurfaceView;
	private Camera mCamera;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check);

		mStringBuilder = new StringBuilder();
		mTvConsole = (TextView) findViewById(R.id.console);
		mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview_surface);
		mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				checkCamera();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (mCamera != null) {
					mCamera.release();
				}
			}
		});
		init();
	}

	private void init() {
		checkNetwork();
	}

	@SuppressLint("DefaultLocale")
	private void checkCamera() {
		if (mCamera != null) {
			mCamera.release();
		}

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
			e("have no camera permission");
			return;
		}

		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(mSurfaceView.getHolder());
			mCamera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					Log.d("chan_debug", "frame");
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			e("preview error");
			return;
		}

		mCamera.startPreview();
	}

	private void checkNetwork() {

	}

	private void i(String msg) {
		mStringBuilder.append("> ").append(msg).append("\n");
		mTvConsole.setText(mStringBuilder.toString());
	}

	private void e(String msg) {
		mStringBuilder.append("* ").append(msg).append("\n");
		mTvConsole.setText(mStringBuilder.toString());
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, CheckActivity.class);
	}
}
