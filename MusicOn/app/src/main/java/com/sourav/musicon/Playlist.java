package com.sourav.musicon;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Playlist extends ListActivity {

    String [] examples;

    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        examples=getResources().getStringArray(R.array.examples);

        ContentResolver cr=getContentResolver();
        Uri uri= MediaStore.Files.getContentUri("external");
        String[] projection=null;
        String sortOrder=MediaStore.MediaColumns.TITLE+" ASC";
        String selectionMimeType=MediaStore.Files.FileColumns.MIME_TYPE +"=?";
        String mimeType= MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] selectionArgs={mimeType};

        String [] from={MediaStore.MediaColumns.TITLE};
        int[] to={R.id.song_title};
        Cursor cursor=cr.query(uri,projection,selectionMimeType,selectionArgs,sortOrder);
        final ArrayList<String> ids=new ArrayList<>();
        final ArrayList<String> song_title=new ArrayList<>();
        int id=cursor.getColumnIndex(MediaStore.Audio.Media._ID);
        while(cursor.moveToNext())
        {
            ids.add(cursor.getString(id));
        }
        cursor.moveToFirst();
        ListAdapter listAdapter=new SimpleCursorAdapter(this,R.layout.list_item,cursor,from,to, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        this.setListAdapter(listAdapter);
        ListView lv=getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int song_index=i;
                Intent intent =new Intent(getApplicationContext(), Main_screen.class);
                intent.putExtra("song_id", ids.get(i));
                LinearLayout linearLayout=(LinearLayout)view;
                String song_title=((TextView)linearLayout.findViewById(R.id.song_title)).getText().toString();
                intent.putExtra("song_title",song_title);
                Log.d("player", (ids.get(i)));
                setResult(RESULT_OK, intent);
                finish();
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
