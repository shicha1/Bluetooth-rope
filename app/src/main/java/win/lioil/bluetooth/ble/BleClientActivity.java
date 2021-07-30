package win.lioil.bluetooth.ble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.adapter.InfoAdapter;
import win.lioil.bluetooth.bean.Student;

import static java.lang.Thread.sleep;

/**
 * BLE客户端(主机/中心设备/Central)
 */
public class BleClientActivity extends Activity {
    private static final String TAG = BleClientActivity.class.getSimpleName();

    private EditText mEdtName;
    private EditText mEdtId;
    private EditText mEdtGender;
    private EditText mEdtHandlerId;
    private EditText mEdtLedId;
    private TextView mTips;
    private BleDevAdapter mBleDevAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean isConnected = false;
    private byte[] writeByteArray;//发送的字节串
    private String recieveStr ="";//接受到的字符串
    private List<Student> stuList = new ArrayList<>();
    private boolean isSendingInfo = false;//判断是否在发送
    private boolean isReadingGrade = false;//判断是否在读取

    private InfoAdapter infoAdapter;
    private RecyclerView rv_2;

    private String[] uuid = new String[4];
    public static  UUID UUID_SERVICE ;
    public static  UUID UUID_CHAR_READ_NOTIFY;
    public static  UUID UUID_DESC_NOTITY;
    public static  UUID UUID_CHAR_WRITE;

    private static Toast toast;

    private void setUUID(){
        UUID_SERVICE = UUID.fromString(uuid[0]); //自定义UUID
        UUID_CHAR_READ_NOTIFY = UUID.fromString(uuid[1]);
        UUID_DESC_NOTITY = UUID.fromString(uuid[2]);
        UUID_CHAR_WRITE = UUID.fromString(uuid[3]);

    }


