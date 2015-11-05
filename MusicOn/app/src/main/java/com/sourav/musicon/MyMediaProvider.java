package com.sourav.musicon;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MyMediaProvider extends ContentProvider {

    SQLiteDatabase db;
    MyHelper myHelper;

    private String NAME="MyMedia";
    private int version=1;
    final static String CREATE_DB="CREATE TABLE IF NOT EXISTS "+MyMediaDataContract.MyMedia.TABLE_NAME+"    ("+MyMediaDataContract.MyMedia._ID+"  INTEGER PRIMARY KEY AUTOINCREMENT, "+
            MyMediaDataContract.MyMedia.TITLE+" TEXT NOT NULL, "+ MyMediaDataContract.MyMedia.URL+" TEXT NOT NULL);";
    final static String DROP_TABLE="DROP TABLE "+MyMediaDataContract.MyMedia.TABLE_NAME+" IF EXISTS;";



    private static final int AUDIO=1;
    private static final int AUDIO_ID=2;

    private static final UriMatcher uriMatcher;
    static{
        uriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MyMediaDataContract.AUTHORITY, MyMediaDataContract.MyMedia.TABLE_NAME,AUDIO);
        uriMatcher.addURI(MyMediaDataContract.AUTHORITY,MyMediaDataContract.MyMedia.TABLE_NAME+"/#",AUDIO_ID);
    }



    public class MyHelper extends SQLiteOpenHelper{


        public MyHelper(Context context) {
            super(context, NAME, null, version);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_DB);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            sqLiteDatabase.execSQL(DROP_TABLE);
            onCreate(sqLiteDatabase);
        }
    }







    public MyMediaProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int number;
        db=myHelper.getWritableDatabase();
        switch(uriMatcher.match(uri))
        {
            case AUDIO:
                number=db.delete(MyMediaDataContract.MyMedia.TABLE_NAME,selection,selectionArgs);
                break;
            case AUDIO_ID:
                String where=MyMediaDataContract.MyMedia._ID+ " = "+ uri.getLastPathSegment();
                selection=selection+" AND "+ where;
                number=db.delete(MyMediaDataContract.MyMedia.TABLE_NAME,selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported uri "+uri);

        }

        if(number>0)
        {
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return number;
    }

    @Override
    public String getType(Uri uri) {
        switch(uriMatcher.match(uri))
        {
            case AUDIO:
                return MyMediaDataContract.MyMedia.CONTENT_TYPE;
            case AUDIO_ID:
                return MyMediaDataContract.MyMedia.CONTENT_ITEM_TYPE;
            default:
                return null;
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db=myHelper.getWritableDatabase();
        long row_ID=db.insertWithOnConflict(MyMediaDataContract.MyMedia.TABLE_NAME,null, values,SQLiteDatabase.CONFLICT_REPLACE);
        if(row_ID>0)
        {
            uri=ContentUris.withAppendedId(uri, row_ID);
            getContext().getContentResolver().notifyChange(uri, null);
            return uri;
        }
        return null;

    }

    @Override
    public boolean onCreate() {
        Context context=getContext();
        myHelper=new MyHelper(context);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        db=myHelper.getReadableDatabase();
        SQLiteQueryBuilder qb=new SQLiteQueryBuilder();
        Cursor cursor;
        switch (uriMatcher.match(uri))
        {
            case AUDIO:
                qb.setTables(MyMediaDataContract.MyMedia.TABLE_NAME);
                if(TextUtils.isEmpty(sortOrder))
                    sortOrder=MyMediaDataContract.MyMedia.DEFAULT_SORT;
                break;
            case AUDIO_ID:
                String where;
                where=MyMediaDataContract.MyMedia._ID+" = "+ uri.getLastPathSegment();
                selection=selection+" AND "+where;
                qb.setTables(MyMediaDataContract.MyMedia.TABLE_NAME);
                if(TextUtils.isEmpty(sortOrder))
                    sortOrder=MyMediaDataContract.MyMedia.DEFAULT_SORT;
                break;
            default:
                throw new IllegalArgumentException("Unsupported uri "+ uri);


        }
        cursor=qb.query(db,projection,selection,selectionArgs,null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int number;
        db=myHelper.getWritableDatabase();
        switch (uriMatcher.match(uri))
        {
            case AUDIO:
                number=db.update(MyMediaDataContract.MyMedia.TABLE_NAME,values, selection, selectionArgs);
                break;
            case AUDIO_ID:
                String where=MyMediaDataContract.MyMedia._ID+" = "+ uri.getLastPathSegment();
                selection=selection+" AND "+where;
                number=db.update(MyMediaDataContract.MyMedia.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Uri unsupported: "+uri);

        }
        if(number>0)
            getContext().getContentResolver().notifyChange(uri,null);
        return number;
    }
}
