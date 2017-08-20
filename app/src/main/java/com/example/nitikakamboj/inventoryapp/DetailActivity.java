package com.example.nitikakamboj.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nitikakamboj.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.nitikakamboj.inventoryapp.data.InventoryDbHelper;

/**
 * Created by Nitika Kamboj on 15-07-2017.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private static final int EXISTING_INVENTORY_LOADER=0;
    private static boolean mItemHasChanged=false;
    private Uri mCurrrentUri;
    SQLiteDatabase database;
    InventoryDbHelper dbHelper;
    Cursor cursor;
    SQLiteDatabase sqLiteDatabaseObj;
    String holder;

    public View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
              mItemHasChanged=true;
            return false;
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrrentUri = intent.getData();
       if(mCurrrentUri!=null)
       { getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);}

        mNameEditText = (EditText) findViewById(R.id.name_edit_field);
        mQuantityEditText = (EditText) findViewById(R.id.quantity_edit_field);
        mPriceEditText = (EditText) findViewById(R.id.price_edit_field);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        Button orderButton = (Button) findViewById(R.id.order_from_supplier);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetailActivity.this, getString(R.string.order_successful), Toast.LENGTH_LONG).show();
            }
        });

        Button incrementButton = (Button) findViewById(R.id.increase_quantity);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
                String quantity=mQuantityEditText.getText().toString();
                openSqliteDatabase();
                holder= "UPDATE" + InventoryEntry.TABLE_NAME + "SET" + InventoryEntry.COLUMN_QUANTITY;
            }
        });

         Button decrementButton = (Button) findViewById(R.id.decrease_quantity);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              decrement();
            }
        });

    }
    public void openSqliteDatabase(){
    sqLiteDatabaseObj=openOrCreateDatabase(InventoryDbHelper.DATABASE_NAME, Context.MODE_PRIVATE,null);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String []projection={InventoryEntry._ID,
        InventoryEntry.COLUMN_ITEM_NAME,
        InventoryEntry.COLUMN_QUANTITY,
        InventoryEntry.COLUMN_PRICE};
        return new CursorLoader(this,
                mCurrrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
     if(data==null||data.getCount()<1)
     {
     return;
     }
     if(data.moveToFirst())
     {
     int nameColumnIndex=data.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
     int quantityColumnIndex=data.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
     int priceColumnIndex=data.getColumnIndex(InventoryEntry.COLUMN_PRICE);

     String name=data.getString(nameColumnIndex);
       String quantity=data.getString(quantityColumnIndex);
      String price=data.getString(priceColumnIndex);

     mNameEditText.setText(name);
     mQuantityEditText.setText(String.valueOf(quantity));
     mPriceEditText.setText(String.valueOf(price));


     }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
     mNameEditText.setText("");
     mQuantityEditText.setText("");
     mPriceEditText.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu,menu);
        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       if(mCurrrentUri==null)
       {
           MenuItem menuItem= menu.findItem(R.id.action_delete);
           menuItem.setVisible(false);
       }
      return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId())
         {
             case R.id.action_save:
              saveItem();
              finish();
              return true;
             case R.id.action_delete:
              showDeleteConfirmationDialog();
              return true;
             case R.id.home:
              if(!mItemHasChanged)
              {
                  NavUtils.navigateUpFromSameTask(DetailActivity.this);
                  return true;
              }
                 DialogInterface.OnClickListener discardButtonClickListener=new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int i) {
                        NavUtils.navigateUpFromSameTask(DetailActivity.this);
                     }
                 };
              showUnsavedChangesDialog(discardButtonClickListener);
               return true;
         }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!mItemHasChanged){
        super.onBackPressed();
        return;}

        DialogInterface.OnClickListener discardButtonClickListener=new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
       private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener)
       {
           AlertDialog.Builder builder=new AlertDialog.Builder(this);
           builder.setMessage(R.string.unsaved_dialog_msg);
           builder.setPositiveButton(R.string.discard,discardButtonClickListener);
           builder.setNegativeButton(R.string.Keep_editing, new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int id) {
                if(dialog!=null)
                {
                  dialog.dismiss();
                }
               }
           });
           AlertDialog alertDialog = builder.create();
           alertDialog.show();
       }
       private void showDeleteConfirmationDialog()
       {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
             deleteItem();
            }
        });
       builder.setNegativeButton(R.string.delete_cancel, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int id) {
            if(dialog!=null)
            {
            dialog.dismiss();
            }
           }
       });
           AlertDialog alertDialog = builder.create();
           alertDialog.show();
       }
       private void deleteItem()
       {
        if(mCurrrentUri==null)
        {
        int rowsDeleted= getContentResolver().delete(mCurrrentUri,null,null);
         if(rowsDeleted==0)
         {
             Toast.makeText(this,getString(R.string.delete_item_failed),Toast.LENGTH_SHORT).show();
         }
         else
         {
         Toast.makeText(this,getString(R.string.delete_item_success),Toast.LENGTH_SHORT).show();
         }
        }
        finish();
       }
      private void saveItem()
      {
      String nameString=mNameEditText.getText().toString().trim();
      String quantityString =mQuantityEditText.getText().toString().trim();
      String priceString=mPriceEditText.getText().toString().trim();

      if(mCurrrentUri==null&& TextUtils.isEmpty(nameString)&&TextUtils.isEmpty(quantityString)&&TextUtils.isEmpty(priceString))
      {
      return;
      }
          ContentValues values=new ContentValues();
         values.put(InventoryEntry.COLUMN_ITEM_NAME,nameString);
         int quantity=0;
         int price=0;
         if(!TextUtils.isEmpty(quantityString))
         {
          quantity=Integer.parseInt(quantityString);
         }
         values.put(InventoryEntry.COLUMN_QUANTITY,quantity);

          if(!TextUtils.isEmpty(priceString))
          {
          price=Integer.parseInt(priceString);
          }
        values.put(InventoryEntry.COLUMN_PRICE,price);

          if(mCurrrentUri==null)
          {
           Uri newUri=getContentResolver().insert(InventoryEntry.CONTENT_URI,values);
           if(newUri==null)
           {
           Toast.makeText(this,getString(R.string.editor_item_save_failed),Toast.LENGTH_SHORT).show();
           }
           else
           {
            Toast.makeText(this,getString(R.string.editor_item_save_successful),Toast.LENGTH_SHORT).show();
           }
          }
          else
          {
          int rowsAffected=getContentResolver().update(mCurrrentUri,values,null,null);
          if(rowsAffected==0)
          {
           Toast.makeText(this,getString(R.string.editor_item_update_failed),Toast.LENGTH_SHORT).show();
          }
          else
          {
          Toast.makeText(this,getString(R.string.editor_item_update_successful),Toast.LENGTH_SHORT).show();
          }
          }
      }
      private void increment()
      { int quantity=0;
       String quantityString=mQuantityEditText.getText().toString().trim();
       if(!TextUtils.isEmpty(quantityString))
       {
        quantity=Integer.parseInt(quantityString);
       }
       quantity=quantity+1;
       mQuantityEditText.setText(String.valueOf(quantity));
      }

      private void decrement()
      {
       int quantity=0;
        String quantityString=mQuantityEditText.getText().toString().trim();
        if(!TextUtils.isEmpty(quantityString))
        {
        quantity=Integer.parseInt(quantityString);
        }
        quantity=quantity-1;
        if(quantity<0)
        {
         Toast.makeText(DetailActivity.this,"Quantity cannot be negative",Toast.LENGTH_SHORT).show();
         return;
        }
        mQuantityEditText.setText(String.valueOf(quantity));
      }

}
