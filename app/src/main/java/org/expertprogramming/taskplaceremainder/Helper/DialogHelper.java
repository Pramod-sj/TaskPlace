package org.expertprogramming.taskplaceremainder.Helper;

import android.app.ProgressDialog;
import android.content.Context;

public class DialogHelper{
    ProgressDialog progressDialog;
    public DialogHelper(Context context) {
        progressDialog=new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

    }
    public void showProgressDialog(String text){
        progressDialog.setMessage(text);
        progressDialog.show();
    }
    public void updateDialogMessage(String message){
        progressDialog.setMessage(message);
    }
    public void dismissProgressDialog(){
        progressDialog.dismiss();
    }

}
