package projectegco.com.myproject;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.PhotoLoader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TakePhotoActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ArrayAdapter<Photo> photoArrayAdapter;
    private PhotoDataSource photoDataSource;
    protected List<Photo> data = new ArrayList<>();
    public String absolutePath;
    String currentDateTime;
    protected static final String selectedSubject = "subject";
    protected static final String idselectedSubject = "subjectid";
    protected static final String imgName = "imagename";
    TextView subTextView;
    Button choosePhotoGall;
    CheckBox checkBox;
    ListView listView;
    ImageView photoView;
    Photo photo1;

    boolean isSelectAll = true;

    CheckBox checkboxAll;
    ArrayList<Integer> msgMultiSelected;

    String filename;
    Uri imageUri;
    String imageurl;
    Bitmap thumbnail,compBitmap;

    ContentValues values;
    byte[] BYTE;

    ArrayList<Bitmap> filepath_array;

    ImageButton fabcam;
    Button fabsend;

    int check = 0;
    int count = 0;
    int j;
    String flag = "false";

    protected static final String chooseimageurl = "imageurl";
    private static final int REQUEST_CODE = 11;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        System.out.println("aaaa stream "+chooseimageurl);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#009688")));
        setTitle("Photo");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //set back button

        final String getSubject = getIntent().getStringExtra(selectedSubject);
        final String getSubjectID = getIntent().getStringExtra(idselectedSubject);

        getIntent().putExtra(CustomAdapter.idselectedSubject,getSubjectID);

        subTextView = (TextView) findViewById(R.id.subjectTxt);
        subTextView.setText(getSubject);
        fabcam = (ImageButton) findViewById(R.id.fabcam);
        fabsend = (Button) findViewById(R.id.fabok);

        if (!hasCamera()) {
            fabcam.setEnabled(false);
        }

        //Set Listview
        photoDataSource = new PhotoDataSource(this);
        photoDataSource.open();
        data = photoDataSource.getAllPhotos(getSubjectID);
        photoArrayAdapter = new CustomAdapter(this, 0, data);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(photoArrayAdapter); //push data in adapter into listview
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemChecked(4, true);

        //Select All checkbox
        checkboxAll = (CheckBox) findViewById(R.id.checkBox2);
        checkboxAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean check = true;
                if (checkboxAll.isChecked()){
                    for (int i = 0; i < data.size(); i++) {
                        data.get(i).setSelected(true);
                    }
                }
                else {
                    for (int i = 0; i < data.size(); i++) {
                        data.get(i).setSelected(false);
                    }

                }
                photoArrayAdapter.notifyDataSetChanged();
            }
        });

        //Get checked checkbox
        fabsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(TakePhotoActivity.this);

                final MyCommand myCommand = new MyCommand(getApplicationContext());
                final MyCommand myFlag = new MyCommand(getApplicationContext());
                Log.e("Size",data.size()+"");
                for (j = 0; j < data.size(); j++) {

                    System.out.println(j + "xxxxcheck = " + data.get(j).isSelected());
                    if (data.get(j).isSelected()) {
                            count = count + 1;

                            dialog.setTitle("Process dialog");
                            dialog.setContentView(R.layout.wait_dialog);
                            dialog.show();
                            dialog.setCanceledOnTouchOutside(false);

                            System.out.println("xxxxdata " + data.get(j).getImgpath());
                            //upload to server
                            try {
                                Bitmap bitmap = PhotoLoader.init().from(data.get(j).getImgpath()).requestSize(2048, 2048).getBitmap();
                                final String encodedString = ImageBase64.encode(bitmap);
                                String url = "http://192.168.1.149/upload/upload.php";

                                System.out.println("uu " + j);

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        check = check + 1;
                                        System.out.println("uu check " + check);
                                        System.out.println("uu response " + response);

//                                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();

                                        if (check == count) {
                                            System.out.println("uu check STOP " + check);
//                                            Toast.makeText(getApplicationContext(), response+" "+count+" photos", Toast.LENGTH_SHORT).show();
//                                            AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
//                                            builder.setMessage("Response: "+response);
//                                            builder.setNegativeButton("Close", null);
//                                            builder.create();
//                                            builder.show();
                                            flag = "true";
                                            System.out.println("uu FLAG:: " + flag);


                                            ////////////////////////////////Connect MATLAB and send 'flag' to PHP///////////////////////////////////////////////////
                                            String url = "http://192.168.1.149/upload/CallConnectMatlab.php";
                                            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                                @Override
                                                public void onResponse(String response) {
//                                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                                                    System.out.println("uu flag heyy " + response);
                                                }
                                            }, new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
                                                    System.out.println("uu flag error " + error);
                                                    builder.setMessage("Error flag: " + error);
                                                    builder.setNegativeButton("Close", null);
                                                    builder.create();
                                                    builder.show();


//                                                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
//                                                        System.out.println("uu flag heyy 1st");
//                                                    } else if (error instanceof AuthFailureError) {
//                                                        System.out.println("uu flag heyy 2nd");
//                                                    } else if (error instanceof ServerError) {
//                                                        System.out.println("uu flag heyy 3td");
//                                                    } else if (error instanceof NetworkError) {
//                                                        System.out.println("uu flag heyy 4th");
//                                                    } else if (error instanceof ParseError) {
//                                                        System.out.println("uu flag heyy 5th");
//                                                    }
                                                }
                                            }) {
                                                @Override
                                                protected Map<String, String> getParams() throws AuthFailureError {
                                                    Map<String, String> params = new HashMap<>();
                                                    //Add data to be send to php server
                                                    params.put("checkallphoto", flag);
                                                    System.out.println("flag status = " + flag);
                                                    return params;
                                                }
                                            };


                                            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                                                    50000,
                                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                                            myFlag.add(stringRequest);
                                            myFlag.execute();

                                            final Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    dialog.dismiss();
                                                    handler.postDelayed(this, 30000);
                                                }
                                            }, 30000);

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
                                                    builder.setMessage("DONE!! Touch 'Result' button to check the result.");
                                                    builder.setNegativeButton("Close",null);
                                                    builder.create();
                                                    builder.show();
                                                }
                                            }, 30000);


                                            ///////////////////////////////////////////////////////////////////////////////////////////////////////


                                        }
                                    }
                                }
                                        , new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        dialog.dismiss();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
                                        builder.setMessage("Error while uploading image volley error. Please check your internet connection.");
                                        builder.setNegativeButton("Close", null);
                                        builder.create();
                                        builder.show();
                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String, String> params = new HashMap<>();
                                        params.put("image", encodedString);
                                        //Add data to be send to php server
                                        params.put("subjectname", getSubjectID);
