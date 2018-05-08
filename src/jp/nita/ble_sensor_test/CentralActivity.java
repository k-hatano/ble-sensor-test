package jp.nita.ble_sensor_test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class CentralActivity extends Activity {

	BluetoothManager mManager;
	BluetoothAdapter mAdapter;
	BluetoothLeScanner mScanner;
	private BluetoothGatt mBleGatt;

	Handler guiThreadHandler = new Handler();

	private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
	private final static int MESSAGE_NEW_SENDNUM = 1;

	final static int STATE_NONE = 0;
	final static int STATE_SCANNING = 1;
	final static int STATE_PAIRED = 2;
	int state;
	int rssiFilter;

	static Object bleProcess = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_central);

		this.state = STATE_NONE;

		try {
			rssiFilter = Integer.parseInt(this.getIntent().getStringExtra("rssiFilter"));
		} catch (NumberFormatException ex) {
			rssiFilter = 0;
		}

		mManager = (BluetoothManager) (this.getSystemService(Context.BLUETOOTH_SERVICE));

		mAdapter = mManager.getAdapter();

		if ((mAdapter == null) || (!mAdapter.isEnabled())) {
			this.setResult(MainActivity.RESULT_MADAPTER_IS_NULL);
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			finish();
			return;
		}

		mScanner = mAdapter.getBluetoothLeScanner();

		final CentralActivity activity = this;
		String macAddress = android.provider.Settings.Secure.getString(activity.getContentResolver(),
				"bluetooth_address");
		showToastAsync(finalActivity, "self : " + macAddress);

		this.scanNewDevice();

		this.setListeners(this);
	}

	private void setListeners(final CentralActivity activity) {
		findViewById(R.id.button_stop_scanning).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CentralActivity.this.stopScanning();
			}
		});

		findViewById(R.id.button_re_scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CentralActivity.this.scanNewDevice();
			}
		});
	}

	private void scanNewDevice() {
		this.state = STATE_SCANNING;
		this.startScanByBleScanner();
	}

	private void stopScanning() {
		if (mScanner == null) {
			showToastAsync(finalActivity, "mScanner is null");
			return;
		}
		this.state = STATE_NONE;
		mScanner.stopScan(mLeScanCallback);
		showToastAsync(finalActivity, "scanning stopped");
	}

	private void startScanByBleScanner() {
		this.state = STATE_SCANNING;
		synchronized (bleProcess) {
			mScanner.startScan(mLeScanCallback);
			showToastAsync(finalActivity, "scanning started");
		}
	}

	final ScanCallback mLeScanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			super.onScanResult(callbackType, result);
			synchronized (bleProcess) {
				if (result.getDevice() == null) {
					return;
				}
				int type = result.getDevice().getType();

				if ((type == BluetoothDevice.DEVICE_TYPE_LE || type == BluetoothDevice.DEVICE_TYPE_DUAL
						|| type == BluetoothDevice.DEVICE_TYPE_UNKNOWN)) {
					if (rssiFilter != 0 && result.getRssi() > rssiFilter) {
						String hex = "";
						StringBuilder sb = new StringBuilder();
						SparseArray<byte[]> manufacturerData = result.getScanRecord().getManufacturerSpecificData();
						for (int i = 0; i < manufacturerData.size(); i++) {
							int key = manufacturerData.keyAt(i);
							byte[] bytes = manufacturerData.get(key);
							for (byte d : bytes) {
								sb.append(String.format("%02X ", d));
							}
							sb.append("\n");
						}
						hex = sb.toString();

						showToastAsync(finalActivity,
								"found : " + result.getDevice().getAddress() + " / " + result.getDevice().getName()
										+ " (" + result.getRssi() + ") \n" + hex
										+ result.getScanRecord().getManufacturerSpecificData());

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		@Override
		public void onScanFailed(int intErrorCode) {
			super.onScanFailed(intErrorCode);

			String description = "";
			if (intErrorCode == ScanCallback.SCAN_FAILED_ALREADY_STARTED) {
				description = "SCAN_FAILED_ALREADY_STARTED";
			} else if (intErrorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
				description = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
			} else if (intErrorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED) {
				description = "SCAN_FAILED_FEATURE_UNSUPPORTED";
			} else if (intErrorCode == ScanCallback.SCAN_FAILED_INTERNAL_ERROR) {
				description = "SCAN_FAILED_INTERNAL_ERROR";
			} else {
				description = "" + intErrorCode;
			}

			showToastAsync(finalActivity, "starting failed : " + description);
		}
	};

	@Override
	protected void onDestroy() {
		if (mScanner != null) {
			mScanner.stopScan(mLeScanCallback);
		}
		if (mBleGatt != null) {
			mBleGatt.close();
			mBleGatt = null;
		}
		super.onDestroy();
	}

	final CentralActivity finalActivity = this;

	public void showToastAsync(final CentralActivity activity, final String text) {
		guiThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (CentralActivity.this != null) {
					TextView textView = ((TextView) (activity.findViewById(R.id.textview_central)));
					String newString = text + "\n" + textView.getText();
					textView.setText(newString);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.central, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
