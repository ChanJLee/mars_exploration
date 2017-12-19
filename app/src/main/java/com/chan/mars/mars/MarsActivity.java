package com.chan.mars.mars;

import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.chan.mars.R;
import com.chan.rtmp.stream.packer.Packer;
import com.chan.rtmp.stream.packer.rtmp.RtmpPacker;
import com.chan.rtmp.stream.sender.rtmp.RtmpSender;
import com.chan.vision.Vision;

import java.nio.ByteBuffer;

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

		final RtmpPacker rtmpPacker = new RtmpPacker();
		final RtmpSender rtmpSender = new RtmpSender();
		rtmpSender.setSenderListener(new RtmpSender.OnSenderListener() {
			@Override
			public void onConnecting() {
				d("connecting");
			}

			@Override
			public void onConnected() {
				d("connected");
			}

			@Override
			public void onDisConnected() {
				d("disconnected");
			}

			@Override
			public void onPublishFail() {
				d("publish fail");
			}

			@Override
			public void onNetGood() {
				d("net good");
			}

			@Override
			public void onNetBad() {
				d("net bad");
			}
		});
		rtmpPacker.setPacketListener(new Packer.OnPacketListener() {
			@Override
			public void onPacket(byte[] data, int packetType) {
				d("packet");
				rtmpSender.onData(data, packetType);
			}
		});
		SurfaceView surfaceView = findViewById(R.id.camera);
		mVision = new Vision(surfaceView.getHolder(), 320, 160);
		mVision.setVisionCallback(new Vision.VisionCallback() {
			@Override
			public void onError(Throwable error) {
				d("error");
			}

			@Override
			public void onStart() {
				d("start");
				new Thread(new Runnable() {
					@Override
					public void run() {
						rtmpSender.setAddress("rtmp://192.168.0.101:1396/chan_live/rtmpstream");
						rtmpSender.connect();
						rtmpSender.start();
						rtmpPacker.start();
					}
				}).start();
			}

			@Override
			public void onPreview(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
				d("preview");
				rtmpPacker.onVideoData(byteBuffer, bufferInfo);
			}

			@Override
			public void onRelease() {
				d("release");
			}
		});
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

	private static void d(String msg) {
		Log.d("Mars", msg);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, MarsActivity.class);
	}
}
