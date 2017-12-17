package com.chan.vision.camera;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;

import com.chan.vision.exception.VisionError;

import java.io.IOException;
import java.util.List;

/**
 * Created by chan on 2017/12/17.
 */

public class CameraCompat {
	private static final String TAG = "CameraCompat";

	private static CameraCompat sCameraCompat;
	private Camera mCamera;
	private Activity mActivity;

	public CameraCompat(Activity activity) {
		mActivity = activity;
	}

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
		Camera.Parameters parameters = mCamera.getParameters();
		setPreviewFormat(parameters);
		setPreviewFps(15, parameters);
		setPreviewSize(360, 640, parameters);
	}

	private void setPreviewFps(int fps, Camera.Parameters parameters) {
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

	public void setPreviewSize(int width, int height, Camera.Parameters parameters) {
		Camera.Size size = getOptimalPreviewSize(width, height);
		if (size == null) {
			return;
		}
		try {
			parameters.setPreviewSize(size.width, size.height);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Camera.Size getOptimalPreviewSize(int width, int height) {
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

	private void setPreviewFormat(Camera.Parameters parameters) {
		try {
			parameters.setPreviewFormat(ImageFormat.NV21);
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

	public static CameraCompat getInstance(Activity activity) {
		if (sCameraCompat == null) {
			synchronized (CameraCompat.class) {
				if (sCameraCompat == null) {
					sCameraCompat = new CameraCompat(activity);
				}
			}
		}
		return sCameraCompat;
	}

	public void setPreviewCallback(final PreviewCallback callback) {
		if (mCamera != null) {
			mCamera.setPreviewCallback(callback == null ? null : new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					d("frame");
					//callback.onPreviewFrame(data);
				}
			});
		}
	}

	public void startPreview() {
		Camera.Parameters parameters = mCamera.getParameters();
		Camera.Size size = mCamera.getParameters().getPreviewSize();
		int bufferSize = size.width * size.height * ImageFormat.getBitsPerPixel(parameters.getPreviewFormat()) / 8;
		mCamera.addCallbackBuffer(new byte[bufferSize]);
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		SurfaceTexture surfaceView = new SurfaceTexture(textures[0]);
		try {
			mCamera.setPreviewTexture(surfaceView);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mCamera.startPreview();
	}

	private static void d(String msg) {
		Log.d(TAG, msg);
	}

	public interface PreviewCallback {
		void onPreviewFrame(byte[] data);
	}
}
