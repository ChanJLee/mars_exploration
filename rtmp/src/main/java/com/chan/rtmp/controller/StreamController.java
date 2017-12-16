package com.chan.rtmp.controller;

import android.media.MediaCodec;

import com.chan.rtmp.configuration.VideoConfiguration;
import com.chan.rtmp.controller.video.IVideoController;
import com.chan.rtmp.stream.packer.Packer;
import com.chan.rtmp.stream.sender.Sender;
import com.chan.rtmp.utils.SopCastUtils;
import com.chan.rtmp.video.OnVideoEncodeListener;

import java.nio.ByteBuffer;

public class StreamController implements OnVideoEncodeListener, Packer.OnPacketListener {
	private Packer mPacker;
	private Sender mSender;
	private IVideoController mVideoController;

	public StreamController(IVideoController videoProcessor) {
		mVideoController = videoProcessor;
	}

	public void setVideoConfiguration(VideoConfiguration videoConfiguration) {
		mVideoController.setVideoConfiguration(videoConfiguration);
	}

	public void setPacker(Packer packer) {
		mPacker = packer;
		mPacker.setPacketListener(this);
	}

	public void setSender(Sender sender) {
		mSender = sender;
	}

	public synchronized void start() {
		SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
			@Override
			public void process() {
				if (mPacker == null) {
					return;
				}
				if (mSender == null) {
					return;
				}
				mPacker.start();
				mSender.start();
				mVideoController.setVideoEncoderListener(StreamController.this);
				mVideoController.start();
			}
		});
	}

	public synchronized void stop() {
		SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
			@Override
			public void process() {
				mVideoController.setVideoEncoderListener(null);
				mVideoController.stop();
				if (mSender != null) {
					mSender.stop();
				}
				if (mPacker != null) {
					mPacker.stop();
				}
			}
		});
	}

	public synchronized void pause() {
		SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
			@Override
			public void process() {
				mVideoController.pause();
			}
		});
	}

	public synchronized void resume() {
		SopCastUtils.processNotUI(new SopCastUtils.INotUIProcessor() {
			@Override
			public void process() {
				mVideoController.resume();
			}
		});
	}

	public boolean setVideoBps(int bps) {
		return mVideoController.setVideoBps(bps);
	}

	@Override
	public void onVideoEncode(ByteBuffer bb, MediaCodec.BufferInfo bi) {
		if (mPacker != null) {
			mPacker.onVideoData(bb, bi);
		}
	}

	@Override
	public void onPacket(byte[] data, int packetType) {
		if (mSender != null) {
			mSender.onData(data, packetType);
		}
	}
}
