package com.thunderpunch.ladderlayoutmanager.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.thunderpunch.ladderlayoutmanager.R;
import com.thunderpunch.ladderlayoutmanager.sample.view.VerticalSampleItemLayout;
import com.thunderpunch.lib.layoutmanager.LadderLayoutManager;
import com.thunderpunch.lib.layoutmanager.LadderSimpleSnapHelper;

/**
 * Created by thunderpunch on 2017/4/4
 * Description:
 */

public class VerticalSampleActivity extends AppCompatActivity {
    LadderLayoutManager llm;
    RecyclerView rcv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_sample);
        llm = new LadderLayoutManager(0.8f).setChildDecorateHelper(new VerticalSampleChildDecorateHelper(getResources().getDimension(R.dimen.item_max_elevation)));
        llm.setMaxItemLayoutCount(7);// 子布局数量限制
        llm.setChildPeekSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, getResources().getDisplayMetrics()));
        rcv = (RecyclerView) findViewById(R.id.rcv);
        rcv.setLayoutManager(llm);
        new LadderSimpleSnapHelper().attachToRecyclerView(rcv);
        rcv.setAdapter(new VSAdapter());
    }

    private class VSAdapter extends RecyclerView.Adapter<VSAdapter.VH> {
        private int[] colors = {0xff03a9f4, 0xff259b24, 0xffffeb3b, 0xffff5722, 0xffe51c23, 0xff673ab7};
        private int count = 20;


        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(VerticalSampleActivity.this).inflate(R.layout.item_vertical, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.v.setStrokeColor(colors[position % colors.length]);
        }

        @Override
        public int getItemCount() {
            return count;
        }

        class VH extends RecyclerView.ViewHolder {
            VerticalSampleItemLayout v;
            View addV;

            public VH(View itemView) {
                super(itemView);
                v = (VerticalSampleItemLayout) itemView.findViewById(R.id.container);
                addV = v.findViewById(R.id.add);
                addV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rcv.smoothScrollToPosition(getLayoutPosition());
                    }
                });
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_horizontal) {
            startActivity(new Intent(VerticalSampleActivity.this, HorizontalSampleActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
