package com.example.nitikakamboj.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.nitikakamboj.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
 private static final int PET_LOADER=0;
    InventoryAdapter mInventoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      FloatingActionButton fab=(FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,DetailActivity.class);
                startActivity(intent);
            }
        });

        ListView inventoryList= (ListView) findViewById(R.id.list);
           View emptyView =findViewById(R.id.empty_text_view);
         inventoryList.setEmptyView(emptyView);

        mInventoryAdapter=new InventoryAdapter(this,null);
        inventoryList.setAdapter(mInventoryAdapter);
        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent=new Intent(MainActivity.this,DetailActivity.class);
            Uri currentInventoryUri= ContentUris.withAppendedId(InventoryEntry.CONTENT_URI,id);
            intent.setData(currentInventoryUri);
            startActivity(intent);
            }
        });
        getLoaderManager().initLoader(PET_LOADER,null,this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection={InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
        InventoryEntry.COLUMN_QUANTITY,
        InventoryEntry.COLUMN_PRICE
        };
        return new CursorLoader(this,InventoryEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
     mInventoryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
     mInventoryAdapter.swapCursor(null);
    }

    private void deleteAllItems()
    {
    int rowsDeleted=getContentResolver().delete(InventoryEntry.CONTENT_URI,null,null);
    Log.v("MainActivity" ,rowsDeleted +"rows deleted from database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_delete_all_items:
             deleteAllItems();
             finish();
             return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