//                                        params.put("checkallphoto", flag);
//                                        System.out.println("uu flag status = "+flag);
                                        return params;
                                    }
                                };
                                myCommand.add(stringRequest);

                            } catch (FileNotFoundException e) {
                                dialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
                                builder.setMessage("Error while loading image file not found. Please try again.");
                                builder.setNegativeButton("Close", null);
                                builder.create();
                                builder.show();
                            }
                        }
                }

                myCommand.execute();


                System.out.println("uuunder execute");

                System.out.println("uu ================uu flag==============");
//                if(flag == "true"){
//                    String url = "http://192.168.1.143/upload/CallConnectMatlab.php";
//                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
//                            System.out.println("uu flag heyy");
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                        Toast.makeText(getApplicationContext(), "Error while uploading subject volley error subject", Toast.LENGTH_SHORT).show();
//                            AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
//                            builder.setMessage("Error flag. Please try again.");
//                            builder.setNegativeButton("Close",null);
//                            builder.create();
//                            builder.show();
//                        }
//                    }) {
//                        @Override
//                        protected Map<String, String> getParams() throws AuthFailureError {
//                            Map<String, String> params = new HashMap<>();
//                            //Add data to be send to php server
//                            params.put("checkallphoto", flag);
//                            System.out.println("flag status= "+flag);
//                            return params;
//                        }
//                    };
//                    myCommand.add(stringRequest);
//                    myCommand.execute();
//
//                }


            }
        });
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void launchCamera(View view) {
        final String getSubject = getIntent().getStringExtra(selectedSubject);
        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, getSubject);
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    //get photo
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK ) {
//            Bundle extras = data.getExtras();
//            Bitmap photo = (Bitmap)extras.get("data");

        System.out.println("printfilename22 "+imageUri);
        try {
                thumbnail = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//                thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                imageurl = getRealPathFromURI(imageUri);

        } catch (Exception e) {
                e.printStackTrace();
            }

            String getSubject = getIntent().getStringExtra(selectedSubject);
            String getSubjectID = getIntent().getStringExtra(idselectedSubject);

        //Get Current time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
            currentDateTime = dateFormat.format(new Date());

            // put value into Photo table
            photoDataSource.open();
            photo1 = photoDataSource.createPhoto(imageurl, getSubjectID, currentDateTime);
            photoArrayAdapter.add(photo1);
            photoArrayAdapter.notifyDataSetChanged();
        }

        else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            String[] projection = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();

            String getSubject = getIntent().getStringExtra(selectedSubject);
            String getSubjectID = getIntent().getStringExtra(idselectedSubject);

            //Get Current time
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH:mm");
            currentDateTime = dateFormat.format(new Date());

            // put value into Photo table
            if (picturePath == null){
                AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this);
                builder.setMessage("This photo isn't exist in memory.");
                builder.setNegativeButton("Close",null);
                builder.create();
                builder.show();
            }else{
                photoDataSource.open();
                photo1 = photoDataSource.createPhoto(picturePath, getSubjectID, currentDateTime);
                photoArrayAdapter.add(photo1);
                photoArrayAdapter.notifyDataSetChanged();
            }

        }

    }


