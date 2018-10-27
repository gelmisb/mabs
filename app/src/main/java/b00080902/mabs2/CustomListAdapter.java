package b00080902.mabs2;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.firebase.ui.auth.AuthUI.TAG;

public class CustomListAdapter extends ArrayAdapter<Article> implements View.OnClickListener{

    private ArrayList<Article> dataSet;
    Context mContext;

    private String type;

    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        TextView txtType;
        TextView txtVersion;
        ImageView info;
    }

    public CustomListAdapter(ArrayList<Article> data, String type, Context context) {
        super(context, R.layout.row_item_add, data);
        this.dataSet = data;
        this.mContext=context;
        this.type = type;

    }



    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        Article model =(Article) object;

        switch (v.getId())
        {
            case R.id.item_info:
                assert model != null;
                Snackbar.make(v, " " +model.getItem(), Snackbar.LENGTH_SHORT)
                        .setAction("1 item removed", new Removing(model.getItem())).show();

                break;
        }
    }




    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Article model = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if(type.equals("income")){
                convertView = inflater.inflate(R.layout.row_item_add, parent, false);
            }
            else {
                convertView = inflater.inflate(R.layout.row_item_remove, parent, false);
            }
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.title);
            viewHolder.txtType = (TextView) convertView.findViewById(R.id.type);
            viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.date);
            viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        assert model != null;
        if(model.getItem() == null){
            viewHolder.txtName.setText("Test");
        }else {
            viewHolder.txtName.setText(model.getItem());
        }


        viewHolder.txtType.setText(model.getDate());
        viewHolder.txtVersion.setText(model.getValue());
        viewHolder.info.setOnClickListener(this);
        viewHolder.info.setTag(position);

        // Return the completed view to render on screen
        return convertView;
    }

}