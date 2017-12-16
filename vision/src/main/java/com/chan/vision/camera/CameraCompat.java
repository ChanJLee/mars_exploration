package com.chan.vision.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;

import com.chan.vision.exception.VisionError;

import java.util.List;

/**
 * Created by chan on 2017/12/17.
 */

public class CameraCompat {
	private static CameraCompat sCameraCompat;
	private Camera mCamera;

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
		setPreviewFps(24, parameters);
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
		//设置预览回调的图片格式
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

	public void setPreviewCallback(final PreviewCallback callback) {
		if (mCamera != null) {
			mCamera.setPreviewCallback(callback == null ? null : new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					callback.onPreviewFrame(data);
				}
			});
		}
	}

	public void startPreview() {
	}

	public interface PreviewCallback {
		void onPreviewFrame(byte[] data);
	}
}
