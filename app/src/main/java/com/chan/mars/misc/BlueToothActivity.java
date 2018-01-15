package com.chan.mars.misc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.chan.mars.R;

public class BlueToothActivity extends AppCompatActivity {
	private BluetoothAdapter mBluetoothAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blue_tooth);

		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		if (bluetoothManager == null) {
			d("manager is null");
			return;
		}

		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			d("adapter is null");
			return;
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(enableBtIntent);
		}

		IntentFilter intent = new IntentFilter();
		intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
		intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
		intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
		intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
		registerReceiver(mSearchDevices, intent);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mSearchDevices);
		if (mBluetoothAdapter != null)
			mBluetoothAdapter.cancelDiscovery();
		super.onDestroy();
	}

	private BroadcastReceiver mSearchDevices = new BroadcastReceiver() {
		//接收
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle b = intent.getExtras();
			Object[] lstName = b.keySet().toArray();

			// 显示所有收到的消息及其细节
			for (int i = 0; i < lstName.length; i++) {
				String keyName = lstName[i].toString();
				d("detail: " + keyName + " : " + String.valueOf(b.get(keyName)));
			}
			BluetoothDevice device;
			// 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				d("find device: " + device);
			}
			//状态改变时
			else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				switch (device.getBondState()) {
					case BluetoothDevice.BOND_BONDING://正在配对
						d("正在配对......");
						break;
					case BluetoothDevice.BOND_BONDED://配对结束
						d("完成配对");
						break;
					case BluetoothDevice.BOND_NONE://取消配对/未配对
						d("取消配对");
					default:
						break;
				}
			}
		}
	};

	private static void d(String msg) {
		Log.d("BlueToothActivity", msg);
	}

	public static Intent createIntent(Context context) {
		return new Intent(context, BlueToothActivity.class);
	}
}