    // 与服务端连接的Callback
    public BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BluetoothDevice dev = gatt.getDevice();
            Log.i(TAG, String.format("onConnectionStateChange:%s,%s,%s,%s", dev.getName(), dev.getAddress(), status, newState));
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                isConnected = true;
                gatt.discoverServices(); //启动服务发现
            } else {
                isConnected = false;
                closeConn();
            }
            logTv(String.format(status == 0 ? (newState == 2 ? "与[%s]连接成功" : "与[%s]连接断开") : ("与[%s]连接出错,错误码:" + status), dev));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(TAG, String.format("onServicesDiscovered:%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), status));
            if (status == BluetoothGatt.GATT_SUCCESS) { //BLE服务发现成功
                // 遍历获取BLE服务Services/Characteristics/Descriptors的全部UUID
                int i =0;
                int size = gatt.getServices().size();
                for (BluetoothGattService service : gatt.getServices()) {


                    StringBuilder allUUIDs = new StringBuilder(service.getUuid().toString());
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        allUUIDs.append(",").append(characteristic.getUuid());
                        for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors())
                            allUUIDs.append(",").append(descriptor.getUuid());
                    }

                    Log.i(TAG, "onServicesDiscovered:" + allUUIDs.toString());
                    //logTv("发现服务" + allUUIDs);
                    i++;
                    if(i==size){
                        uuid = allUUIDs.toString().split(",");
                        setUUID();
                    }


                     setNotify();


                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();

            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("收到消息:\n" + valueStr);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            UUID uuid = characteristic.getUuid();
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            logTv("发送成功");
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            UUID uuid = characteristic.getUuid();
            recieveStr = new String(characteristic.getValue());
            String valueStr = new String(characteristic.getValue());
            Log.i(TAG, String.format("onCharacteristicChanged:%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr));
            //logTv("收到回复：" + valueStr);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            Log.i(TAG, String.format("onDescriptorRead:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            //logTv("读取Descriptor[" + uuid + "]:\n" + valueStr);
            logTv(valueStr);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            UUID uuid = descriptor.getUuid();
            String valueStr = Arrays.toString(descriptor.getValue());
            Log.i(TAG, String.format("onDescriptorWrite:%s,%s,%s,%s,%s", gatt.getDevice().getName(), gatt.getDevice().getAddress(), uuid, valueStr, status));
            //logTv("发送消息:\n" + valueStr);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleclient);

        mEdtName = findViewById(R.id.edt_name);
        mEdtId = findViewById(R.id.edt_id);
        mEdtGender = findViewById(R.id.edt_gender);
        mEdtHandlerId = findViewById(R.id.edt_handler_id);
        mEdtLedId = findViewById(R.id.edt_led_id);
        mEdtLedId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>before){
                    String ss = s.toString();
                    String str = s.toString();
                    if(s.charAt(s.length()-1)!='.'){
                        ss=ss.replace(".","");
                        int a = ss.length();
                        if(a>=3){
                            if(a%3==0){
                                str = s.toString()+".";
                                mEdtLedId.setText(str);
                            }
                        }
                    }


                }

                mEdtLedId.setSelection(mEdtLedId.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEdtHandlerId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count>before){
                    String ss = s.toString();
                    String str = s.toString();
                    if(s.charAt(s.length()-1)!='.'){
                        ss=ss.replace(".","");
                        int a = ss.length();
                        if(a>=3){
                            if(a%3==0){
                                str = s.toString()+".";
                                mEdtHandlerId.setText(str);
                            }
                        }
                    }


                }

                mEdtHandlerId.setSelection(mEdtHandlerId.getText().length());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTips = findViewById(R.id.tv_tips);
        infoAdapter = new InfoAdapter();

        RecyclerView rv = findViewById(R.id.rv_ble);
        RecyclerView rv_2 = findViewById(R.id.rv_ble_2);
        rv_2.setLayoutManager(new LinearLayoutManager(this));
        rv_2.setAdapter(infoAdapter);



        rv.setLayoutManager(new LinearLayoutManager(this));
        mBleDevAdapter = new BleDevAdapter(new BleDevAdapter.Listener() {
            @Override
            public void onItemClick(BluetoothDevice dev) {
                closeConn();
                mBluetoothGatt = dev.connectGatt(BleClientActivity.this, false, mBluetoothGattCallback); // 连接蓝牙设备
                logTv(String.format("与[%s]开始连接............", dev));
            }
        });
        rv.setAdapter(mBleDevAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeConn();
    }

    // BLE中心设备连接外围设备的数量有限(大概2~7个)，在建立新连接之前必须释放旧连接资源，否则容易出现连接错误133
    private void closeConn() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
    }

    // 扫描BLE
    public void reScan(View view) {
        if (mBleDevAdapter.isScanning)
//            APP.toast("正在扫描...", 0);
        Toast.makeText(this,"正在扫描",Toast.LENGTH_SHORT).show();
        else
            mBleDevAdapter.reScan();
    }

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 读取数据成功会回调->onCharacteristicChanged()
    /*public void read(View view) {
        BluetoothGattService service = getGattService(UUID_SERVICE);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_READ_NOTIFY);//通过UUID获取可读的Characteristic
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }*/

    // 注意：连续频繁读写数据容易失败，读写操作间隔最好200ms以上，或等待上次回调完成后再进行下次读写操作！
    // 写入数据成功会回调->onCharacteristicWrite()
    public void write() {
        BluetoothGattService service = getGattService(UUID_SERVICE);
        if (service != null) {

            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_WRITE);//通过UUID获取可写的Characteristic
            characteristic.setValue(writeByteArray); //单次最多20个字节
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    // 设置通知Characteristic变化会回调->onCharacteristicChanged()
    public void setNotify() {
        BluetoothGattService service = getGattService(UUID_SERVICE);
        if (service != null) {
            // 设置Characteristic通知
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHAR_READ_NOTIFY);//通过UUID获取可通知的Characteristic
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);

            // 向Characteristic的Descriptor属性写入通知开关，使蓝牙设备主动向手机发送数据
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_DESC_NOTITY);
            // descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);//和通知类似,但服务端不主动发数据,只指示客户端读取数据
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    // 获取Gatt服务
    private BluetoothGattService getGattService(UUID uuid) {
        if (!isConnected) {
//            APP.toast("没有连接", 0);
  //          show(BleClientActivity.this,"没有连接");
            return null;
        }
        BluetoothGattService service = mBluetoothGatt.getService(uuid);
        if (service == null){}
//            APP.toast("没有找到服务UUID=" + uuid, 0);
            //Toast.makeText(this,"没有找到服务UUID=" + uuid,Toast.LENGTH_SHORT).show();
        return service;
    }

    // 输出日志
    private void logTv(final String msg) {
        if (isDestroyed())
            return;
        runOnUiThread(new Runnable() {
            @SuppressLint("WrongConstant")
            @Override
            public void run() {

//                APP.toast(msg, 0);
//                Toast.makeText(BleClientActivity.this,msg,0).show();
                //show(BleClientActivity.this,msg);
                mTips.append(msg + "\n\n");
            }
        });
    }

    private boolean checkEdit(){
        if(mEdtName.getText().toString().trim().equals("")||mEdtId.getText().toString().trim().equals("")
                ||mEdtGender.getText().toString().trim().equals("")||mEdtHandlerId.getText().toString().trim().equals("")
                )return false;
        else return true;
    }

    private void clearEdit(){
        mEdtName.setText("");
        mEdtId.setText("");
        mEdtGender.setText("");
        mEdtHandlerId.setText("");
        mEdtLedId.setText("");

    }

    public void importInfo(View view){
        if(stuList.size()>=20)Toast.makeText(this,"导入最多20条！",Toast.LENGTH_SHORT).show();
        //else if(!checkEdit()) Toast.makeText(this,"输入不能为空",Toast.LENGTH_SHORT).show();
        else {
            Student student = new Student();
            student.setLedId((mEdtLedId.getText().toString().replace(".","")));

            if(student.getLedId().equals("")) {
                student.setName(mEdtName.getText().toString());
                student.setId(mEdtId.getText().toString());
                student.setGender(mEdtGender.getText().toString());
                student.setHandlerId((mEdtHandlerId.getText().toString().replace(".","")));
                stuList.add(student);
                infoAdapter.add(student);
                logTv("导入学生："+student.toString());
            }
            else{
                student.setHandlerId("");
                student.setGender("");
                stuList.add(student);
                infoAdapter.add(student);
                logTv("导入LED："+student.toString1());
            }
        }

    }

    public void sendInfo(View view){
            if(isSendingInfo)Toast.makeText(this,"正在发送数据请不要重复点击！",Toast.LENGTH_SHORT).show();
            else if(isReadingGrade){
                Toast.makeText(this,"正在读取成绩请不要重复点击！",Toast.LENGTH_SHORT).show();
            }else if(stuList==null||stuList.size()==0){
                Toast.makeText(this,"信息为空，请先导入！",Toast.LENGTH_SHORT).show();
            }
            else{
                isSendingInfo = true;

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        boolean flag = true;
                        for (int i =0;i<stuList.size();i++){
                            String id = stuList.get(i).getId();
                            StringBuilder totalSendStr = new StringBuilder();
                            if(stuList.get(i).getLedId().equals("")){
                                totalSendStr.append(stuList.get(i).toString());
                            }
                            else{
                                totalSendStr.append(stuList.get(i).toString1());
                            }
                            byte[] sendByte = totalSendStr.toString().getBytes();
                            int length = sendByte.length;
                            int start = 0;
                            byte[] send = new byte[20];
                            //send[0]='S';
                            //send[19]='E';
                            //发送成功标识

                            //每20字节一个包
                            int barnum=0;
                            logTv("正在发送第"+(i+1)+"条信息：");

                            while (length>0){
                                System.arraycopy(sendByte,start,send,0,length>20?20:length);
                                length-=20;
                                start+=20;

                                if (length<0){
                                    byte[] msend = new byte[length+20];
                                    System.arraycopy(send,0,msend,0,length+20);
                                    //msend[msend.length-1]='E';
                                    send = msend;
                                }

                                writeByteArray = send;
                                Log.e((i+1)+"包：", String.valueOf(writeByteArray));
                                write();
                                //最多重发三次
                                /*int j =0;
                                for (;j<3;j++){
                                    writeByteArray = send;
                                    logTv("第"+i+"条信息"+"第"+barnum+"包"+(j+1)+"次发送："+writeByteArray+"  ");
                                    write();
                                    try {
                                        //1秒后读主机发过来的信息
                                        sleep(5000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    //收到回复跳出重发，继续下一条
                                   // if(recieveStr.equals("SRecieve"+id+"OK"))
                                    if(!recieveStr.isEmpty()){

                                        logTv("收到回复："+recieveStr+"\n");
                                        recieveStr="";
                                        break;
                                    }else{
                                        logTv("5秒未收到回复！重发"+"\n");
                                    }

                                    //清空接受信息的字符串
                                    recieveStr="";
                                }*/
                                try {
                                    sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                barnum++;
                            }
                            try {
                                sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if("OK".equals(recieveStr.trim().toUpperCase())){

                                logTv("收到OK回复："+recieveStr+"\n");
                                recieveStr="";

                            }else{
                                logTv("未收到回复！发送失败"+"\n");
                                recieveStr="";
                                break;
                            }


                            recieveStr="";

                        }

                        isSendingInfo = false;
                    }
                }).start();
            }
    }



    public void clearInfo(View view){
        if(isSendingInfo)Toast.makeText(this,"正在发送数据请不要重复点击！",Toast.LENGTH_SHORT).show();
        else if(isReadingGrade){
            Toast.makeText(this,"正在读取成绩请不要重复点击！",Toast.LENGTH_SHORT).show();
        }else {
            mTips.setText("");
            stuList.clear();
            infoAdapter.studentList.clear();
            clearEdit();
            infoAdapter.removeItem();


        }
    }

    public void readGrade(View view){

        if(isSendingInfo)Toast.makeText(this,"正在发送数据请不要重复点击！",Toast.LENGTH_SHORT).show();
        else if(stuList==null||stuList.size()==0){
            Toast.makeText(this,"信息为空，请先导入！",Toast.LENGTH_SHORT).show();
        }else if(isReadingGrade){
            Toast.makeText(this,"正在读取成绩请不要重复点击！",Toast.LENGTH_SHORT).show();
        }else{
            new Thread(){
                @Override
                public void run() {

                    boolean flag = true;
                    recieveStr="";

                    for (int i = 0; i < stuList.size(); i++) {
                        for (int j = 0; recieveStr.equals("") && j < 3; j++) {
                            logTv("第"+i+"条信息"+j+"次发送：");
                            writeByteArray = (stuList.get(i).getId()+",s=????").getBytes();
                            write();
                            //writeByteArray = ", s=????".getBytes();
                            //write();

                            try {
                                sleep(3000);       //暂停，每一秒输出一次
                            }catch (InterruptedException e) {
                                return;
                            }
                            if (recieveStr.equals("")&&j==2){
                                logTv("没有收到回复");
                                flag  =false;
                            }


                        }
                        if (!flag) break;
                        else{
                            String str= recieveStr;
                            String[] sourceStrArray = str.split(",");
                            String str1=sourceStrArray[1];
                            String[] sourceStrArray1 = str1.split("=");
                            stuList.get(i).setGrade(sourceStrArray1[1]);
                            logTv("收到回复，插入成绩:\n"+stuList.get(i).toStringWithGrade()+"\n");

                        }

                        recieveStr="";
                        //  if (recieveStr == null);
                    }
                    if (flag)
                        logTv("所有成绩读取成功！");

                    isReadingGrade = false;


                }
            }.start();
        }



    }






    public void upLoad(View view){
        if(isSendingInfo)Toast.makeText(this,"正在发送数据请不要重复点击！",Toast.LENGTH_SHORT).show();
        else if(isReadingGrade){
            Toast.makeText(this,"正在读取成绩请不要重复点击！",Toast.LENGTH_SHORT).show();
        }else{

        }

    }
    /**
     * 将每三个数字（或字符）加上逗号处理（通常使用金额方面的编辑）
     * 5000000.00 --> 5,000,000.00
     * 20000000 --> 20,000,000
     * @param str  无逗号的数字
     * @return 加上逗号的数字
     * 已经把逗号改成了小数点***********请注意
     */
    public  String strAddComma(String str) {
        if (str == null) {
            str = "";
        }
        String addCommaStr = ""; // 需要添加逗号的字符串（整数）
        String tmpCommaStr = ""; // 小数，等逗号添加完后，最后在末尾补上
        if (str.contains(".")) {
            addCommaStr = str.substring(0,str.indexOf("."));
            tmpCommaStr = str.substring(str.indexOf("."),str.length());
        }else{
            addCommaStr = str;
        }
        // 将传进数字反转
        String reverseStr = new StringBuilder(addCommaStr).reverse().toString();
        String strTemp = "";
        for (int i = 0; i < reverseStr.length(); i++) {
            if (i * 3 + 3 > reverseStr.length()) {
                strTemp += reverseStr.substring(i * 3, reverseStr.length());
                break;
            }
            strTemp += reverseStr.substring(i * 3, i * 3 + 3) + ",";
        }
        // 将 "5,000,000," 中最后一个","去除
        if (strTemp.endsWith(",")) {
            strTemp = strTemp.substring(0, strTemp.length() - 1);
        }
        // 将数字重新反转,并将小数拼接到末尾
        String resultStr = new StringBuilder(strTemp).reverse().toString() + tmpCommaStr;
        return resultStr;
    }

    /**
     * 将加上逗号处理的数字（字符）的逗号去掉 （通常使用金额方面的编辑）
     * 5,000,000.00 --> 5000000.00
     * 20,000,000 --> 20000000
     * @param str  加上逗号的数字（字符）
     * @return 无逗号的数字（字符）
     */
    public  String strRemoveComma(String str) {
        if (str == null) {
            str = "";
        }
        String resultStr = str.replaceAll(",",""); // 需要去除逗号的字符串（整数）

        return resultStr;
    }
    /*public static void show(Context context, String msg) {//单例toast 防止弹窗一直弹出
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }*/
}