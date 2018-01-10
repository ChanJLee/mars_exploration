package com.chan.vision.encode;

/**
 * Created by chan on 2017/12/17.
 */

public class VideoEncoder {
	private int mYLen = -1;
	private Callback mCallback;

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public void encode(byte[] data) {
		if (mYLen <= 0 || data.length < mYLen) {
			return;
		}

		if (mCallback != null) {
			mCallback.onEncoded(data, 0, mYLen);
		}
	}

	public void release() {
		mCallback = null;
	}

	public void setEncodeParameters(int width, int height, int format) {
		mYLen = width * height;
	}

	public interface Callback {
		void onEncoded(byte[] data, int offset, int len);
	}
}
