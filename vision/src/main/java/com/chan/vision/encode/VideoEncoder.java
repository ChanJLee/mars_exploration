package com.chan.vision.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;

import java.nio.ByteBuffer;

/**
 * Created by chan on 2017/12/17.
 */

public class VideoEncoder {
	private Callback mCallback;
	private MediaCodec mMediaCodec;
	private MediaCodec.BufferInfo mBufferInfo;
	private long mTimestamp;

	public VideoEncoder() {
		MediaFormat format = MediaFormat.createVideoFormat("video/avc", 360, 640);
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 38016);
		format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
		format.setInteger(MediaFormat.KEY_BIT_RATE, 32 * 360 * 640 * 15 / 100);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);
		format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
		format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
		format.setInteger(MediaFormat.KEY_COMPLEXITY, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR);
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
		ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
		int inputBufferIndex = mMediaCodec.dequeueInputBuffer(10000);
		if (inputBufferIndex >= 0) {
			ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
			inputBuffer.clear();
			inputBuffer.put(data);
			mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, SystemClock.elapsedRealtime() - mTimestamp, 0);
		}

		ByteBuffer[] outBuffers = mMediaCodec.getOutputBuffers();
		int outBufferIndex = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 12000);
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

	public interface Callback {
		void onEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
	}
}
