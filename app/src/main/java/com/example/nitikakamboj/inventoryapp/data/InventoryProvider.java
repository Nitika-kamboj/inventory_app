package com.example.nitikakamboj.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.nitikakamboj.inventoryapp.data.InventoryContract.InventoryEntry;


public class InventoryProvider extends ContentProvider {
    public InventoryDbHelper mDatabase;
    public static final int INVENTORY=100;
    public static final int INVENTORY_ID=101;
    public static final UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);
    public static final String LOG_TAG= InventoryProvider.class.getSimpleName();

    static {
     sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY,INVENTORY);
     sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY +"/#",INVENTORY_ID);
    }
    @Override
    public boolean onCreate() {

        mDatabase=new InventoryDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query( Uri uri,  String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database=mDatabase.getReadableDatabase();
        Cursor cursor=null;
        int match=sUriMatcher.match(uri);
        switch (match)
        {
            case INVENTORY:
            cursor=database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
            break;
            case INVENTORY_ID:
            selection=InventoryEntry._ID + "=?";
            selectionArgs=new String []{String.valueOf(ContentUris.parseId(uri))};
            cursor=database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
            break;
            default:
            throw new IllegalArgumentException("cannot query unknown query" +uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;

    }


    @Override
    public String getType(Uri uri) {
       final int match=sUriMatcher.match(uri);
        switch(match)
        {
            case INVENTORY:
            return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
             return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
            throw new IllegalStateException("Unknown uri"+uri);
        }

    }


    @Override
    public Uri insert(Uri uri,  ContentValues values) {
        final int match =sUriMatcher.match(uri);
        switch (match)
        {
            case INVENTORY:
           return insertItem(uri,values);
           default:
           throw  new IllegalArgumentException("cannot insert for this uri"+uri);
        }
    }

    public Uri insertItem(Uri uri,ContentValues values)
    {
     String name=values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
     if(name==null)
     {
     throw new IllegalArgumentException("Item requires a name");
     }
     Integer quantity=values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
     if(quantity==null&&quantity<0)
     {
     throw new IllegalArgumentException("Item requires a quantity");
     }
    Integer price=values.getAsInteger(InventoryEntry.COLUMN_PRICE);
     if(price==null&&price<0)
     {
      throw new IllegalArgumentException("Item requires price");
     }
     SQLiteDatabase database=mDatabase.getWritableDatabase();
     long id=database.insert(InventoryEntry.TABLE_NAME,null,values);
      if(id==-1)
      {
          Log.e(LOG_TAG, "Failed to insert row for " + uri);
          return null;
      }
      getContext().getContentResolver().notifyChange(uri,null);
      return ContentUris.withAppendedId(uri,id);
    }

    @Override
    public int delete( Uri uri, String selection,  String[] selectionArgs) {
        int rowsDeleted;
        SQLiteDatabase database=mDatabase.getWritableDatabase();
        final int match=sUriMatcher.match(uri);
        switch (match)
        {
            case INVENTORY:
           rowsDeleted=database.delete(InventoryEntry.TABLE_NAME,selection,selectionArgs);
            if(rowsDeleted!=0)
            {
            getContext().getContentResolver().notifyChange(uri,null);
            }
            return rowsDeleted;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            rowsDeleted=database.delete(InventoryEntry.TABLE_NAME,selection,selectionArgs);
            if(rowsDeleted!=0)
            {
            getContext().getContentResolver().notifyChange(uri,null);
            }
          return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for" + uri);
        }
    }

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match=sUriMatcher.match(uri);
        switch (match)
        {
            case INVENTORY:
            return updateItem(uri,values,selection,selectionArgs);
            case INVENTORY_ID:
            selection=InventoryEntry._ID + "=?";
            selectionArgs=new String[]{String.valueOf(ContentUris.parseId(uri))};
            return updateItem(uri,values,selection,selectionArgs);
            default:
            throw new IllegalArgumentException("Update failed for this uri"+uri);
        }
    }
    public int updateItem(Uri uri,ContentValues values,String selection,String[] selectionArgs)
    {
      if(values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)){
      String name=values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
      if(name==null)
      {
       throw new IllegalArgumentException("cannot update for this uri"+uri);
      }
      }

      if(values.containsKey(InventoryEntry.COLUMN_QUANTITY))
      {
      Integer quantity=values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
      if(quantity==null&&quantity<0)
      {
      throw new IllegalArgumentException("cannot update for this uri +uri");
      }
      }

      if(values.containsKey(InventoryEntry.COLUMN_PRICE))
      {
      Integer price=values.getAsInteger(InventoryEntry.COLUMN_PRICE);
      if(price==null&&price<0)
      {
      throw new IllegalArgumentException("cannot update for this uri"+uri);
      }
      }
        if (values.size() == 0) {
            return 0;
        }
      SQLiteDatabase database=mDatabase.getWritableDatabase();
      int rowsUpdated=database.update(InventoryEntry.TABLE_NAME,values,selection,selectionArgs);
      if(rowsUpdated!=0)
      {
       getContext().getContentResolver().notifyChange(uri,null);
      }
      return rowsUpdated;
    }
}
