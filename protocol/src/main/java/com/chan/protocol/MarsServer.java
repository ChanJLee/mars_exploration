package com.chan.protocol;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by chan on 2018/1/4.
 */

public class MarsServer {
	private static final byte[] MAGIC_HEADER = {0x05, 0x21, 0x05, 0x25, 0x12, 0x12, 0x01, 0x18};
	/*
	 * type len, 2B
	 * */
	private static final int TYPE_LEN = 2;

	/*
	 * package len 10B
	 * */
	private static final int PACKAGE_LEN = 4;

	/*
	 * mata data len 12B
	 * */
	private static final int META_DATA_LEN = TYPE_LEN + PACKAGE_LEN;

	/*
	 * head len
	 * */
	private static final int HEADER_LEN = MAGIC_HEADER.length + META_DATA_LEN;

	private String mHost;
	private int mPort;

	private Socket mSocket;
	private Thread mInitThread;

	private Thread mReadThread;
	private HandlerThread mWriteThread;

	private Handler mWriteHandler;
	private Handler mReadHandler;
	private CountDownLatch mCountDownLatch;
	private List<Package> mPool = new ArrayList<>();

	private Listener mListener;

	public MarsServer(String host, int port) {
		mHost = host;
		mPort = port;
	}

	public void start() {
		mReadHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (mListener != null) {
					//mListener.onReceiveData(msg.what, (byte[]) msg.obj);
				}
			}
		};

		mInitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mSocket = new Socket(mHost, mPort);
					mCountDownLatch = new CountDownLatch(2);
					writeOutputStream(mSocket.getOutputStream());
					readInputStream(mSocket.getInputStream());
					mCountDownLatch.await();
					if (mListener != null) {
						mListener.onConnected();
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (mListener != null) {
						mListener.onError(e);
					}
				}
			}
		});
		mInitThread.start();
	}

	private void writeOutputStream(final OutputStream outputStream) {
		mWriteThread = new HandlerThread("MarsServer");
		mWriteThread.start();
		mWriteHandler = new Handler(mWriteThread.getLooper()) {

			@Override
			@SuppressLint("DefaultLocale")
			public void handleMessage(Message msg) {
				try {
					Iterator<Package> iterator = mPool.iterator();
					while (iterator.hasNext()) {
						iterator.next().writeData(outputStream);
						iterator.remove();
					}

					Package pkg = (Package) msg.obj;
					pkg.writeData(outputStream);
					d("type: " + pkg.getType());
				} catch (IOException e) {
					e.printStackTrace();
					if (mListener != null) {
						mListener.onError(e);
					}
				}
			}
		};
		mCountDownLatch.countDown();
	}

	private void readInputStream(final InputStream inputStream) {
		mReadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[128];
				int segmentLen = -1;
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int cachedLen = 0;
				int currentLen = 0;
				int currentType = 0;
				mCountDownLatch.countDown();
				try {
					while ((segmentLen = inputStream.read(buffer)) != -1) {
						if (segmentLen >= HEADER_LEN) {
							if (isMagicHeader(buffer)) {

								currentType = Integer.parseInt(new String(buffer, MAGIC_HEADER.length, TYPE_LEN));
								currentLen = Integer.parseInt(new String(buffer, MAGIC_HEADER.length + TYPE_LEN, PACKAGE_LEN));
								// init buffer
								byteArrayOutputStream.reset();
								cachedLen = segmentLen - HEADER_LEN;
								byteArrayOutputStream.write(buffer, HEADER_LEN, cachedLen);
								continue;
							}
						}

						byteArrayOutputStream.write(buffer, 0, segmentLen);
						cachedLen += segmentLen;
						if (cachedLen >= currentLen) {
							Message message = mReadHandler.obtainMessage();
							message.what = currentType;
							message.obj = byteArrayOutputStream.toByteArray();
							mReadHandler.sendMessage(message);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					if (mListener != null) {
						mListener.onError(e);
					}
				}
			}
		});
		mReadThread.start();
	}

	private boolean isMagicHeader(byte[] buffer) {
		for (int i = 0; i < MAGIC_HEADER.length; ++i) {
			if (buffer[i] != MAGIC_HEADER[i]) {
				return false;
			}
		}
		return true;
	}


	public void sendImage(byte[] data, int offset, int len) {
		ImagePackage imagePackage = new ImagePackage(data, offset, len);
		sendPackage(imagePackage);
	}

	public void sendWindowSize(int width, int height) {
		WindowSizePackage windowSizePackage = new WindowSizePackage(width, height);
		sendPackage(windowSizePackage);
	}

	public void sendHeartBeat() {
		HeartBeatPackage heartBeatPackage = new HeartBeatPackage();
		sendPackage(heartBeatPackage);
	}

	private void sendPackage(Package pkg) {
		if (mWriteHandler == null) {
			if (pkg.getType() == PackageType.TYPE_WINDOW_SIZE) {
				mPool.add(pkg);
			}
			return;
		}

		Message message = mWriteHandler.obtainMessage();
		message.obj = pkg;
		mWriteHandler.sendMessage(message);
	}

	public void release() {
		if (mInitThread != null) {
			mInitThread.interrupt();
			mInitThread = null;
		}

		if (mReadThread != null) {
			mReadThread.interrupt();
			mReadThread = null;
		}

		if (mWriteThread != null) {
			mWriteThread.interrupt();
			mWriteThread = null;
		}

		try {
			if (mSocket != null) {
				mSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	private static void d(String msg) {
		Log.d("MarsServer", msg);
	}

	public interface Listener {
		void onConnected();

		void onReceiveAction(@Action.ActionType int action);

		void onError(Throwable throwable);
	}
}