//    public Uri getImageUri(Context inContext, Bitmap inImage) {
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
//        System.out.println("xxpathparse: " + Uri.parse(path));
//        System.out.println("xxpath..: " + path);
//        return Uri.parse(path);
//    }

    public String getRealPathFromURI(Uri uri) {
//        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
//        cursor.moveToFirst();
//        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//        System.out.println("xxidx: " + cursor.getString(idx));
//        return cursor.getString(idx);

        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        return cursor.getString(column_index);

    }

    public void onClick(View view){

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void showResult(View view){

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://192.168.1.149/upload/QueryDB.php"));
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        final String getSubjectID = getIntent().getStringExtra(idselectedSubject);
        if (id == android.R.id.home) {
            finish();
        }
        if (id == R.id.deletebtn){

                    AlertDialog.Builder builder = new AlertDialog.Builder(TakePhotoActivity.this); // where dialog appear
                    builder.setMessage("Do you want to delete the photos?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        // when ans = yes do this
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int a = 0; a < data.size(); a++) {
                                if (data.get(a).isSelected()) {
                                    photoDataSource.open();

                                    for (int j = 0; j < data.size(); j++) {
                                        System.out.println(j + "xxxxcheck = " + data.get(j).isSelected());
                                        if (data.get(j).isSelected()) {
                                            photo1 = photoArrayAdapter.getItem(j);
                                            photoDataSource.deleteResult(data.get(j));
                                        }
                                    }
                                    data = photoDataSource.getAllPhotos(getSubjectID);
                                    photoArrayAdapter = new CustomAdapter(TakePhotoActivity.this, 0, data);
                                    listView.setAdapter(photoArrayAdapter);
                                    Toast.makeText(TakePhotoActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    onPause();
                                }
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create();
                    builder.show();
                }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        photoDataSource.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        photoDataSource.close();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deletebutton, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
