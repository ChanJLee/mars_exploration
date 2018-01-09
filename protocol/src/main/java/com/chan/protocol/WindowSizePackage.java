package com.chan.protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan on 2018/1/8.
 */

public class WindowSizePackage extends Package {
	private int mWidth;
	private int mHeight;

	public WindowSizePackage(int width, int height) {
		super(PackageType.TYPE_WINDOW_SIZE, 8);
		mWidth = width;
		mHeight = height;
	}

	@Override
	protected void writeData(OutputStream outputStream) throws IOException {
		outputStream.write(mWidth);
		outputStream.write(mHeight);
	}
}
