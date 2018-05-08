package jp.nita.ble_sensor_test;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final int REQUEST_CENTRAL = 1;
	public static final int REQUEST_SETTINGS = 2;
	public static final int RESULT_OK = 0;
	public static final int RESULT_MADAPTER_IS_NULL = 1;
	public static final int RESULT_MADVERTISER_IS_NULL = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.button_central).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String rssiFilter = ((EditText)(findViewById(R.id.textedit_rssi_filter))).getText().toString();

				Intent intent = new Intent(MainActivity.this, CentralActivity.class);
				intent.putExtra("rssiFilter", rssiFilter);
				startActivityForResult(intent, REQUEST_CENTRAL);
			}

		});

		BluetoothManager bluetoothManager = (BluetoothManager) (this.getSystemService(Context.BLUETOOTH_SERVICE));
		BluetoothAdapter adapter = bluetoothManager.getAdapter();

		if ((adapter == null) || (!adapter.isEnabled())) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(enableBtIntent);
			return;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CENTRAL:
			if (resultCode == RESULT_MADAPTER_IS_NULL) {
				Toast.makeText(this, "mAdapter is null", Toast.LENGTH_SHORT).show();
				break;
			}
			if (resultCode == RESULT_MADVERTISER_IS_NULL) {
				Toast.makeText(this, "mAdvertiser is null", Toast.LENGTH_SHORT).show();
				break;
			}
			break;
		default:
			break;
		}
	}

}