package com.life.yiwanchen.barcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class FormListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerListView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
//        dividerItemDecoration.setDrawable(new ColorDrawable(ContextCompat.getColor(this,R.color.colorPrimaryDark)));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        Intent intent = getIntent();

        String formNo = intent.getStringExtra("FormNo");

        ArrayList<FormItem> formList = intent.getParcelableArrayListExtra("FormList");

        getSupportActionBar().setTitle(formNo);


        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(formList);
        mRecyclerView.setAdapter(mAdapter);

    }


}
