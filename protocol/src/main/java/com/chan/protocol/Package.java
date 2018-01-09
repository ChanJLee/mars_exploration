package com.chan.protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan on 2018/1/8.
 */

public abstract class Package {
	private static final byte[] MAGIC_HEADER = {0x05, 0x21, 0x05, 0x25, 0x12, 0x12, 0x01, 0x18};

	private short mType;
	private int mLen;

	public Package(@PackageType.Type int type, int len) {
		mType = (short) type;
		mLen = len;
	}

	public void write(OutputStream os) {
		try {
			os.write(MAGIC_HEADER);
			os.write(mType);
			os.write(mLen);
			writeData(os);
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

	protected abstract void writeData(OutputStream outputStream) throws IOException;
}
