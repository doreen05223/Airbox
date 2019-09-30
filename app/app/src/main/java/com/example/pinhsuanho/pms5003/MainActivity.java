package com.example.pinhsuanho.pms5003;

import android.Manifest;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // GUI Components
    private TextView mdate;
    private TextView mBluetoothStatus;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;
    private ListView mDevicesListView;

    private Button mSearch;
    private Button pm1Chart;
    private Button pm25Chart;
    private Button pm10Chart;
    private Button temperatureChart;
    private Button humidityChart;
    private Button co2Chart;
    private Button avgPM25Chart;
    private Button avgCO2Chart;
    private  Button mStart;
    private  Button mClose;
    private Button mNow;
    private TextView pm1Value;
    private TextView pm25Value;
    private TextView pm10Value;
    private  TextView temperatureValue;
    private  TextView humidityValue;
    private  TextView co2Value;
    private TextView mState;
    private TextView sDate;

    private Handler mHandler;   // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread;   // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null;   // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString
            ("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1;     // used to identify adding bluetooth names
    private final static int MESSAGE_READ = 2;           // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3;     // used in bluetooth handler to identify message status
    private String _recieveData = "";
    private String time;
    private String currentTime;
    private int mYear, mMonth, mDay;
    private String y, m, d;

    // used in database
    private static String DATABASE_TABLE = "datas";
    private SQLiteDatabase db;
    private StdDBHelper dbHelper;

    //draw chartline
    LineChart mChart;
    ArrayList<Entry> entries_pm1 = new ArrayList<Entry>();
    ArrayList<Entry> entries_pm25 = new ArrayList<Entry>();
    ArrayList<Entry> entries_pm10 = new ArrayList<Entry>();
    ArrayList<Entry> entries_temperature = new ArrayList<Entry>();
    ArrayList<Entry> entries_humidity = new ArrayList<Entry>();
    ArrayList<Entry> entries_co2 = new ArrayList<Entry>();
    ArrayList<Entry> entries_avgPM25 = new ArrayList<Entry>();
    ArrayList<Entry> entries_avgCO2 = new ArrayList<Entry>();

    // send request every 5 seconds
    Runnable samRun=new Runnable() {
        @Override
        public void run() {
//            if(mConnectedThread != null) //First check to make sure thread created
//                mConnectedThread.write("send");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
                Date curDate = new Date(System.currentTimeMillis()) ; // 獲取當前時間
                time = formatter.format(curDate);
            mHandler.postDelayed(this,5000);
        }
    };

    //show current time every second
    Runnable showTime=new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日\tHH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis()) ; // 獲取當前時間
            currentTime = formatter.format(curDate);
            mdate.setText(currentTime);
            mHandler.postDelayed(this,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 建立SQLiteOpenHelper物件
        dbHelper = new StdDBHelper(this);
        db = dbHelper.getWritableDatabase(); // 開啟資料庫

        //初始化元件
        mdate = (TextView) findViewById(R.id.date);
        mBluetoothStatus = (TextView)findViewById(R.id.bluetoothStatus);
        mListPairedDevicesBtn = (Button)findViewById(R.id.PairedBtn);
        mDiscoverBtn = (Button)findViewById(R.id.discoverbtn);
        mSearch = (Button)findViewById(R.id.search);
        pm1Chart = (Button)findViewById(R.id.pmOne);
        pm25Chart = (Button)findViewById(R.id.pm25);
        pm10Chart = (Button)findViewById(R.id.pmTen);
        temperatureChart = (Button)findViewById(R.id.temperature);
        humidityChart = (Button)findViewById(R.id.humidity);
        co2Chart = (Button)findViewById(R.id.CO2);
        avgPM25Chart = (Button)findViewById(R.id.avgPM25);
        avgCO2Chart = (Button)findViewById(R.id.avgCO2);
        mStart = (Button)findViewById(R.id.start);
        mClose = (Button)findViewById(R.id.close);
        mNow = (Button)findViewById(R.id.nowbutton);
        pm1Value = (TextView) findViewById(R.id.pm1Num);
        pm25Value = (TextView) findViewById(R.id.pm25Num);
        pm10Value = (TextView) findViewById(R.id.pm10Num);
        temperatureValue = (TextView) findViewById(R.id.temperatureNum);
        humidityValue = (TextView) findViewById(R.id.humidityNum);
        co2Value = (TextView) findViewById(R.id.CO2Num);
        mState = (TextView) findViewById(R.id.state);
        sDate = (TextView) findViewById(R.id.searchDate);
        mChart = (LineChart) findViewById(R.id.lineChart);

        mBTArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        // get a handle on the bluetooth radio

        mDevicesListView = (ListView)findViewById(R.id.discover);
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);


        // 詢問藍芽裝置權限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        //定義執行緒 當收到不同的指令做對應的內容
        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){ //收到MESSAGE_READ 開始接收資料
                    _recieveData = ""; //清除上次收到的資料
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                        //readMessage =  readMessage.substring(0,1);
                        //取得傳過來字串的第一個字元，其餘為雜訊
                        _recieveData += readMessage; //拼湊每次收到的字元成字串
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    String[] separated =  _recieveData.split(" ");
                    if(separated[0].toString().equals("V"))
                        mState.setText("V");
                    else if(separated[0].toString().equals("S")) {
                        mState.setText("S");
                    }
                    else {
                        pm1Value.setText(separated[1].toString() + "(ug/m3)");
                        pm25Value.setText(separated[2].toString() + "(ug/m3)");
                        pm10Value.setText(separated[3].toString() + "(ug/m3)");
                        temperatureValue.setText(separated[4].toString());
                        humidityValue.setText(separated[5].toString());
                        co2Value.setText(separated[6].toString() + "ppm");
                    }

                    /****************  store data in database  ****************************/
                    ContentValues cv = new ContentValues();
                    if(!separated[0].toString().equals("V") && !separated[0].toString().equals("S") && !separated[0].toString().equals("N")) {
                        cv.put("time", time.toString());
                        cv.put("pm1", separated[1].toString());
                        cv.put("pm25", separated[2].toString());
                        cv.put("pm10", separated[3].toString());
                        cv.put("temperature", separated[4].toString());
                        cv.put("humidity", separated[5].toString());
                        cv.put("co2", separated[6].toString());
                        //cv.put("avgPM25", separated[7].toString());
                        //cv.put("avgCO2", separated[8].toString());
                        db.insert(DATABASE_TABLE, null, cv);
                    }
                    /***************************************************************/
                }

                if(msg.what == CONNECTING_STATUS){
                    //收到CONNECTING_STATUS 顯示以下訊息
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: "
                                + (String)(msg.obj));
                    else
                        mBluetoothStatus.setText("Connection Failed");
                }
            }
        };


        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {
            mSearch.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);
                    new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            sDate.setText(year+"/"+(month+1)+"/"+day);
                            y = Integer.toString(year);
                            m = Integer.toString(month+1);
                            if(m.length() == 1) m = "0" + m;
                            d = Integer.toString(day);
                            if(d.length() == 1) d = "0" + d;
                        }
                    }, mYear,mMonth, mDay).show();
                }
            });

            mStart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null) //First check to make sure thread created
                        //mConnectedThread.write("start");
                        mConnectedThread.write(time);
                }
            });

            mClose.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("close");
                }
            });

            mNow.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(mConnectedThread != null) //First check to make sure thread created
                        mConnectedThread.write("now");
                }
            });

            pm1Chart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet = new LineDataSet(entries_pm1, "PM1");
                    dataSet.setColor(Color.BLUE);
                    dataSet.setCircleRadius(1f);

                    LineDataSet dataSet2 = new LineDataSet(entries_pm25, "PM2.5");
                    dataSet2.setColor(Color.GREEN);
                    dataSet2.setCircleRadius(1f);

                    LineDataSet dataSet3 = new LineDataSet(entries_pm10, "PM10");
                    dataSet3.setColor(Color.RED);
                    dataSet3.setCircleRadius(1f);

                    ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                    dataSets.add(dataSet);
                    dataSets.add(dataSet2);
                    dataSets.add(dataSet3);
                    LineData lineData = new LineData(dataSets);
                    mChart.setData(lineData);
                    mChart.invalidate();
                }
            });

            pm25Chart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet2 = new LineDataSet(entries_pm25, "PM2.5");
                    dataSet2.setColor(Color.GREEN);
                    dataSet2.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets2 = new ArrayList<>();
                    dataSets2.add(dataSet2);
                    LineData lineData2 = new LineData(dataSets2);
                    mChart.setData(lineData2);
                    mChart.invalidate();
                }
            });

            pm10Chart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet3 = new LineDataSet(entries_pm10, "PM10");
                    dataSet3.setColor(Color.RED);
                    dataSet3.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets3 = new ArrayList<>();
                    dataSets3.add(dataSet3);
                    LineData lineData3 = new LineData(dataSets3);
                    mChart.setData(lineData3);
                    mChart.invalidate();
                }
            });

            temperatureChart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet4 = new LineDataSet(entries_temperature, "TEMPERATURE");
                    dataSet4.setColor(Color.RED);
                    dataSet4.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets4 = new ArrayList<>();
                    dataSets4.add(dataSet4);
                    LineData lineData4 = new LineData(dataSets4);
                    mChart.setData(lineData4);
                    mChart.invalidate();
                }
            });

            humidityChart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet5 = new LineDataSet(entries_humidity, "HUMIDITY");
                    dataSet5.setColor(Color.RED);
                    dataSet5.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets5 = new ArrayList<>();
                    dataSets5.add(dataSet5);
                    LineData lineData5 = new LineData(dataSets5);
                    mChart.setData(lineData5);
                    mChart.invalidate();
                }
            });

            co2Chart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet6 = new LineDataSet(entries_co2, "CO2");
                    dataSet6.setColor(Color.RED);
                    dataSet6.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets6 = new ArrayList<>();
                    dataSets6.add(dataSet6);
                    LineData lineData6 = new LineData(dataSets6);
                    mChart.setData(lineData6);
                    mChart.invalidate();
                }
            });

            avgPM25Chart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet6 = new LineDataSet(entries_avgPM25, "avgPM2.5");
                    dataSet6.setColor(Color.RED);
                    dataSet6.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets6 = new ArrayList<>();
                    dataSets6.add(dataSet6);
                    LineData lineData6 = new LineData(dataSets6);
                    mChart.setData(lineData6);
                    mChart.invalidate();
                }
            });

            avgCO2Chart.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    SqlQuery("SELECT * FROM " + DATABASE_TABLE, y, m, d);
                    LineDataSet dataSet6 = new LineDataSet(entries_avgCO2, "avgCO2");
                    dataSet6.setColor(Color.RED);
                    dataSet6.setCircleRadius(1f);
                    ArrayList<ILineDataSet> dataSets6 = new ArrayList<>();
                    dataSets6.add(dataSet6);
                    LineData lineData6 = new LineData(dataSets6);
                    mChart.setData(lineData6);
                    mChart.invalidate();
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    listPairedDevices(v);
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover(v);
                }
            });
        }
        mHandler.post(samRun);
        mHandler.post(showTime);
    }


    /*********************  database  ********************************/
    @Override
    protected void onStop() {
        super.onStop();
        db.close(); // 關閉資料庫
    }

    // 執行SQL查詢
    public void SqlQuery(String sql, String y, String m, String d) {
        Cursor c = db.rawQuery(sql, null);
        //int colNum = c.getColumnCount();
        c.moveToFirst();  // 第1筆
        // 顯示欄位值
        int x=0;
        for (int i = 0; i < c.getCount(); i++) {
            String[] getTime =  c.getString(0).split("/");
            if(getTime[0].equals(y) && getTime[1].equals(m) && getTime[2].equals(d)) {
                entries_pm1.add(new Entry(x, Integer.parseInt(c.getString(1))));
                entries_pm25.add(new Entry(x, Integer.parseInt(c.getString(2))));
                entries_pm10.add(new Entry(x, Integer.parseInt(c.getString(3))));
                entries_temperature.add(new Entry(x, Float.parseFloat(c.getString(4))));
                entries_humidity.add(new Entry(x, Float.parseFloat(c.getString(5))));
                entries_co2.add(new Entry(x, Integer.parseInt(c.getString(6))));
                x++;
            }
            c.moveToNext();  // 下一筆
        }
    }
    /**************************************************************/

    private void discover(View view){
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){ //如果已經找到裝置
            mBTAdapter.cancelDiscovery(); //取消尋找
            Toast.makeText(getApplicationContext(),"Discovery stopped",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) { //如果沒找到裝置且已按下尋找
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery(); //開始尋找
                Toast.makeText(getApplicationContext(), "Discovery started",
                        Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new
                        IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "Bluetooth not on",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(View view){
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "Show Paired Devices",
                    Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "Bluetooth not on",
                    Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new
            AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

                    if(!mBTAdapter.isEnabled()) {
                        Toast.makeText(getBaseContext(), "Bluetooth not on",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mBluetoothStatus.setText("Connecting...");
                    // Get the device MAC address, which is the last 17 chars in the View
                    String info = ((TextView) v).getText().toString();
                    final String address = info.substring(info.length() - 17);
                    final String name = info.substring(0,info.length() - 17);

                    // Spawn a new thread to avoid blocking the GUI one
                    new Thread()
                    {
                        public void run() {
                            boolean fail = false;
                            //取得裝置MAC找到連接的藍芽裝置
                            BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                            try {
                                mBTSocket = createBluetoothSocket(device);
                                //建立藍芽socket
                            } catch (IOException e) {
                                fail = true;
                                Toast.makeText(getBaseContext(), "Socket creation failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                            // Establish the Bluetooth socket connection.
                            try {
                                mBTSocket.connect(); //建立藍芽連線
                            } catch (IOException e) {
                                try {
                                    fail = true;
                                    mBTSocket.close(); //關閉socket
                                    //開啟執行緒 顯示訊息
                                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                            .sendToTarget();
                                } catch (IOException e2) {
                                    //insert code to deal with this
                                    Toast.makeText(getBaseContext(), "Socket creation failed",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                            if(fail == false) {
                                //開啟執行緒用於傳輸及接收資料
                                mConnectedThread = new ConnectedThread(mBTSocket);
                                mConnectedThread.start();
                                //開啟新執行緒顯示連接裝置名稱
                                mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                        .sendToTarget();
                            }
                        }
                    }.start();
                }
            };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws
            IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.available();
                    if(bytes != 0) {
                        SystemClock.sleep(100);
                        //pause and wait for rest of data
                        bytes = mmInStream.available();
                        // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes);
                        // record how many bytes we actually read
                        mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget(); // Send the obtained bytes to the UI activity
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}