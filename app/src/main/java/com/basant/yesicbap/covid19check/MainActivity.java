package com.basant.yesicbap.covid19check;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    //*******************************************************************************variable declaration starting********************************************************************
    // for input 1
    private TextView mTextview_1 = null;
    private EditText mEditview_1 = null;

    // for input 2
    private TextView mTextview_2 = null;
    private EditText mEditview_2 = null;

    // for input 3
    private TextView mTextview_3 = null;
    private EditText mEditview_3 = null;

    // for input 4
    private TextView mTextview_4 = null;
    private TextView mTextview_4_tv = null;
    private Button mButton_4_btn = null;

    //for submit button
    private Button mSubmit = null;

    // for debug
    private static final String LOG_TAG = "Covid19CheckTest";

    // for accessing media recorder
    private MediaRecorder mMediaRecorder;
    private  String mFileName = null;
    private String randomFileName = "ABCDEFGHIJKLMNOP";

    // firebase and loading variable
    private FirebaseFirestore mFirebaseFirestore;
    private StorageReference mStorageRef;
    private ProgressDialog mPrograss;

     // for random file name and request code needed for run time permission
    private Random random ;
    public static final int RequestPermissionCode = 123;

    //text field input variable
    private String mEditTextAnswer_1;
    private String mEditTextAnswer_2;
    private String mEditTextAnswer_3;


    //logic variable
    private boolean isRecording;
    private boolean isRecorded;
    //******************************************************global variable declaration ending**********************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    //*************************************************************************************app execution begins here*************************************************************************








     //**********************************************************************************referencing starts*******************************************************************************
        bindingViewWithJavaCode();
     //***********************************************************************************referencing ends**********************************************************************************









     //******************************************************************when record btn is pressed down and pressed up starts*********************************************************
         mButton_4_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 if(isRecording){
                     isRecording = false;
                     stopRecording();
                     mTextview_4_tv.setText("recording stoped...\n(रेकर्डि रोकियो ...)");
                     mButton_4_btn.setText("Start Recording again\n(पुनः रेकर्डि सुरू गर्नुहोस्)");
                     isRecorded =true;
                     mButton_4_btn.setBackgroundColor(getResources().getColor(R.color.OffRecordingBtn));

                 }else{
                     isRecording = true;
                     startRecording();
                     mTextview_4_tv.setText("recording started...\n(रेकर्डि सुरु भयो ...)");
                     mButton_4_btn.setText("stop Recording\n(रेकर्डि रोक्नुहोस्)");
                     mButton_4_btn.setBackgroundColor(getResources().getColor(R.color.OnRecordingBtn));
                 }
                 }

         });
    //******************************************************************when record btn is pressed down and pressed up ends*********************************************************







    //******************************************************************when submit data button is pressed starts**************************************************************
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadAudioAndDataToFirebase();
            }
        });
    //******************************************************************when submit data button is pressed ends***********************************************************************








    //*******************************************************************setting up text watcher for all input fields starts*********************************************************
        mEditview_1.addTextChangedListener(loginTextWatcher);
        mEditview_2.addTextChangedListener(loginTextWatcher);
        mEditview_3.addTextChangedListener(loginTextWatcher);
    //*******************************************************************setting up text watcher for all input fields ends************************************************************






    //***********************************************************************checking run time permission******************************************************************************
        requestPermission();
        checkPermission();
    //***************************************************************************app execution ends here*******************************************************************************

    }






    //************************************************* assigning part of code to variable to reduce extra code start*******************************************************************
    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


            //  text field input variable
            mEditTextAnswer_1 = mEditview_1.getText().toString().trim().toLowerCase();
            mEditTextAnswer_2 = mEditview_2.getText().toString().trim();
            mEditTextAnswer_3 = mEditview_3.getText().toString().trim();
            mSubmit.setBackgroundColor(getResources().getColor(R.color.submitBackground_enabled));
            mSubmit.setEnabled(!mEditTextAnswer_1.isEmpty() && !mEditTextAnswer_2.isEmpty()&& !mEditTextAnswer_3.isEmpty());


        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };
    //******************************************************************assigning part of code to variable to reduce extra code ends*********************************************************






    //*********************************************************************declaration of member variable starts*****************************************************************************

    private boolean isReadyToUpload(){
        if(mEditTextAnswer_1.matches("yes")||mEditTextAnswer_1.matches("no")){
            return true;
        }
        return false;
    }

    private void bindingViewWithJavaCode() {
        mTextview_1 = findViewById(R.id.input_1_question);
        mEditview_1 = findViewById(R.id.input_1_answer);

        mTextview_2 = findViewById(R.id.input_2_question);
        mEditview_2 = findViewById(R.id.input_2_answer);

        mTextview_3 = findViewById(R.id.input_3_question);
        mEditview_3 = findViewById(R.id.input_3_answer);


        mTextview_4 = findViewById(R.id.input_4_question);
        mTextview_4_tv = findViewById(R.id.input_4_answer_tv);
        mButton_4_btn = findViewById(R.id.input_4_answer_btn);

        mSubmit = findViewById(R.id.input_form_submit_btn);
        mPrograss = new ProgressDialog(this);
        // initializing random variable
        random = new Random();
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CreateRandomAudioFileName(5) + "Covid19Audio.3gp";
        // Create a storage reference from our app
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();

        //logic variable
        isRecording = false;
        isRecorded = false;


    }

    private void startRecording() {

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mMediaRecorder.setOutputFile(mFileName);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        mMediaRecorder.start();
    }

    private void stopRecording() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    private void uploadAudioAndDataToFirebase() {
         if(!isRecording){

               if(isRecorded){

                   if(isReadyToUpload()){
                       //upload file to database
                       //progressbar
                       mPrograss.setMessage("uploading audio and data...");
                       mPrograss.show();

                       //  upload the data(text amd audio) entered by user to the firebase
                       //  uploading audio into fireStore
                       Uri uri = Uri.fromFile(new File(mFileName));
                       // creating a map
                       Map<String, String> uesMap = new HashMap<>();
                       uesMap.put("answer_1", mEditTextAnswer_1);
                       uesMap.put("answer_2", mEditTextAnswer_2);
                       uesMap.put("answer_3", mEditTextAnswer_3);
                       uesMap.put("audio_url", "Audio/"+ uri.getLastPathSegment());

                       StorageReference filePath = mStorageRef.child("Audio/"+ uri.getLastPathSegment());
                       // Register observers to listen for when the download is done or if it fails
                       filePath.putFile(uri).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception exception) {
                               // Handle unsuccessful uploads
                               Log.d(LOG_TAG,"Fail to upload audio(अडियो अपलोड गर्न असफल)");
                           }
                       }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                               // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                               Log.d(LOG_TAG,"audio successfully uploaded(अडियो सफलतापूर्वक अपलोड गरियो)");
                           }
                       }).addOnCanceledListener(new OnCanceledListener() {
                           @Override
                           public void onCanceled() {
                               Log.d(LOG_TAG,"audio upload canceled(अडियो अपलोड रद्द गरियो)");
                           }
                       });

                       //  uploading audio url and QNA data to firebase fireStore




                       mFirebaseFirestore.collection("Covid19_Patient_Properties").add(uesMap).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                           @Override
                           public void onSuccess(DocumentReference documentReference) {
                               mPrograss.dismiss();
                               showAlertDialog("Happy(खुशी)", "upload successful, Your time and effort is highly appreciated \n Thank you!!!\nअपलोड सफल, तपाइँको समय र प्रयास अत्यधिक सराहना गरियो\n" +
                                       "  धन्यवाद!!!");
                               resetInputFields();
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               mPrograss.dismiss();
                               showAlertDialog("Sad(दुखः)", "Fail to upload Try again !!!\n(अपलोड गर्न असफल पुनः प्रयास गर्नुहोस् !!!)");
                               resetInputFields();
                           }
                       }).addOnCanceledListener(new OnCanceledListener() {
                           @Override
                           public void onCanceled() {
                               mPrograss.dismiss();
                               showAlertDialog("Sad(दुखः)", "upload canceled,Try again !!!\n(अपलोड रद्द गरियो, पुन: प्रयास गर्नुहोस् !!!)");
                               resetInputFields();
                           }
                       });


                   }else {
                    Toast.makeText(getApplicationContext(), "Please insert yes or no only(yes वा no मात्र सम्मिलित गर्नुहोस्)",Toast.LENGTH_LONG).show();
                   }
               }
         }else{
             //display recording is ON warning
           showAlertDialog("Error:(त्रुटि:)", "Please turn off the recording!!!\n(कृपया रेकर्डि बन्द गर्नुहोस् !!!)");
         }
    }

    private void showAlertDialog(String title, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Okay(ल)", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel(रद्द गर्नुहोस्)",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void resetInputFields() {
        mEditview_1.setText("");
        mEditview_2.setText("");
        mEditview_3.setText("");
        mTextview_4_tv.setText("");
    }

    private void requestPermission() {
       if(ContextCompat.checkSelfPermission(MainActivity.this,
               INTERNET)+
               ContextCompat.checkSelfPermission(MainActivity.this,
                  RECORD_AUDIO)+
          ContextCompat.checkSelfPermission(MainActivity.this,
                   WRITE_EXTERNAL_STORAGE)+
           ContextCompat.checkSelfPermission(MainActivity.this,
                   READ_EXTERNAL_STORAGE) !=
               PackageManager.PERMISSION_GRANTED){
             // when permission not granted
           if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                   INTERNET) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                   RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                   WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                   READ_EXTERNAL_STORAGE)){
               // create alert dialog
               AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Grant those Permissions");
                builder.setMessage("Internet, Record audio, read and write external storage permission");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        INTERNET,
                                        RECORD_AUDIO,
                                        READ_EXTERNAL_STORAGE,
                                        WRITE_EXTERNAL_STORAGE
                                },RequestPermissionCode

                        );
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();

           }else{
               ActivityCompat.requestPermissions(
                       MainActivity.this,
                       new String[]{
                               INTERNET,
                               RECORD_AUDIO,
                               READ_EXTERNAL_STORAGE,
                               WRITE_EXTERNAL_STORAGE
                       },RequestPermissionCode

               );

           }



       }else {
            // when permission are already granted
          Toast.makeText(getApplicationContext(), "Permission already granted...",
                  Toast.LENGTH_LONG).show();
       }

    }

    private boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                INTERNET)+
                ContextCompat.checkSelfPermission(MainActivity.this,
                        RECORD_AUDIO)+
                ContextCompat.checkSelfPermission(MainActivity.this,
                        WRITE_EXTERNAL_STORAGE)+
                ContextCompat.checkSelfPermission(MainActivity.this,
                        READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
            return  true;
        }else {
            requestPermission();
        }

        return false;
    }

    public String CreateRandomAudioFileName(int string){

        StringBuilder stringBuilder = new StringBuilder( string );

        int i = 0 ;
        while(i < string ) {

            stringBuilder.append(randomFileName.charAt(random.nextInt(randomFileName.length())));

            i++ ;
        }
        return stringBuilder.toString();

    }

    private void showCustomDialog() {

        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.custom_dialog,null);
        final Button button_no = mView.findViewById(R.id.custom_dialog_no);
        final Button button_yes = mView.findViewById(R.id.custom_dialog_yes);
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);

        //if no button is clicked
        button_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });

        //if yes button is clicked

        button_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //function call to exit from the app
                clickExit();
            }


        });

        //to show alert dialog
        alertDialog.show();
    }

    private void clickExit() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }


    //*********************************************declaration of member variable ends*****************************************************************************











    //********************************************Overridden function declaration function starts**********************************************************************

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==RequestPermissionCode){

            if((grantResults.length >0 )
                    && ((grantResults[0]
                    + grantResults[1]
                    + grantResults[2]
                    + grantResults[3]) ==  PackageManager.PERMISSION_GRANTED)){

                Toast.makeText(getApplicationContext(),
                        "Permission granted...", Toast.LENGTH_LONG).show();
            }else{

                Toast.makeText(getApplicationContext(),
                        "Permission denied...", Toast.LENGTH_LONG).show();

            }


        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

       getMenuInflater().inflate(R.menu.main_menu, menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.privacyPolicy:
                Intent pIntent = new Intent(MainActivity.this,WebViewActivity.class);
                pIntent.putExtra("url",String.valueOf("https://yesicbap.blogspot.com/p/covid19checkadatacollectionappprivacypo.html"));
                startActivity(pIntent);



                break;

            case R.id.termsAndService:
                Intent pIntent1 = new Intent(MainActivity.this,WebViewActivity.class);
                pIntent1.putExtra("url",String.valueOf("https://yesicbap.blogspot.com/p/covid19checkadatacollectionapptermsands.html"));
                startActivity(pIntent1);
                break;

            case R.id.share:
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "your body";
                String shareSubject = "your subject";

                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                startActivity(Intent.createChooser(shareIntent,"Share through"));


                break;

            case R.id.exit:
                //calling of custom dialog mathod
                showCustomDialog();
                break;


        }

        return super.onOptionsItemSelected(item);
    }



    //********************************************Overridden function declaration function ends**********************************************************************










}
