package com.chan.vision.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.chan.vision.exception.VisionError;

import java.io.IOException;
import java.util.List;

/**
 * Created by chan on 2017/12/17.
 */

public class CameraCompat {

	private static final int DEFAULT_FPS = 15;
	private static CameraCompat sCameraCompat;
	private Camera mCamera;
	private byte[] mCameraBuffer;
	private PreviewCallback mPreviewCallback;

	public void open() {
		open(getDefaultCamera());
	}

	public void open(int id) {
		if (mCamera != null) {
			throw new IllegalStateException("camera has opened");
		}

		if (Camera.getNumberOfCameras() < 0) {
			throw new RuntimeException("current platform has no camera");
		}

		mCamera = Camera.open(id);
		setPreviewFormat(ImageFormat.NV21);
		setPreviewFps(DEFAULT_FPS);
	}

	private void setPreviewFps(int fps) {
		Camera.Parameters parameters = mCamera.getParameters();
		try {
			parameters.setPreviewFrameRate(fps);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}

		int[] range = adaptPreviewFps(fps, parameters.getSupportedPreviewFpsRange());

		try {
			parameters.setPreviewFpsRange(range[0], range[1]);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setPreviewSize(int width, int height) {
		Camera.Size size = getOptimalPreviewSize(width, height);
		if (size == null) {
			return;
		}
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(size.width, size.height);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Camera.Size getOptimalPreviewSize(int width, int height) {
		Camera.Size optimalSize = null;
		double minHeightDiff = Double.MAX_VALUE;
		double minWidthDiff = Double.MAX_VALUE;
		List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
		if (sizes == null) return null;
		//找到宽度差距最小的
		for (Camera.Size size : sizes) {
			if (Math.abs(size.width - width) < minWidthDiff) {
				minWidthDiff = Math.abs(size.width - width);
			}
		}
		//在宽度差距最小的里面，找到高度差距最小的
		for (Camera.Size size : sizes) {
			if (Math.abs(size.width - width) == minWidthDiff) {
				if (Math.abs(size.height - height) < minHeightDiff) {
					optimalSize = size;
					minHeightDiff = Math.abs(size.height - height);
				}
			}
		}
		return optimalSize;
	}


	private int[] adaptPreviewFps(int expectedFps, List<int[]> fpsRanges) {
		expectedFps *= 1000;
		int[] closestRange = fpsRanges.get(0);
		int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
		for (int[] range : fpsRanges) {
			if (range[0] <= expectedFps && range[1] >= expectedFps) {
				int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
				if (curMeasure < measure) {
					closestRange = range;
					measure = curMeasure;
				}
			}
		}
		return closestRange;
	}

	private void setPreviewFormat(int format) {
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewFormat(format);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			throw new VisionError("platform do not support nv21");
		}
	}

	public void release() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	private int getDefaultCamera() {
		int numberOfCameras = Camera.getNumberOfCameras();
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				return i;
			}
		}
		return 0;
	}

	public static CameraCompat getInstance() {
		if (sCameraCompat == null) {
			synchronized (CameraCompat.class) {
				if (sCameraCompat == null) {
					sCameraCompat = new CameraCompat();
				}
			}
		}
		return sCameraCompat;
	}

	public void setPreviewCallback(PreviewCallback callback) {
		mPreviewCallback = callback;
		if (mCamera != null) {
			mCamera.setPreviewCallbackWithBuffer(callback == null ? null : new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					mPreviewCallback.onPreviewFrame(data);
					camera.addCallbackBuffer(mCameraBuffer);
				}
			});
		}
	}

	public void startPreview(SurfaceHolder surfaceHolder, int width, int height) {
		setPreviewSize(width, height);
		Camera.Parameters parameters = mCamera.getParameters();
		Camera.Size size = mCamera.getParameters().getPreviewSize();
		if (mPreviewCallback != null) {
			mPreviewCallback.onWindowSizeChange(size.width, size.height);
		}
		int pixelSize = ImageFormat.getBitsPerPixel(parameters.getPreviewFormat());
		int bufferSize = size.width * size.height * pixelSize / 8;
		mCameraBuffer = new byte[bufferSize];
		mCamera.addCallbackBuffer(mCameraBuffer);
		surfaceHolder.addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					mCamera.setPreviewDisplay(holder);
					mCamera.startPreview();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				mCamera.stopPreview();
				mCamera.release();
			}
		});
	}

	public interface PreviewCallback {
		void onWindowSizeChange(int width, int height);

		void onPreviewFrame(byte[] data);
	}
}
