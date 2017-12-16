package com.chan.mars.mars;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chan.mars.R;
import com.chan.rtmp.camera.CameraListener;
import com.chan.rtmp.configuration.AudioConfiguration;
import com.chan.rtmp.configuration.CameraConfiguration;
import com.chan.rtmp.configuration.VideoConfiguration;
import com.chan.rtmp.stream.packer.rtmp.RtmpPacker;
import com.chan.rtmp.stream.sender.rtmp.RtmpSender;
import com.chan.rtmp.ui.CameraLivingView;

public class MarsActivity extends AppCompatActivity implements View.OnClickListener {

	private EditText mEtAddress;
	private Button mBtnLive;
	private CameraLivingView mLFLiveView;
	private GestureDetector mGestureDetector;
	private VideoConfiguration mVideoConfiguration;
	private RtmpSender mRtmpSender;
	private int mCurrentBps;
	private RtmpSender.OnSenderListener mSenderListener = new RtmpSender.OnSenderListener() {
		@Override
		public void onConnecting() {

		}

		@Override
		public void onConnected() {
			mLFLiveView.start();
			mCurrentBps = mVideoConfiguration.maxBps;
		}

		@Override
		public void onDisConnected() {
			Toast.makeText(MarsActivity.this, "fail to live", Toast.LENGTH_SHORT).show();
			mLFLiveView.stop();
		}

		@Override
		public void onPublishFail() {
			Toast.makeText(MarsActivity.this, "fail to publish stream", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onNetGood() {
			if (mCurrentBps + 50 <= mVideoConfiguration.maxBps) {
				int bps = mCurrentBps + 50;
				if (mLFLiveView != null) {
					boolean result = mLFLiveView.setVideoBps(bps);
					if (result) {
						mCurrentBps = bps;
					}
				}
			}
		}

		@Override
		public void onNetBad() {
			if (mCurrentBps - 100 >= mVideoConfiguration.minBps) {
				int bps = mCurrentBps - 100;
				if (mLFLiveView != null) {
					boolean result = mLFLiveView.setVideoBps(bps);
					if (result) {
						mCurrentBps = bps;
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mars);

		mBtnLive = findViewById(R.id.start_live);
		mEtAddress = findViewById(R.id.address);
		mLFLiveView = findViewById(R.id.live_view);

		mBtnLive.setOnClickListener(this);
		mLFLiveView.init();
		CameraConfiguration.Builder cameraBuilder = new CameraConfiguration.Builder();
		cameraBuilder.setOrientation(CameraConfiguration.Orientation.LANDSCAPE)
				.setFacing(CameraConfiguration.Facing.BACK);
		CameraConfiguration cameraConfiguration = cameraBuilder.build();
		mLFLiveView.setCameraConfiguration(cameraConfiguration);

		VideoConfiguration.Builder videoBuilder = new VideoConfiguration.Builder();
		videoBuilder.setSize(640, 360);
		mVideoConfiguration = videoBuilder.build();
		mLFLiveView.setVideoConfiguration(mVideoConfiguration);

		//设置预览监听
		mLFLiveView.setCameraOpenListener(new CameraListener() {
			@Override
			public void onOpenSuccess() {
				Toast.makeText(MarsActivity.this, "摄像头已经打开", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onOpenFail(int error) {
				Toast.makeText(MarsActivity.this, "摄像头打开失败", Toast.LENGTH_LONG).show();
			}

			@Override
			public void onCameraChange() {
				Toast.makeText(MarsActivity.this, "切换摄像头", Toast.LENGTH_LONG).show();
			}
		});

		//设置手势识别
		mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener());
		mLFLiveView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mGestureDetector.onTouchEvent(event);
				return false;
			}
		});

		//初始化flv打包器
		RtmpPacker packer = new RtmpPacker();
		packer.initAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
		mLFLiveView.setPacker(packer);

		//设置发送器
		mRtmpSender = new RtmpSender();
		mRtmpSender.setVideoParams(640, 360);
		mRtmpSender.setAudioParams(AudioConfiguration.DEFAULT_FREQUENCY, 16, false);
		mRtmpSender.setSenderListener(mSenderListener);
		mLFLiveView.setSender(mRtmpSender);
		mLFLiveView.setLivingStartListener(new CameraLivingView.LivingStartListener() {
			@Override
			public void startError(int error) {
				//直播失败
				Toast.makeText(MarsActivity.this, "推流失败", Toast.LENGTH_SHORT).show();
				mLFLiveView.stop();
			}

			@Override
			public void startSuccess() {
				//直播成功
				Toast.makeText(MarsActivity.this, "推流成功", Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnLive) {
			String url = mEtAddress.getText().toString();
			mRtmpSender.setAddress(url);
			Toast.makeText(this, "开始推流", Toast.LENGTH_SHORT).show();
			mRtmpSender.connect();
		}
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, MarsActivity.class);
	}
}
