package com.chan.protocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() throws Exception {
		MarsServer marsServer = new MarsServer("192.168.0.102", 8765);
		marsServer.sendHeartBeat();
		marsServer.sendImage(new byte[1], 0, 1);
		marsServer.sendWindowSize(100, 200);
		marsServer.start();
	}
}