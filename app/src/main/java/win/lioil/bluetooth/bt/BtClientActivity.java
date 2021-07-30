package win.lioil.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.adapter.InfoAdapter;
import win.lioil.bluetooth.bean.Student;
import win.lioil.bluetooth.util.BtReceiver;

public class BtClientActivity extends Activity implements BtBase.Listener, BtReceiver.Listener, BtDevAdapter.Listener {
    private TextView mTips;              //在BtClientActivity实现BtBase.Listener, BtReceiver.Listener, BtDevAdapter.Listener这三个接口中的方法
    private EditText mInputStuNum;
    private EditText mInputName;
    private EditText mInputFile;
    private TextView mLogs;
    private InfoAdapter infoAdapter;
    private RecyclerView recyclerView;
    private BtReceiver mBtReceiver;
    private final BtDevAdapter mBtDevAdapter = new BtDevAdapter(this);
    private final BtClient mClient = new BtClient(this);
    private String message=" ";
    List<Student> stuList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btclient);



        mTips = findViewById(R.id.tv_tips);
        mInputStuNum = findViewById(R.id.input_num);                          //绑定控件
        mInputName = findViewById(R.id.input_name);                          //绑定控件
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        mBtReceiver = new BtReceiver(this, this);         //注册蓝牙广播
        BluetoothAdapter.getDefaultAdapter().startDiscovery();           //开始搜索附近蓝牙设备

        RecyclerView rv = findViewById(R.id.rv_bt);
        rv.setLayoutManager(new LinearLayoutManager(this));          //设置布局管理器
        rv.setAdapter(mBtDevAdapter);

        recyclerView = findViewById(R.id.lv_info);
        infoAdapter = new InfoAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(infoAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        mClient.unListener();
        mClient.close();
    }

    @Override
    public void onItemClick(BluetoothDevice dev) {
        if (mClient.isConnected(dev)) {
            APP.toast("已经连接了", 0);      //连接成功时显示已经连接了
            return;
        }
        mClient.connect(dev);
        APP.toast("正在连接...", 0);        //点击扫描设备时显示正在连接...
        mTips.setText("正在连接...");
    }

    @Override
    public void foundDev(BluetoothDevice dev) {
        mBtDevAdapter.add(dev);
    }

    // 重新扫描
    public void reScan(View view) {
        mBtDevAdapter.reScan();
    }

    public void sendMsg(View view) {
        if (mClient.isConnected(null)) {
            String msg = mInputStuNum.getText().toString();
            String msg1 = mInputName.getText().toString();
            if (TextUtils.isEmpty(msg))
                APP.toast("输入不能空", 0);
            else
                mClient.sendMsg(listToString(),"  ");                    //调用的是BtBase里的sendMsg
//            String msg1 = mInputName.getText().toString();
//            if (TextUtils.isEmpty(msg1))
//                APP.toast("输入不能空", 0);
//            else
//                mClient.sendMsg(msg1);               //调用的是BtBase里的sendMsg
        } else
            APP.toast("没有连接", 0);
    }

    public void sendMsg1(View view) {
        if (mClient.isConnected(null)) {
            String msg = mInputStuNum.getText().toString();
            String msg1 = mInputName.getText().toString();
            if (TextUtils.isEmpty(msg))
                APP.toast("输入不能空", 0);
            else
                //mLogs.append(msg+' ');

            //message=message+msg+msg1;
            if (TextUtils.isEmpty(msg1))
                APP.toast("输入不能空", 0);
            else
                //mLogs.append(msg1);
                //mLogs.append(System.getProperty("line.separator"));
                importStudentInfo(msg1,msg,0);

             //   mClient.sendMsg(msg);
        } else
            APP.toast("没有连接", 0);
    }

    public  String listToString(){

        Gson mGson = new Gson();
        return mGson.toJson(stuList);

    }

    public void importStudentInfo(String name,String id,int grade){
        Student person = new Student();
        person.setName(name);
        person.setId(id);
        person.setGrade("0");
        stuList.add(person);

        infoAdapter.add(person);

    }

    public void sendFile(View view) {
        if (mClient.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile())
                APP.toast("文件无效", 0);
            else
                mClient.sendFile(filePath);
        } else
            APP.toast("没有连接", 0);         //未连接时点击sendMsg显示没有连接
    }

    @Override
    public void socketNotify(int state, final Object obj) {
        if (isDestroyed())
            return;
        String msg = null;
        switch (state) {
            case BtBase.Listener.CONNECTED:
                BluetoothDevice dev = (BluetoothDevice) obj;
                msg = String.format("与%s(%s)连接成功", dev.getName(), dev.getAddress());
                mTips.setText(msg);
                break;
            case BtBase.Listener.DISCONNECTED:
                msg = "连接断开";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:
                msg = String.format("\n%s", obj);
                //mLogs.append(msg);
                infoAdapter = new InfoAdapter(BtBase.stringToList(BtBase.jsonMsg));
                recyclerView.setAdapter(infoAdapter);
                break;
        }
        APP.toast(msg, 0);
    }
}