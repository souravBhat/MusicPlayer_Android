package com.sourav.musicon;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.util.List;

public class Stream_Activity extends AppCompatActivity {
    private EditText edit_title;
    private EditText edit_url;
    private Button query;
    private Button update;
    private Button delete;
    private Button play;
    private Button add;
    private Button see_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_);

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Stream Music");


        edit_title=(EditText)findViewById(R.id.edit_title);
        edit_url=(EditText)findViewById(R.id.edit_url);
        query=(Button)findViewById(R.id.query);
        update=(Button)findViewById(R.id.update);
        delete=(Button)findViewById(R.id.delete);
        play=(Button)findViewById(R.id.play);
        add=(Button)findViewById(R.id.add);
        see_list=(Button)findViewById(R.id.see_list);

        ////set initial condition
        edit_title.setText("");
        edit_title.setText("");

        ////onclick listener for query
        query.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(edit_title.getText().toString()))
                {
                    ContentResolver contentResolver=getContentResolver();
                    Cursor cursor=contentResolver.query(MyMediaDataContract.MyMedia.CONTENT_URI,MyMediaDataContract.MyMedia.PROJECTION_All,
                            MyMediaDataContract.MyMedia.TITLE+" =?", new String[]{edit_title.getText().toString()},null);
                    if(cursor.getCount()>0)
                    {
                        cursor.moveToNext();
                        String url=cursor.getString(cursor.getColumnIndex(MyMediaDataContract.MyMedia.URL));
                        edit_url.setText(url);
                    }
                    else
                        Toast.makeText(getApplicationContext(),"Song Title not found", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"Please enter song title", Toast.LENGTH_SHORT).show();
                }

            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver contentResolver=getContentResolver();
                String title=edit_title.getText().toString();
                int counter;
                if(!TextUtils.isEmpty(edit_url.getText().toString()) && !TextUtils.isEmpty(edit_title.getText().toString()))
                {
                    ContentValues values=new ContentValues();
                    values.put(MyMediaDataContract.MyMedia.URL,edit_url.getText().toString());
                    counter=contentResolver.update(MyMediaDataContract.MyMedia.CONTENT_URI, values, MyMediaDataContract.MyMedia.TITLE + " =?", new String[]{title});

                    if(counter>0)
                    {
                        Toast.makeText(getApplicationContext(), "Record successfully updated", Toast.LENGTH_SHORT).show();
                        edit_title.setText("");
                        edit_url.setText("");
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Couldn't find record to update", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Please enter existing song title and url to update", Toast.LENGTH_SHORT).show();
                }

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver contentResolver=getContentResolver();
                int counter;
                if(!TextUtils.isEmpty(edit_title.getText().toString()))
                {
                    counter=contentResolver.delete(MyMediaDataContract.MyMedia.CONTENT_URI, MyMediaDataContract.MyMedia.TITLE + " =?",
                            new String[]{edit_title.getText().toString()});
                    if(counter>0)
                    {
                        Toast.makeText(getApplicationContext(), "Record successfully deleted", Toast.LENGTH_SHORT).show();
                        edit_title.setText("");
                        edit_url.setText("");
                    }else
                    {
                        Toast.makeText(getApplicationContext(), "Couldn't find record to delete", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Enter song title to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContentResolver contentResolver = getContentResolver();
                if (!TextUtils.isEmpty(edit_url.getText().toString()) && !TextUtils.isEmpty(edit_title.getText().toString())) {
                    ContentValues values = new ContentValues();
                    values.put(MyMediaDataContract.MyMedia.TITLE, edit_title.getText().toString());
                    values.put(MyMediaDataContract.MyMedia.URL, edit_url.getText().toString());
                    contentResolver.insert(MyMediaDataContract.MyMedia.CONTENT_URI, values);
                    Toast.makeText(getApplicationContext(), "Song has been added", Toast.LENGTH_SHORT).show();
                    edit_title.setText("");
                    edit_url.setText("");

                } else {
                    Toast.makeText(getApplicationContext(), "Please enter song title and url to add", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ////see list of streaming audio
        see_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getApplicationContext(), Stream_list.class);
                startActivityForResult(intent,1);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
                if(networkInfo!=null)
                {
                    if(!TextUtils.isEmpty(edit_url.getText().toString()) && !TextUtils.isEmpty(edit_title.getText().toString()))
                    {
                        Intent intent=new Intent();
                        intent.putExtra("uri", edit_url.getText().toString());
                        intent.putExtra("song_title",edit_title.getText().toString());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "Not connected to the internet", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK && requestCode==1)
        {
            //edit_url.setText(data.getStringExtra("edit_url"));
            edit_title.setText(data.getStringExtra("edit_title"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stream_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else
            onBackPressed();

        return super.onOptionsItemSelected(item);
    }
}
