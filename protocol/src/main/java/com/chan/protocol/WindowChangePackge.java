package com.chan.protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan on 2018/1/8.
 */

public class WindowChangePackge extends Package {
	private int mWidth;
	private int mHeight;

	public WindowChangePackge(int width, int height) {
		super((short) 2, 8);
		mWidth = width;
		mHeight = height;
	}

	@Override
	protected void writeData(OutputStream outputStream) throws IOException {
		outputStream.write(mWidth);
		outputStream.write(mHeight);
	}
}
