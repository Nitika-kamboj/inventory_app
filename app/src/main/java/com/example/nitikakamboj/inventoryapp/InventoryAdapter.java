package com.example.nitikakamboj.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.nitikakamboj.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Nitika Kamboj on 15-07-2017.
 */

public class InventoryAdapter extends CursorAdapter {

 public InventoryAdapter(Context context,Cursor c){
  super(context,c,0);
 }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
       TextView nameView=(TextView) view.findViewById(R.id.name);
       TextView quantityView=(TextView)view.findViewById(R.id.current_quantity);
       TextView priceview =(TextView) view.findViewById(R.id.price);

       int nameColumn =cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int quantityColumn=cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
      int priceColumn=cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);

      String name=cursor.getString(nameColumn);
      String quantity = cursor.getString(quantityColumn);
        String price=cursor.getString(priceColumn);

      nameView.setText(name);
     quantityView.setText(String.valueOf(quantity));
      priceview.setText(String.valueOf(price));
    }


}
