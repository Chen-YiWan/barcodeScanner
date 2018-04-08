package com.life.yiwanchen.barcodescanner;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kevinsawicki.http.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements BarcodeScanFragment.OnFragmentInteractionListener {

    private static final String TAG = "mainActivity";
    private static final int ZXING_CAMERA_PERMISSION = 1;
    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }
    BarcodeScanFragment cameraFragment;
    ProgressDialog dialog;
    String formNo;
    ArrayList<FormItem> formList;
    View progressLayout;
    ProgressBar progressBar;
    TextView progressText;
    MenuItem menuList;
    int currentProgress;
    int progressLength;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        menuList = menu.findItem(R.id.action_list);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list:
                // User chose the "Settings" item, show the app settings UI...
                nextPage();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

//        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
//        setContentView(mScannerView);                // Set the scanner view as the content view

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
//            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        }

        progressLayout = findViewById(R.id.progressLayout);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        cameraFragment = (BarcodeScanFragment) getSupportFragmentManager().findFragmentById(R.id.cameraFragment);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    private void nextPage(){
        Intent intent = new Intent(this, FormListActivity.class);
        intent.putParcelableArrayListExtra("FormList",formList);
        intent.putExtra("FormNo",formNo);
        startActivity(intent);
    }

    private void checkItem(String id){
        Boolean itemBoolean = false;
        for ( FormItem formItem: formList){
            if ( id.equals(formItem.id) && !formItem.enabled){
                itemBoolean = true;
                formItem.enabled = true;
                updateProgress();

                if (currentProgress == progressLength){
                    showSuccessDialog("Finish!");
                    nextPage();
                    formNo = null;
                    formList.clear();
                    progressLayout.setVisibility(View.GONE);
                    menuList.setVisible(false);
                }
                break;
            }else if ( id.equals(formItem.id) && formItem.enabled ){
                showErrorDialog("Number is enabled!");
                itemBoolean = true;
                break;
            }
        }
        if (!itemBoolean){
            showErrorDialog("Number is not in list!");
        }

    }

    @Override
    public void onFragmentInteraction(String barcode) {
//        if (barcode.equals("9780201379624")) {
//            Toast.makeText(this, "OKOK", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "NONONO", Toast.LENGTH_SHORT).show();
//        }

        if (formNo == null){
            formNo = barcode;
            new ApiTask().execute("http://192.168.2.203:3000/formNo/" + formNo);

        }else{
            checkItem(barcode);
        }

    }

    private void updateProgress(){
        currentProgress++;
        progressBar.setProgress(currentProgress);
        progressText.setText(currentProgress + "/" + progressLength);

    }

    private void showSuccessDialog(String msg){
        new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setTitle("Succcess")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showErrorDialog(String error){
        new AlertDialog.Builder(MainActivity.this)
        .setCancelable(false)
        .setTitle("Error")
        .setMessage(error)
        .setPositiveButton("OK", null)
        .show();
    }

    private class ApiTask extends AsyncTask<String, Long, AsyncTaskResult<String>> {

        protected void onPreExecute() {
            dialog = ProgressDialog.show(MainActivity.this,
                    "讀取中", "請等待...",true);
            cameraFragment.startHttp();
        }

        protected AsyncTaskResult<String> doInBackground(String... urls) {
            try {
                HttpRequest request = HttpRequest.get(urls[0]).connectTimeout(10000);

                Log.d("MyApp", request.toString());
                if (request.ok()) {
                    String json = request.body();
                    return new AsyncTaskResult<String>(json);
                } else {
                    return new AsyncTaskResult<String>(new Exception("fail " + request.code()));
                }
            } catch (HttpRequest.HttpRequestException exception) {
                Log.w("MyAPP", exception.toString());
                return new AsyncTaskResult<String>(exception);
            }
        }

        protected void onPostExecute(AsyncTaskResult<String> json) {
            if (json.getError() == null){
                Log.d("MyApp", "JSON: " + json);
                try {
                    JSONObject response = new JSONObject(json.getResult());
                    JSONArray list = response.getJSONArray("list");
                    int len = list.length();


                    if (len == 0){
                        Toast.makeText(MainActivity.this, "No item !", Toast.LENGTH_SHORT).show();
                        formNo = null;
                    } else {
                        progressLength = len;
                        currentProgress = 0;

                        progressBar.setMax(progressLength);
                        progressBar.setProgress(currentProgress);

                        progressText.setText(currentProgress + "/" + progressLength);

                        formList = new ArrayList<FormItem>();

                        for (int i=0;i<len;i++){
                            FormItem formItem = new FormItem(list.getJSONObject(i));

                            formList.add(formItem);
                        }


                        progressLayout.setVisibility(View.VISIBLE);
                        menuList.setVisible(true);
                    }

//                    Toast.makeText(MainActivity.this, list.toString(), Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    formNo = null;
                    showErrorDialog(e.toString());
                }

            }else{
                showErrorDialog(json.getError().toString());
                formNo = null;
                Log.d("MyApp", "HTTP failed");

            }
            dialog.dismiss();
            cameraFragment.finishHttp();

        }
    }

}
