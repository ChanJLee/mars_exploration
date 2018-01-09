package com.chan.protocol;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by chan on 2018/1/9.
 */

public class HeartBeatPackage extends Package {

	public HeartBeatPackage() {
		super(PackageType.TYPE_HEART_BEAT, 0);
	}

	@Override
	protected void writeData(OutputStream outputStream) throws IOException {
		//NOTHING
	}
}
