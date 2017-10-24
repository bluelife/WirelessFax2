package wirelessfax.phonelink.com.cn.wirelessfax;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import wirelessfax.phonelink.com.cn.Sqlite.FileLib;
import wirelessfax.phonelink.com.cn.Sqlite.FileLibController;
import wirelessfax.phonelink.com.cn.Utls.Utls;

/**
 * Created by CNFAFAN3 on 11/6/2016.
 */

public class ListContentViewAdapter extends BaseAdapter {
    private org.slf4j.Logger log = LoggerFactory.getLogger(ListContentViewAdapter.class);

    private LayoutInflater mInflater;

    private List<FileLib> list = new ArrayList<>();
    private FileLib fileLib = new FileLib();

    private  int miType = FileLibController.SEND_TYPE;

    public ListContentViewAdapter(Context context){
        this.mInflater = LayoutInflater.from(context);
    }

    public boolean clear()
    {
        list.clear();
        this.notifyDataSetChanged();

        return true;
    }

    //type：send,receive,draft,star file
//    public boolean search(int Type)
//    {
//        list.clear();
//        DataQryCondition cond = new DataQryCondition();
//        cond.iType = 0;
//        list = getItems(cond);
//        log.debug("list size: "+list.size());
//
//        return true;
//    }
    public boolean setDataList(List<FileLib> dataList, int iType)
    {
        list.clear();
        list = dataList;
        miType = iType;

        return true;
    }

    @Override
    public int getCount() {
        log.debug("curelist size:"+list.size());
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        FileLib fileItem = list.get(position);
        return fileItem;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public final class ViewHolder{
        public ImageView imgRead;
        public TextView txtSender;
        public TextView txtTime;
        public TextView txtSubject;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String str="null";
        int type = 0;
        ViewHolder holder = null;
        if (convertView == null) {
            log.debug("getView :"+position);
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_content_item, null);
            holder.imgRead = (ImageView)convertView.findViewById(R.id.imgRead);
            holder.txtSender = (TextView) convertView.findViewById(R.id.txtSender);
            holder.txtTime = (TextView) convertView.findViewById(R.id.txtTime);
            holder.txtSubject = (TextView) convertView.findViewById(R.id.txtSubject);

            FileLib item = list.get(position);

            if(item.isbRead()) {
                holder.imgRead.setBackgroundResource (R.drawable.img_guide_point_normal);
            } else {
                holder.imgRead.setBackgroundResource (R.drawable.img_guide_point_select);
            }

            if (miType == FileLibController.SEND_TYPE) {
                holder.txtSender.setText(item.getStrReceiver());
            } else if (miType == FileLibController.RECV_TYPE) {
                holder.txtSender.setText(item.getStrSender());
            } else {
                if(!item.getStrSender().isEmpty()) {
                    holder.txtSender.setText(item.getStrSender());
                } else {
                    holder.txtSender.setText(item.getStrReceiver());
                }
            }

            if(holder.txtSender.getText().toString().trim().isEmpty()) {
                if(!item.getStrSender().isEmpty()) {
                    holder.txtSender.setText(item.getStrSender());
                } else {
                    holder.txtSender.setText(item.getStrReceiver());
                }
            }


            String str2 = item.getStrFilePath();
            int pos = str2.lastIndexOf('/');
            if (pos >= 0) {
                holder.txtSubject.setText(str2.substring(pos+1, str2.length()));
            } else {
                holder.txtSubject.setText(str2);
            }
            if(item.getiTotal() <= 1) {
                holder.txtTime.setText(Utls.getUIShowTime(item.getlTime()));
            } else {
                holder.txtTime.setText(""+item.getiTotal());
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        //holder.image.setImageResource(R.drawable.small);
        return convertView;
    }

}
