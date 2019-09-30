package com.example.pinhsuanho.pms5003;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class chart extends AppCompatActivity {

    LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chart);
        mChart= (LineChart) findViewById(R.id.lineChart);
        //创建描述信息
//        Description description =new Description();
//        description.setText("测试图表");
//        description.setTextColor(Color.RED);
//        description.setTextSize(20);
//        lineChar.setDescription(description);//设置图表描述信息
//        lineChar.setNoDataText("没有数据熬");//没有数据时显示的文字
//        lineChar.setNoDataTextColor(Color.BLUE);//没有数据时显示文字的颜色
//        lineChar.setDrawGridBackground(false);//chart 绘图区后面的背景矩形将绘制
//        lineChar.setDrawBorders(false);//禁止绘制图表边框的线

        /* 使用 setData 的方法設定 LineData 物件 */


        LineDataSet dataSet = new LineDataSet(dataValues1(), "Data Set 1");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        mChart.setData(lineData);

        // 圖表基本設定
        //mChart.setDescription(null);

        // 設定其他樣式
        //setupXAxis();
        //setupYAxis();
        //setupLegend();
        //setupViewPort();

        mChart.invalidate();
        //mChart.notifyDataSetChanged();

    }
    private ArrayList<Entry> dataValues1()
    {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(0, 4f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(2, 6f));
        entries.add(new Entry(3, 2f));
        entries.add(new Entry(4, 18f));
        entries.add(new Entry(5, 9f));
        return entries;
    }

    private void setupXAxis() {
        XAxis mXAxis = mChart.getXAxis();
        mXAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
        mXAxis.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
        mXAxis.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);
    }

    private void setupYAxis() {
        YAxis mLeftYAxis = mChart.getAxis(YAxis.AxisDependency.LEFT);
        YAxis mRightYAxis = mChart.getAxis(YAxis.AxisDependency.RIGHT);

        mLeftYAxis.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
        mRightYAxis.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
        mLeftYAxis.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);
        mRightYAxis.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);

        mLeftYAxis.setTextColor(ColorTemplate.PASTEL_COLORS[0]);
        mRightYAxis.setTextColor(ColorTemplate.PASTEL_COLORS[0]);

    }

    private void setupViewPort(){

    }

    private void setupLegend(){
        Legend lg = mChart.getLegend();
        lg.setXEntrySpace(20f);
    }

    /* 將 DataSet 資料整理好後，回傳 LineData */
    private LineData getLineData() {
//        final int DATA_COUNT = 5;
//
//        // LineDataSet(List<Entry> 資料點集合, String 類別名稱)
//        LineDataSet dataSetA = new LineDataSet(getChartData(DATA_COUNT, 1), "Apple");
//        LineDataSet dataSetB = new LineDataSet(getChartData(DATA_COUNT, 3), "Banana");
//
//        // 設定 datasetA 的 data point 樣式
//        dataSetA.setColor(ColorTemplate.JOYFUL_COLORS[0]);
//        dataSetA.setCircleColor(ColorTemplate.JOYFUL_COLORS[0]);
//        dataSetA.setDrawCircleHole(true);
////        dataSetA.setCircleColorHole(Color.WHITE);
////        dataSetA.setCircleSize(4f);
////        dataSetA.setDrawCubic(true);
//
//        // 設定 datasetB 的 data point 樣式
//        dataSetB.setColor(ColorTemplate.JOYFUL_COLORS[1]);
//        dataSetB.setCircleColor(ColorTemplate.JOYFUL_COLORS[1]);
//        dataSetB.setDrawCircleHole(true);
////        dataSetB.setCircleColorHole(Color.WHITE);
////        dataSetB.setCircleSize(4f);
////        dataSetB.setDrawCubic(true);
//
//        List<LineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(dataSetA);
//        dataSets.add(dataSetB);
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 4f));
        entries.add(new Entry(1, 8f));
        entries.add(new Entry(2, 6f));
        entries.add(new Entry(3, 2f));
        entries.add(new Entry(4, 18f));
        entries.add(new Entry(5, 9f));

        LineDataSet dataSet = new LineDataSet(entries, "# of Calls");
        LineData lineData = new LineData(dataSet);

        // LineData(List<String> Xvals座標標籤, List<Dataset> 資料集)
        //LineData data = new LineData(getLabels(DATA_COUNT), dataSets);

        return lineData;

    }

    /* 取得 List<Entry> 的資料給 DataSet */
    private List<Entry> getChartData(int count, int ratio) {

        List<Entry> chartData = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int _ratio = (i % 2 == 0) ? ratio : -ratio;

            // Entry(value 數值, index 位置)
            chartData.add(new Entry(i * _ratio, i));
        }
        return chartData;
    }

    /* 取得 XVals Labels 給 LineData */
    private List<String> getLabels(int count) {

        List<String> chartLabels = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            chartLabels.add("X" + i);
        }
        return chartLabels;
    }
}

//
//    /* 將 DataSet 資料整理好後，回傳 LineData */
//    private LineData getLineData(){
//        final int DATA_COUNT = 5;  //資料數固定為 5 筆
//
//        // LineDataSet(List<Entry> 資料點集合, String 類別名稱)
//        LineDataSet dataSetA = new LineDataSet( getChartData(DATA_COUNT, 1), "A");
//        LineDataSet dataSetB = new LineDataSet( getChartData(DATA_COUNT, 2), "B");
//
//        List<LineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(dataSetA);
//        dataSets.add(dataSetB);
//
//        // LineData(List<String> Xvals座標標籤, List<Dataset> 資料集)
//        LineData data = new LineData( getLabels(DATA_COUNT), dataSets);
//
//        return data;
//
//    }
//
//    /* 取得 List<Entry> 的資料給 DataSet */
//    private List<Entry> getChartData(int count, int ratio){
//
//        List<Entry> chartData = new ArrayList<>();
//        for(int i=0;i<count;i++){
//            // Entry(value 數值, index 位置)
//            chartData.add(new Entry( i*2*ratio, i));
//        }
//        return chartData;
//    }
//
//    /* 取得 XVals Labels 給 LineData */
//    private List<String> getLabels(int count){
//
//        List<String> chartLabels = new ArrayList<>();
//        for(int i=0;i<count;i++) {
//            chartLabels.add("X" + i);
//        }
//        return chartLabels;
//    }
//}
