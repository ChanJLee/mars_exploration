package com.chan.protocol;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntRange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.Socket;

/**
 * Created by chan on 2018/1/4.
 */

public class PackageSender {
	private static final byte[] MAGIC_HEADER = {0x05, 0x21, 0x05, 0x25, 0x00};
	/*
	 * magic number len 5B
	 * */
	private static final int MAGIC_LEN = 5;

	/*
	 * type len, 2B
	 * */
	private static final int TYPE_LEN = 2;

	/*
	 * package len 10B
	 * */
	private static final int PACKAGE_LEN = 10;

	/*
	 * mata data len 12B
	 * */
	private static final int META_DATA_LEN = TYPE_LEN + PACKAGE_LEN;

	/*
	 * head len
	 * */
	private static final int HEADER_LEN = MAGIC_LEN + META_DATA_LEN;

	private String mHost;
	private int mPort;

	private Socket mSocket;
	private Thread mInitThread;

	private Thread mReadThread;
	private HandlerThread mWriteThread;

	private Handler mWriteHandler;
	private Handler mReadHandler;

	private Listener mListener;

	public PackageSender(String host, int port) {
		mHost = host;
		mPort = port;
	}

	void start() {
		mReadHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (mListener != null) {
					mListener.onReceiveData(msg.what, (byte[]) msg.obj);
				}
			}
		};

		mInitThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mSocket = new Socket(mHost, mPort);
					writeOutputStream(mSocket.getOutputStream());
					readInputStream(mSocket.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		mInitThread.start();
	}

	private void writeOutputStream(final OutputStream outputStream) {
		mWriteThread = new HandlerThread("PackageSender");
		mWriteThread.start();
		mWriteHandler = new Handler(mWriteThread.getLooper()) {

			@Override
			@SuppressLint("DefaultLocale")
			public void handleMessage(Message msg) {
				String type = String.format("%02d", msg.what);
				byte[] data = (byte[]) msg.obj;
				try {
					outputStream.write(MAGIC_HEADER);
					outputStream.write(type.getBytes());
					outputStream.write(String.format("%010d", data.length).getBytes());
					outputStream.write(data);
					outputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
	}

	private void readInputStream(final InputStream inputStream) {
		mReadThread = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[128];
				int segmentLen = -1;
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int cached_len = 0;
				int currentLen = 0;
				int currentType = 0;
				try {
					while ((segmentLen = inputStream.read(buffer)) != -1) {
						if (segmentLen >= HEADER_LEN) {
							if (isMagicHeader(buffer)) {

								currentType = Integer.parseInt(new String(buffer, MAGIC_LEN, TYPE_LEN));
								currentLen = Integer.parseInt(new String(buffer, MAGIC_LEN + TYPE_LEN, PACKAGE_LEN));
								// init buffer
								byteArrayOutputStream.reset();
								cached_len = segmentLen - HEADER_LEN;
								byteArrayOutputStream.write(buffer, HEADER_LEN, cached_len);
								continue;
							}
						}

						byteArrayOutputStream.write(buffer, 0, segmentLen);
						cached_len += segmentLen;
						if (cached_len >= currentLen) {
							Message message = mReadHandler.obtainMessage();
							message.what = currentType;
							message.obj = byteArrayOutputStream.toByteArray();
							mReadHandler.sendMessage(message);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					//TODO
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

	@IntRange(from = 0, to = 99)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Type {

	}

	public void sendImage(byte[] data, int offset, int len) {

	}

	public void sendWindowSize(int width, int height) {

	}

	public void write(@Type int type, byte[] data) {
		if (type < 0 || type >= 100 || data == null) {
			return;
		}

		Message message = mWriteHandler.obtainMessage();
		message.what = type;
		message.obj = data;
		mWriteHandler.sendMessage(message);
	}

	private void release() {
		mInitThread.interrupt();
		mInitThread = null;

		mReadThread.interrupt();
		mReadThread = null;

		mWriteThread.interrupt();
		mWriteThread = null;

		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setListener(Listener listener) {
		mListener = listener;
	}

	public interface Listener {
		void onReceiveData(int type, byte[] data);

		void onError(Throwable throwable);
	}
}
