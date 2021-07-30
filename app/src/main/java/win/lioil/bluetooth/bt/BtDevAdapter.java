package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import win.lioil.bluetooth.R;

public class BtDevAdapter extends RecyclerView.Adapter<BtDevAdapter.VH> {
    private static final String TAG = BtDevAdapter.class.getSimpleName();
    private final List<BluetoothDevice> mDevices = new ArrayList<>();
    private final Listener mListener;

    BtDevAdapter(Listener listener) {
        mListener = listener;
        addBound();
    }

    private void addBound() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null)
            mDevices.addAll(bondedDevices);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        BluetoothDevice dev = mDevices.get(position);
        String name = dev.getName();                   //设备（手机）名字
        String address = dev.getAddress();             //设备物理地址
        int bondState = dev.getBondState();
        holder.name.setText(name == null ? "" : name);
        holder.address.setText(String.format("%s (%s)", address, bondState == 10 ? "未配对" : "配对"));
    }                                                                              //这里的配对和未配对是显示在物理地址后面的

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void add(BluetoothDevice dev) {
        if (mDevices.contains(dev))
            return;
        mDevices.add(dev);
        notifyDataSetChanged();
    }

    public void reScan() {
        mDevices.clear();
        addBound();
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (!bt.isDiscovering())
            bt.startDiscovery();
        notifyDataSetChanged();
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;
        final TextView address;

        VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();               //pos指代的是位置，判断点击的是列表中的哪一个设备
            Log.d(TAG, "onClick, getAdapterPosition=" + pos);        //只是写在日志中，不显示在app上
            if (pos >= 0 && pos < mDevices.size())
                mListener.onItemClick(mDevices.get(pos));    //mDevices是自定义的List类型，get(pos)就是获取其中对应的元素
        }               //onItemClick调用的是下面的函数，而下面的函数只是一个定义，没有给出实现，事实上是在
    }                   //BtClientActivity这个类中给出的onItemClick函数的实现

    public interface Listener {
        void onItemClick(BluetoothDevice dev);
    }
}
