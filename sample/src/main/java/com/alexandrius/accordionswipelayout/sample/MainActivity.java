package com.alexandrius.accordionswipelayout.sample;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.*;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Toast;

import com.alexandrius.accordionswipelayout.library.SwipeLayout;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new RecyclerAdapter());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

//        swipeLayout = (SwipeLayout) findViewById(R.id.swipe_layout);
//        swipeLayout.setOnSwipeItemClickListener(new SwipeLayout.OnSwipeItemClickListener() {
//            @Override
//            public void onSwipeItemClick(boolean left, int index) {
//                if (left) {
//                    switch (index) {
//                        case 0:
//                            showToast("REFRESH");
//                            break;
//                    }
//                } else {
//                    switch (index) {
//                        case 0:
//                            showToast("REFRESH");
//                            break;
//                        case 1:
//                            showToast("SETTINGS");
//                            break;
//                        case 2:
//                            showToast("TRASH");
//                            break;
//                    }
//                }
//            }
//        });
    }

    private void showToast(String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    public void collapse(View view) {
        ((SwipeLayout) recyclerView.findViewHolderForAdapterPosition(0).itemView).setItemState(SwipeLayout.ITEM_STATE_COLLAPSED, true);
    }
}
