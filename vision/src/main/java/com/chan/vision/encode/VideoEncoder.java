package com.chan.vision.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by chan on 2017/12/17.
 */

public class VideoEncoder {
	private int mWidth = -1;
	private int mHeight = -1;
	private Callback mCallback;
	private MediaCodec mMediaCodec;
	private MediaCodec.BufferInfo mBufferInfo;
	private long mTimestamp;

	private void initMediaCodec(int width, int height) {
		MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
		format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 6);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
		try {
			mMediaCodec = MediaCodec.createEncoderByType("video/avc");
			mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
		} catch (Exception e) {
			e.printStackTrace();
			if (mMediaCodec != null) {
				mMediaCodec.stop();
				mMediaCodec.release();
				mMediaCodec = null;
			}
			throw new RuntimeException("create media codec failed");
		}
		mBufferInfo = new MediaCodec.BufferInfo();
	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public void encode(byte[] data) {
		if (mHeight < 0 || mWidth < 0 || mMediaCodec == null) {
			return;
		}

		int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
			inputBuffer.rewind();
			Log.d("chan_debug", inputBuffer.limit() + " " + inputBuffer.position() + " " + data.length);
			inputBuffer.put(data);
			mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, SystemClock.elapsedRealtime() - mTimestamp, 0);
		}

		ByteBuffer[] outBuffers = mMediaCodec.getOutputBuffers();
		int outBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 11000);
		if (outBufferIndex >= 0) {
			ByteBuffer byteBuffer = outBuffers[outBufferIndex];
			if (mCallback != null) {
				mCallback.onEncoded(byteBuffer, mBufferInfo);
			}
			mMediaCodec.releaseOutputBuffer(outBufferIndex, false);
		}
	}

	public void release() {
		if (mMediaCodec != null) {
			mMediaCodec.signalEndOfInputStream();
			mMediaCodec.stop();
			mMediaCodec.release();
			mMediaCodec = null;
		}
	}

	public void start() {
		mTimestamp = SystemClock.elapsedRealtime();
		mMediaCodec.start();
	}

	public void setWindowSize(int width, int height) {
		mWidth = width;
		mHeight = height;
		if (mWidth < 0 || mHeight < 0) {
			return;
		}

		MediaCodec mediaCodec = mMediaCodec;
		mMediaCodec = null;
		if (mediaCodec != null) {
			mediaCodec.signalEndOfInputStream();
			mediaCodec.stop();
			mediaCodec.release();
		}
		initMediaCodec(width, height);
	}

	public interface Callback {
		void onEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
	}
}
