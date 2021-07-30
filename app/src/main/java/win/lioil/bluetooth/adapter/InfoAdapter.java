package win.lioil.bluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import win.lioil.bluetooth.R;
import win.lioil.bluetooth.bean.Student;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.VH> {

    public List<Student> studentList;

    public InfoAdapter (List<Student> list){
        this.studentList = list;
    }
    public InfoAdapter (){
        super();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_item_1, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Student student = studentList.get(position);
        holder.tv_name.setText(student.getName());
        holder.tv_id.setText(student.getId());
        holder.tv_grade.setText(student.getGrade()+"");
        holder.tv_gender.setText(student.getGender()+"");
        holder.tv_handlerId.setText(student.getHandlerId()+"");
        holder.tv_ledId.setText(student.getLedId()+"");

    }

    public void removeItem(){
        studentList.clear();//删除数据源,移除集合中当前下标的数据
        notifyDataSetChanged();
//        notifyItemRemoved(position);//刷新被删除的地方
//        notifyItemRangeChanged(position,getItemCount()); //刷新被删除数据，以及其后面的数据
    }

    public void add(Student stu) {
       if (studentList ==null)
           studentList = new ArrayList<>();

       System.out.println("infoAdapter:"+stu.toString());

        studentList.add(stu);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return studentList==null?0:studentList.size();
    }

    class VH extends RecyclerView.ViewHolder  {
        TextView tv_name;
        TextView tv_id;
        TextView tv_grade;
        TextView tv_gender;
        TextView tv_handlerId;
        TextView tv_ledId;

        public VH(View itemView) {
            super(itemView);

            tv_name=itemView.findViewById(R.id.tv_name);
            tv_id=itemView.findViewById(R.id.tv_id);
            tv_grade=itemView.findViewById(R.id.tv_grade);
            tv_gender=itemView.findViewById(R.id.tv_gender);
            tv_handlerId=itemView.findViewById(R.id.tv_handlerId);
            tv_ledId=itemView.findViewById(R.id.tv_ledId);

        }


    }
}
