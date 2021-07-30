package win.lioil.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.R;
import win.lioil.bluetooth.adapter.InfoAdapter;
import win.lioil.bluetooth.bean.Student;

public class BtServerActivity extends Activity implements BtBase.Listener {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtServer mServer;
    private RecyclerView recyclerView;
    private InfoAdapter infoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btserver);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);
        mServer = new BtServer(this);
        recyclerView = findViewById(R.id.lv_info);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        infoAdapter = new InfoAdapter();
        recyclerView.setAdapter(infoAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.unListener();
        mServer.close();
    }

    public void sendMsg(View view) {
        if (mServer.isConnected(null)) {
            String msg = mInputMsg.getText().toString();   //可以在这里随机生成跳绳的次数，然后在下面判断是否收到了~，收到了就返还~
            String msg1 = mInputMsg.getText().toString();
//            if (TextUtils.isEmpty(msg))
//                APP.toast("消息不能空", 0);
//            else

                List<Student> sList;
                sList =BtBase.generateGrade(BtBase.stringToList(BtBase.jsonMsg));
                BtBase.jsonMsg=BtBase.listToString(sList);
                mServer.sendMsg(BtBase.listToString(sList),"");
                infoAdapter = new InfoAdapter(sList);
                recyclerView.setAdapter(infoAdapter);

        } else
            APP.toast("没有连接", 0);
    }

    public void sendFile(View view) {
        if (mServer.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile())
                APP.toast("文件无效", 0);
            else
                mServer.sendFile(filePath);
        } else
            APP.toast("没有连接", 0);
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
                mServer.listen();
                msg = "连接断开,正在重新监听...";
                mTips.setText(msg);
                break;
            case BtBase.Listener.MSG:

                msg = String.format("%s", obj);
                mLogs.append(msg);
                List<Student> mlist = BtBase.stringToList(BtBase.jsonMsg);
                infoAdapter = new InfoAdapter(mlist);
                System.out.println("打印:"+mlist.toString());
                System.out.println("打印:"+msg);
                recyclerView.setAdapter(infoAdapter);





                break;
        }
        APP.toast(msg, 0);
    }
}