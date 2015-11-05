package com.sourav.musicon;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Created by ASUS on 01/11/2015.
 */
public  final class MyMediaDataContract {
    public final static String AUTHORITY="com.sourav.musicon";
    public final static Uri CONTENT_URI=Uri.parse("content://"+AUTHORITY);

    public static final class MyMedia
    {
        public final static String TABLE_NAME="MyMedia";
        public final static String _ID="_id";
        public final static String TITLE="title";
        public final static String URL="url";

        public static final String[] PROJECTION_All={_ID,TITLE,URL};
        public static final String DEFAULT_SORT=TITLE+" ASC";

        public static final String CONTENT_TYPE= ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd/com.sourav.musicon/myMedia";
        public static final String CONTENT_ITEM_TYPE=ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd/com.sourav.musicon/myMedia/item";

        public  static Uri CONTENT_URI= Uri.withAppendedPath(MyMediaDataContract.CONTENT_URI,TABLE_NAME);

    }
}
