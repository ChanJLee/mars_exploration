package com.chan.vision;

import com.chan.vision.compat.CameraCompat;
import com.chan.vision.encode.VideoEncoder;

/**
 * Created by chan on 2017/12/17.
 */

public class Vision {

	private VideoEncoder mVideoEncoder;
	private VisionCallback mVisionCallback;

	public Vision() {
		mVideoEncoder = new VideoEncoder();
		mVideoEncoder.setCallback(new VideoEncoder.Callback() {
			@Override
			public void onEncoded(byte[] data, int offset, int len) {
				if (mVisionCallback != null) {
					mVisionCallback.onPreview(data, offset, len);
				}
			}
		});
	}

	public void start() {
		try {
			mVideoEncoder.start();
			CameraCompat camera = CameraCompat.getInstance();
			camera.open();
			camera.setPreviewCallback(new CameraCompat.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data) {
					mVideoEncoder.encode(data);
				}
			});
			camera.startPreview();

			if (mVisionCallback != null) {
				mVisionCallback.onStart();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mVisionCallback != null) {
				mVisionCallback.onError(e);
			}
		}
	}

	public void release() {
		CameraCompat.getInstance().release();
		mVideoEncoder.release();
		if (mVisionCallback != null) {
			mVisionCallback.onRelease();
		}
	}

	public void setVisionCallback(VisionCallback visionCallback) {
		mVisionCallback = visionCallback;
	}

	public interface VisionCallback {
		void onError(Throwable error);

		void onStart();

		void onPreview(byte[] data, int offset, int len);

		void onRelease();
	}
}
