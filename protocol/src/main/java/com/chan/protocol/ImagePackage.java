package com.chan.protocol;


import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan on 2018/1/8.
 */

public class ImagePackage extends Package {

	private byte[] mData;
	private int mOffset;
	private int mLen;

	ImagePackage(byte[] data, int offset, int len) {
		super(PackageType.TYPE_IMAGE, len - offset);
		mData = data;
		mOffset = offset;
		mLen = len;
	}

	@Override
	protected void writeData(OutputStream outputStream) throws IOException {
		outputStream.write(mData, mOffset, mLen);
	}
}
