package com.dgsd.android.graphing.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.dgsd.android.graphing.BarGraph;

public class DemoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout ll = new LinearLayout(this);
        ll.setPadding(50, 50, 50, 50);

        final BarGraph graph = new BarGraph(this);
        graph.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));
        ll.addView(graph);

        //For testing..
        graph.add("Jan", 25, 0xFF558A06);
        graph.add("Feb", 50, 0xFF558A06);
        graph.add("Mar", 75, 0xFF558A06);
        graph.add("Apr", 100, 0xFF558A06);

        graph.postDelayed(new Runnable() {
            @Override
            public void run() {
                graph.animateGraph();
            }
        }, 500);

        setContentView(ll);
    }

}
