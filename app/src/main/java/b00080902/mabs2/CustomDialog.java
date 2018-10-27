package b00080902.mabs2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomDialog {

    LayoutInflater inflater;

    public void setupDialog(final Context context){
        inflater= LayoutInflater.from(context);

//        TextView textView = new TextView(context);
//        textView.setText("This is a custom dialog");
//        textView.setGravity(Gravity.CENTER_HORIZONTAL);
//        textView.setTextSize(22);
//        textView.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
//
//        final AlertDialog alertDialogBuilder = new  AlertDialog.Builder(context)
//                .setCustomTitle(inflater.inflate(R.layout.dialog_title, null))
//                .setView(textView)
//                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i)
//                    {
//
//                    }}).create();
//        alertDialogBuilder.show();

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setIcon(R.drawable.mabslogo);
        builderSingle.setTitle("Select One Name:-");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add("Hardik");
        arrayAdapter.add("Archit");
        arrayAdapter.add("Jignesh");
        arrayAdapter.add("Umang");
        arrayAdapter.add("Gatti");

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);
                AlertDialog.Builder builderInner = new AlertDialog.Builder(context);
                builderInner.setMessage(strName);
                builderInner.setTitle("Your Selected Item is");
                builderInner.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which) {
                        dialog.dismiss();
                    }
                });
                builderInner.show();
            }
        });
        builderSingle.show();
    }
}