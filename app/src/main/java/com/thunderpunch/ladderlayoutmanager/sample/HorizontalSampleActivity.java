package com.thunderpunch.ladderlayoutmanager.sample;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.thunderpunch.ladderlayoutmanager.R;
import com.thunderpunch.ladderlayoutmanager.sample.view.HWRatioContainer;
import com.thunderpunch.lib.layoutmanager.LadderLayoutManager;
import com.thunderpunch.lib.layoutmanager.LadderSimpleSnapHelper;

/**
 * Created by thunderpunch on 2017/4/5
 * Description:
 */

public class HorizontalSampleActivity extends AppCompatActivity {
    LadderLayoutManager llm;
    RecyclerView rcv;
    HSAdapter adapter;
    int scrollToPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_sample);

        llm = new LadderLayoutManager(1.5f, 0.85f, LadderLayoutManager.HORIZONTAL).
                setChildDecorateHelper(new LadderLayoutManager.DefaultChildDecorateHelper(getResources().getDimension(R.dimen.item_max_elevation)));
        llm.setChildPeekSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                30, getResources().getDisplayMetrics()));
        llm.setMaxItemLayoutCount(5);

        rcv = (RecyclerView) findViewById(R.id.rcv);
        rcv.setLayoutManager(llm);
        new LadderSimpleSnapHelper().attachToRecyclerView(rcv);
        adapter = new HSAdapter();
        rcv.setAdapter(adapter);

        final SeekBar sb = (SeekBar) findViewById(R.id.sb);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                llm.setVanishOffset((progress - 10) * 1.0f / 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb.setProgress(8);
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.add:
                adapter.count += adapter.imgRes.length;
                adapter.notifyDataSetChanged();
                break;
            case R.id.remove:
                adapter.count -= adapter.imgRes.length;
                adapter.notifyDataSetChanged();
                break;
            case R.id.reverse:
                llm.setReverse(!llm.isReverse());
                break;
            case R.id.scroll:
                if (scrollToPosition >= adapter.count) scrollToPosition -= adapter.count;
                rcv.smoothScrollToPosition(scrollToPosition);
                scrollToPosition++;
                break;
        }
    }

    private class HSAdapter extends RecyclerView.Adapter<HSAdapter.VH> {
        int[] imgRes = {R.drawable.blastocyst_full, R.drawable.chub_full3, R.drawable.dukeofflies_full, R.drawable.fistula, R.drawable.gemini_full, R.drawable.larryjr_full, R.drawable.loki_full, R.drawable.monstro};
        int count = imgRes.length;

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(HorizontalSampleActivity.this).inflate(R.layout.item_horizontal, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            holder.iv.setImageResource(imgRes[position % imgRes.length]);
            holder.tv.setText("Position - " + position);
        }

        @Override
        public int getItemCount() {
            return count;
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView iv;
            TextView tv;
            HWRatioContainer ivc;

            public VH(View itemView) {
                super(itemView);
                iv = (ImageView) itemView.findViewById(R.id.iv);
                tv = (TextView) itemView.findViewById(R.id.tv);
                ivc = (HWRatioContainer) itemView.findViewById(R.id.ivc);
                ivc.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ivc.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            ivc.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                        ivc.setTranslationX(ivc.getWidth() >> 4);
                    }
                });

            }
        }
    }
}
