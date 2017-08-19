package com.example.junseo.test03.arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.junseo.test03.MainActivity;
import com.example.junseo.test03.MenuActivity;
import com.example.junseo.test03.R;
import com.example.junseo.test03.STTActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Activity that pairs with bluetooth devices.
 *
 * Lists out the bluetooth devices and connects them.
 */
public class BluetoothPairActivity extends Activity {
    private static final String TAG = BluetoothPairActivity.class.getSimpleName();
    private ListView listview_devices_;
    private ListView listview_paired_;

    private Button device_refresh_;
    private Button device_stop_;
    private Set<BluetoothDevice> paired_devices_;
    private BluetoothAdapter bluetooth_;

    private ArrayAdapter listview_adapter_;
    private ArrayAdapter listview_adapter2_;
    private HashMap<String, BluetoothDevice> device_name_map_;

    final static int BLUETOOTH_REQUEST_CODE = 100;


    //Adapter
    SimpleAdapter adapterPaired;


    //list - Device 목록 저장
    private List<Map<String,String>> dataPaired;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bluetooth_pair);

        //setBackgroundColor();



        //블루투스 장치 새로운 device , 페얼이된 paired device setup view
        listview_devices_ = (ListView) findViewById(R.id.listViewBluetoothDevices);
        listview_paired_ = (ListView) findViewById(R.id.listViewBluetoothDevices2);

        //Adapter1
        dataPaired = new ArrayList<>();
        adapterPaired = new SimpleAdapter(this, dataPaired, android.R.layout.simple_list_item_2, new String[]{"name","address"}, new int[]{android.R.id.text1, android.R.id.text2});
        listview_paired_.setAdapter(adapterPaired);



        device_refresh_ = (Button) findViewById(R.id.buttonBluetoothDeviceRefresh);
        device_refresh_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateDeviceListView();
/*                device_refresh_.setVisibility(v.GONE);
                device_stop_.setVisibility(v.VISIBLE);*/
            }
        });



        device_stop_ = (Button) findViewById(R.id.buttonBluetoothStop);
        device_stop_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopDeviceListView();
  /*              device_stop_.setVisibility(v.GONE);
                device_refresh_.setVisibility(v.VISIBLE);*/
            }
        });



        bluetooth_ = BluetoothAdapter.getDefaultAdapter();

        listview_adapter_ = new ArrayAdapter(this,android.R.layout.simple_list_item_1);


        listview_devices_.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = listview_devices_.getItemAtPosition(position).toString();
                Log.d(TAG, "Connect name : " + name);
                BluetoothDevice device = device_name_map_.get(name);
                if (device == null) {
                    Toast.makeText(getApplicationContext(),"Can't find the device!",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Intent result_intent = new Intent(getApplicationContext(), STTActivity.class);
                result_intent.putExtra("device",device_name_map_.get(name));
                setResult(RESULT_OK, result_intent);
                finish();
            }
        });




        listview_devices_.setAdapter(listview_adapter_);
        listview_paired_.setAdapter(listview_adapter2_);

        device_name_map_ = new HashMap<>();

    }

    // Set gradient background color.
    private void setBackgroundColor() {
        View layout = findViewById(R.id.pairActivity);
        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] {0xFFF0FAFF,0xFFA3E0FF});
        gd.setCornerRadius(0f);
        layout.setBackground(gd);
    }

    @Override
    public void onResume() {
        super.onResume();
        UpdateDeviceListView();
//        UpdateBondedDevices();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver_);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_pair, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void ClearDeviceList() {
        listview_adapter_.clear();
        device_name_map_.clear();
    }

    public void StopDeviceListView(){
        if(!bluetooth_.isEnabled()){
            Toast.makeText(getApplicationContext(), "블루투스 검색 종료", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    public void UpdateDeviceListView() {
        if (!bluetooth_.isEnabled()) {
            Toast.makeText(getApplicationContext(),"bluetooth is not enabled",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ClearDeviceList();
//        UpdateBondedDevices();
        DiscoverDevices();
    }

    private void UpdateBondedDevices() {
        paired_devices_ = bluetooth_.getBondedDevices();
        for(BluetoothDevice device : paired_devices_) {
            Log.d(TAG, "bonded devices : " + device.getName());
            //AddDevice(device);
            listview_adapter2_.add(device);
        }

    }



    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver_ = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(TAG, "found devices : " + device.getName());
                // Add the name and address to an array adapter to show in a ListView
                AddDevice(device,null);
            }
                //블루투스 디바이스 페어링 상태 변화
            if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                    BluetoothDevice paired = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(paired.getBondState()==BluetoothDevice.BOND_BONDED){
                        //데이터 저장
                        Map map2 = new HashMap();
                        map2.put("name", paired.getName()); //device.getName() : 블루투스 디바이스의 이름
                        map2.put("address", paired.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                        dataPaired.add(map2);
                        //리스트 목록갱신
                        adapterPaired.notifyDataSetChanged();

                     /*   //검색된 목록
                        if(selectDevice != -1){
                            bluetoothDevices.remove(selectDevice);

                            dataDevice.remove(selectDevice);
                            adapterDevice.notifyDataSetChanged();
                            selectDevice = -1;
                        }*/
                    }

            }
        }
    };


    private void AddDevice(BluetoothDevice device,BluetoothDevice device2) {
        String name = device.getName() + "\n" + device.getAddress();
        if (!device_name_map_.containsKey(name)) {
            listview_adapter_.add(name);
        }
        // Even if the map already has the device, put the object again to have the new device object
        device_name_map_.put(name, device);
    }

    private void DiscoverDevices() {
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver_, filter); // Don't forget to unregister during onDestroy

        if (!bluetooth_.startDiscovery()) {
            Log.e(TAG, "startDiscovery failed");
        }
    }

    @Override
    public String toString() {
        String str = "Bluetooth pair Activit\n";
        for (BluetoothDevice device : device_name_map_.values()) {
            str += device.toString() + "\n";
        }
        return str;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case BLUETOOTH_REQUEST_CODE:
                //블루투스 활성화 승인
                if(resultCode == Activity.RESULT_OK){
                    GetListPairedDevice();
                }
                //블루투스 활성화 거절
                else{
                    Toast.makeText(this, "블루투스를 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                break;
        }
    }


    public void GetListPairedDevice(){
        Set<BluetoothDevice> paired_devices_ = bluetooth_.getBondedDevices();

        dataPaired.clear();
        if(paired_devices_.size() > 0){
            for(BluetoothDevice device : paired_devices_){
                //데이터 저장
                Map map = new HashMap();
                map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                dataPaired.add(map);
            }
        }
        //리스트 목록갱신
        adapterPaired.notifyDataSetChanged();
    }



}

