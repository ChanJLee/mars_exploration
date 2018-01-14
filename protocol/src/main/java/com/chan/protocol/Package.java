package com.chan.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan on 2018/1/8.
 */

public abstract class Package {
	private static final byte[] MAGIC_HEADER = {0x05, 0x21, 0x05, 0x25, 0x12, 0x12, 0x01, 0x18};
	private static final byte[] PADDING = {0x00, 0x00};
	private short mType;
	private int mLen;
	private ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();

	public Package(@PackageType.Type int type, int len) {
		mType = (short) type;
		mLen = len;
	}

	final public void write(OutputStream os) {
		try {
			mByteArrayOutputStream.reset();
			mByteArrayOutputStream.write(MAGIC_HEADER);
			mByteArrayOutputStream.write(short2Bytes(mType));
			mByteArrayOutputStream.write(PADDING);
			mByteArrayOutputStream.write(int2Bytes(mLen));
			writeData(mByteArrayOutputStream);
			os.write(mByteArrayOutputStream.toByteArray());
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public short getType() {
		return mType;
	}

	public int getLen() {
		return mLen;
	}

	private byte[] short2Bytes(short value) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value >> 8) & 0xff);
		return bytes;
	}

	private byte[] int2Bytes(int value) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (value & 0xff);
		bytes[1] = (byte) ((value >> 8) & 0xff);
		bytes[2] = (byte) ((value >> 16) & 0xff);
		bytes[3] = (byte) ((value >> 24) & 0xff);
		return bytes;
	}

	protected abstract void writeData(OutputStream outputStream) throws IOException;
}
