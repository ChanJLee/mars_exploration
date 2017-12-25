package com.chan.vision;

import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import com.chan.vision.camera.CameraCompat;
import com.chan.vision.encode.VideoEncoder;

import java.nio.ByteBuffer;

/**
 * Created by chan on 2017/12/17.
 */

public class Vision {
	private static final String TAG = "vision";

	private VideoEncoder mVideoEncoder;
	private VisionCallback mVisionCallback;
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
			public void onEncoded(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
				if (mVisionCallback != null) {
					mVisionCallback.onPreview(byteBuffer, bufferInfo);
				}
			}
		});
	}

	boolean writed = false;

	public void start() {
		try {
			mVideoEncoder.start();
			CameraCompat camera = CameraCompat.getInstance();
			camera.open();
			camera.setPreviewCallback(new CameraCompat.PreviewCallback() {
				@Override
				public void onWindowSizeChange(int width, int height) {
					mVideoEncoder.setWindowSize(width, height);
				}

				@Override
				public void onPreviewFrame(byte[] data) {
					mVideoEncoder.encode(data);
				}
			});
			camera.startPreview(mSurfaceHolder, mWidth, mHeight);

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

	private static void d(String msg) {
		Log.d(TAG, msg);
	}

	public interface VisionCallback {
		void onError(Throwable error);

		void onStart();

		void onPreview(ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);

		void onRelease();
	}
}
