package com.chan.vision.encode;

/**
 * Created by chan on 2017/12/17.
 */

public class VideoEncoder {
	private Callback mCallback;

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public void encode(byte[] data) {
	}

	public void release() {
	}

	public void start() {
	}

	public interface Callback {
		void onEncoded(byte[] data, int offset, int len);
	}
}
