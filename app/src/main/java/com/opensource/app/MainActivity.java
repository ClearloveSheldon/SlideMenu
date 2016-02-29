package com.opensource.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.opensource.slidemenu.SlideMenu;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SlideMenu slideMenu = (SlideMenu) findViewById(R.id.menu);
        ListView lv = (ListView) slideMenu.getContentView().findViewById(R.id.lv);
        final List<String> strs = new ArrayList<>();
        for (int i = 0;i<60;i++){
            strs.add("item"+i);
        }
        lv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return strs.size();
            }

            @Override
            public Object getItem(int i) {
                return strs.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view==null){
                    view = new TextView(MainActivity.this);
                }
                ((TextView)view).setText(strs.get(i));
                return view;
            }
        });
    }
}
