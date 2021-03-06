package com.example.he.Activity;
/**
*多个电池曲线绘制Acivity
*
*author 陈鹤
*created 16/9/13 下午3:25
*/
import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.he.MyTools.MyValueFormatter;
import com.example.he.Database.MySQLiteOpenHelper;
import com.example.he.batteryinfoActivity.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiLineChartActivity extends Activity {

    private List<String> mlist = new ArrayList<>();
    private String tablename;
    private MySQLiteOpenHelper helper;
    private SQLiteDatabase database;
    private LineChart mlinechart1;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_line_chart);
        mlist = (List<String>)getIntent().getSerializableExtra("Battery_No");
        tablename = "TestID_" + getIntent().getExtras().getString("expid");

        mlinechart1 = (LineChart) findViewById(R.id.line_chart_1);
        setLineChart(mlinechart1);
        loadLineChartData(mlinechart1);

        button = (Button) findViewById(R.id.button_reset);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mlinechart1.fitScreen();
            }
        });
    }

    /**
     * 设置折现图的样式
     * @param chart
     */
    private void setLineChart(LineChart chart) {

        chart.setDescription("Cycle_Times - Degradion_amended");
        chart.setDrawGridBackground(false);//设置网格背景
        chart.setScaleEnabled(true);//设置缩放
        chart.setDoubleTapToZoomEnabled(false);//设置双击不进行缩放
        chart.enableScroll();

        //设置X轴
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴的位置
//        xAxis.setTypeface(mTf);//设置字体
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);

        //获得左侧侧坐标轴
        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setTypeface(mTf);
        leftAxis.setLabelCount(5,false);
//        leftAxis.setAxisLineWidth(1.5f);
        leftAxis.setValueFormatter(new MyValueFormatter());
        leftAxis.setDrawGridLines(false);


        //设置右侧坐标轴
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawAxisLine(false);//右侧坐标轴线
        rightAxis.setDrawLabels(false);//右侧坐标轴数组Lable
//        rightAxis.setTypeface(mTf);
//        rightAxis.setLabelCount(5);
//        rightAxis.setDrawGridLines(false);
    }

    /**
     * 为折线图设置数据
     * @param chart
     */
    private void loadLineChartData(LineChart chart){
        //所有线的List
        ArrayList<ILineDataSet> allLinesList = new ArrayList<ILineDataSet>();

        String batteryid,sql;
        int maxX = 0;
        helper = MySQLiteOpenHelper.getInstance(MultiLineChartActivity.this);
        database = helper.getReadableDatabase();
        for(int i=0;i<mlist.size();i++){
            batteryid = mlist.get(i);
            sql = "select Cycle_Times,Degradion_amended from " + tablename + " where Battery_NO = '"
                    + batteryid + "' order by Cycle_Times";
            Cursor cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            ArrayList<Entry> entryList = new ArrayList<>();
            try {
                while (!cursor.isAfterLast()){
                    entryList.add(new Entry(Float.parseFloat(cursor.getString(1))
                            ,Integer.parseInt(cursor.getString(0))));
                    cursor.moveToNext();
                }
            }catch (Exception e){
                Toast.makeText(this,"存在数据不完整",Toast.LENGTH_SHORT).show();
            }
            LineDataSet dataSet = new LineDataSet(entryList,batteryid);
            dataSet.setLineWidth(2.5f);
            dataSet.setCircleRadius(2.5f);
            //dataSet.setHighLightColor(Color.RED);//设置点击某个点时，横竖两条线的颜色
            dataSet.setDrawValues(false);//是否在点上绘制Value
            int color = getRandColorCode();
            dataSet.setColor(color);
            dataSet.setCircleColor(color);

            allLinesList.add(dataSet);

            cursor.moveToLast();

            if(!cursor.isBeforeFirst() && maxX<Integer.valueOf(cursor.getString(0))){
                maxX = Integer.valueOf(cursor.getString(0));
            }
        }

        //LineData表示一个LineChart的所有数据(即一个LineChart中所有折线的数据)
        LineData mChartData = new LineData(getXAxisShowLable(maxX),allLinesList);

        // set data
        chart.setData((LineData) mChartData);
        chart.animateX(1500);//设置动画
    }

    private ArrayList<String> getXAxisShowLable(int max) {
        ArrayList<String> m = new ArrayList<String>();
        for (int i=0;i<=max;i++){
            m.add(String.valueOf(i));
        }
        return m;
    }

    public static int getRandColorCode(){
        int r,g,b;
        Random random = new Random();
        r = random.nextInt(256);
        g = random.nextInt(256);
        b = random.nextInt(256);

//        r = r.length()==1 ? "0" + r : r ;
//        g = g.length()==1 ? "0" + g : g ;
//        b = b.length()==1 ? "0" + b : b ;

        int color = Color.argb(150,r,g,b);
        return color;
    }
}
