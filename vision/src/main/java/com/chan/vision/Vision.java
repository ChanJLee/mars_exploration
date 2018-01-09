package com.chan.vision;

import android.view.SurfaceHolder;

import com.chan.vision.camera.CameraCompat;
import com.chan.vision.encode.VideoEncoder;

/**
 * Created by chan on 2017/12/17.
 */

public class Vision {
	private static final String TAG = "vision";

	private VideoEncoder mVideoEncoder;
	private Listener mListener;
	private SurfaceHolder mSurfaceHolder;
	private int mWidth;
	private int mHeight;

	public Vision(SurfaceHolder surfaceHolder, int width, int height) {
		mWidth = width;
		mHeight = height;
		mSurfaceHolder = surfaceHolder;
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mVideoEncoder = new VideoEncoder();
		mVideoEncoder.setCallback(new VideoEncoder.Callback() {
			@Override
			public void onEncoded(byte[] data, int offset, int len) {
				if (mListener != null) {
					mListener.onPreview(data, offset, len);
				}
			}
		});
	}

	public void start() {
		try {
			CameraCompat camera = CameraCompat.getInstance();
			camera.open();
			camera.setPreviewCallback(new CameraCompat.PreviewCallback() {
				@Override
				public void onConfigChange(int width, int height, int format) {
					mVideoEncoder.setEncodeParameters(width, height, format);
					if (mListener != null) {
						mListener.onWindowSizeChanged(width, height);
					}
				}

				@Override
				public void onPreviewFrame(byte[] data) {
					mVideoEncoder.encode(data);
				}
			});
			camera.startPreview(mSurfaceHolder, mWidth, mHeight);

			if (mListener != null) {
				mListener.onStart();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mListener != null) {
				mListener.onError(e);
			}
		}
	}

	public void release() {
		CameraCompat.getInstance().release();
		mVideoEncoder.release();
		if (mListener != null) {
			mListener.onRelease();
		}
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	public interface Listener {
		void onError(Throwable error);

		void onStart();

		void onWindowSizeChanged(int width, int height);

		void onPreview(byte[] data, int offset, int len);

		void onRelease();
	}
}
