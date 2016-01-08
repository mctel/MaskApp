package com.stollmann.tiov2sample;


import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import com.stollmann.shared.STTrace;
import com.stollmann.shared.STUtil;
import com.stollmann.terminalIO.TIOManager;
import com.stollmann.terminalIO.TIOPeripheral;
import com.stollmann.terminalIO.TIOPeripheralCallback;
import com.stollmann.custom.MyYAxisValueFormatter;


import android.os.Handler;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.SystemClock;
import android.view.View;
import android.view.Window;
import android.widget.ToggleButton;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

//import edu.emory.mathcs.jtransforms.fft.*;
//import edu.emory.mathcs.utils.*;
import org.jtransforms.fft.*;
import org.jtransforms.utils.*;

public class PeripheralActivity extends DemoBase implements TIOPeripheralCallback,
		OnChartGestureListener, OnChartValueSelectedListener, AdapterView.OnItemSelectedListener {

	private static final int RSSI_INTERVAL = 1670;
	private static final int NUM_PACKAGES = 5;

	private TIOPeripheral _peripheral;
	private Handler _rssiHandler = new Handler();
	private Runnable _rssiRunnable;

	private TextView _frequencyTextView;
	private TextView _timeTextView;
	private ToggleButton _connectionButton;
	private ToggleButton _flowBtn;
	private ToggleButton _volumeBtn;
	private ToggleButton _CO2Btn;

	private Spinner _commandSpinner;
	private long startTime = 0L;
	private long endTime = 0L;
	int cont=0;

	//___________________CHART______________
	private LineChart mChart;
	public static byte [] data2;
	public static boolean chartFlag= false, chartFlag2=false;
	ArrayList<String> xVals = new ArrayList<String>();
	ArrayList<Entry> yVals = new ArrayList<Entry>();
	ArrayList<Entry> yVals2 = new ArrayList<Entry>();
	ArrayList<Entry> yVals3 = new ArrayList<Entry>();
	LineData dataVolume;
	LineData dataFlow;
	LineData dataCO2;
	int contY1=0, contY2=0, contY3=0;
	double time=0.0;
	public static String textToSend;
	boolean firstTime=true;
	//______________________________________
	BigDecimal timeInMilliseconds;
	BigDecimal thousand = new BigDecimal("1000");
	BigDecimal numP = new BigDecimal(NUM_PACKAGES);
	BigDecimal one = new BigDecimal("1");
	BigDecimal timeInSeconds;
	BigDecimal freq;

	//________PROGRESS BAR__________________
	private ProgressBar _pbCO2;
	private int mProgressStatus = 0;
	//_______________________________________

	//________FOURIER TRANSFORM__________________
	private final int fftWindowSize = 1024;
	DoubleFFT_1D fft = new DoubleFFT_1D(fftWindowSize);
	private int fftCounter = 0;
	private double[] fftArray = new double[fftWindowSize];
	//_______________________________________

	//******************************************************************************
	// Activity overrides 
	//******************************************************************************

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		STTrace.method("onCreate");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peripheral);
		// Show the Up button in the action bar.
		//setupActionBar();

		this.connectViews();
		this.connectPeripheral();
		this.updateUIState();
		this.displayVersionNumber();

		//_____________________CHART__________________________________________

		mChart = (LineChart) findViewById(R.id.chart1);
		mChart.setOnChartGestureListener(this);
		mChart.setOnChartValueSelectedListener(this);
		mChart.setDrawGridBackground(false);

		// no description text
		mChart.setDescription("");
		mChart.setNoDataTextDescription("You need to provide data for the chart.");
		// enable touch gestures
		mChart.setTouchEnabled(true);

		// enable scaling and dragging
		mChart.setDragEnabled(true);
		mChart.setScaleEnabled(true);

		// if disabled, scaling can be done on x- and y-axis separately
		mChart.setPinchZoom(true);

		MyYAxisValueFormatter valueFormatterY = new MyYAxisValueFormatter();

		YAxis leftAxis = mChart.getAxisLeft();
		leftAxis.setValueFormatter(valueFormatterY);
		leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		leftAxis.setStartAtZero(false);

		Legend legend = mChart.getLegend();
		legend.setEnabled(false);

		// limit lines are drawn behind data (and not on top)
		leftAxis.setDrawLimitLinesBehindData(true);

		//MyXAxisValueFormatter valueformatterX = new MyXAxisValueFormatter();
		XAxis xAxis = mChart.getXAxis();
		//xAxis.setValueFormatter(valueformatterX);
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setSpaceBetweenLabels(2);

		mChart.getAxisRight().setEnabled(false);

		//mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);

		// get the legend (only possible after setting data)
		Legend l = mChart.getLegend();

		// modify the legend ...
		l.setForm(Legend.LegendForm.LINE);

		// // dont forget to refresh the drawing
		mChart.invalidate();
		//_______________________________________________________________


		_pbCO2.setMax(1100);


	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// Checks the orientation of the screen
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	/*private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}*/


	@Override
	protected void onResume() {
		STTrace.method("onResume");

		if (this._peripheral.isConnected()) {
			this.startRSSITimer();
			//this._localCreditsTextView.setText(Integer.toString(this._peripheral.getLocalUARTCreditsCount()));
			//this._remoteCreditsTextView.setText(Integer.toString(this._peripheral.getRemoteUARTCreditsCount()));
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		STTrace.method("onPause");

		this.stopRSSITimer();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		STTrace.method("onDestroy");

		this._peripheral.setListener(null);

		super.onDestroy();
	}


	//******************************************************************************
	// UI event handlers 
	//******************************************************************************

	public void onConnectButtonPressed() {
		STTrace.method("onConnectButtonPressed");

		this._peripheral.connect();
		this.updateUIState();

		while (this._peripheral.isConnecting()){
			//_connectionButton.setText(R.string.connecting);
		}
		_connectionButton.setTextOff(getString(R.string.toggle_off));

	}

	public void onDisconnectButtonPressed() {
		STTrace.method("onDisconnectButtonPressed");

		this.stopRSSITimer();
		this._peripheral.disconnect();
		_connectionButton.setTextOn(getString(R.string.toggle_on));
	}

	//******************************************************************************
	// TIOPeripheralCallback implementation 
	//******************************************************************************

	@Override
	public void tioPeripheralDidConnect(TIOPeripheral peripheral) {
		STTrace.method("tioPeripheralDidConnect");

		this.updateUIState();
		this.startRSSITimer();

		if (!this._peripheral.shallBeSaved())
		{
			// save if connected for the first time
			this._peripheral.setShallBeSaved(true);
			TIOManager.sharedInstance().savePeripherals();
		}
	}

	@Override
	public void tioPeripheralDidFailToConnect(TIOPeripheral peripheral, String errorMessage) {
		STTrace.method("tioPeripheralDidFailToConnect", errorMessage);

		this.updateUIState();

		if (errorMessage.length() > 0) {
			STUtil.showErrorAlert("Failed to connect with error message: " + errorMessage, this);
		}
	}

	@Override
	public void tioPeripheralDidDisconnect(TIOPeripheral peripheral, String errorMessage) {
		STTrace.method("tioPeripheralDidDisconnect", errorMessage);

		this.stopRSSITimer();
		this.updateUIState();

		if (errorMessage.length() > 0) {
			STUtil.showErrorAlert("Disconnected with error message: " + errorMessage, this);
		}
	}

	public static SpiroMeasurement measurement1 = new SpiroMeasurement();
	SpiroMeasurement measurement2= new SpiroMeasurement();

	@Override
	public void tioPeripheralDidReceiveUARTData(TIOPeripheral peripheral, byte[] data) {
		STTrace.method("tioPeripheralDidReceiveUARTData", STTrace.byteArrayToString(data));

		//Log.i("info","Received " + data.length + " bytes");
		try {

			switch (data[0]){
				case CommandType.Data:
					DataCommand newData = new DataCommand(data);
					break;
				case CommandType.Settings:

				default:
					return;
			}

			DataCommand newData = new DataCommand(data);
			data2 = data.clone();

			for (int i=0; i<4;i++) {
				measurement1.Append(newData.Channel1[i]);
				measurement2.Append(newData.Channel2[i]);
				setData(i);

				fftArray[fftCounter] = measurement1.getLastValue().getFlow();
				fftCounter++;

				Log.i("TimeKcal", measurement1.listBreathing.toStringKcalTime());
				//Log.i("TimeKcal", measurement2.listBreathing.toStringKcalTime());

				//Once we have a complet window, we calculate the FFT of the whole and get the maxValue
				if(fftCounter == fftWindowSize - 1) {

					fft.realForward(fftArray);

					double maxValue = 0;
					int maxIndex = 0;

					for (int k = 0; k < fftArray.length;k++){
						if(fftArray[k] > maxValue){
							maxValue = fftArray[k];
							maxIndex = k;
						}
					}

					Log.i("FFT","MaxIndex: "+maxIndex);
					Log.i("FFT","Freq: " + ((double)maxIndex * 100.0 / (double)fftWindowSize));

					fftCounter = 0;
				}
			}

			//Log.i("Flow: ", String.valueOf(measurement1.getLastValue().getFlow()));
			//Log.i("Time", String.valueOf(measurement.getLastValue().getTime()));
			//Log.i("Volume", String.valueOf(measurement.getLastValue().getVolume()));

			setFrequency();

		} catch (Exception ex) {
			STTrace.exception(ex);
		}
	}

	@Override
	public void tioPeripheralDidWriteNumberOfUARTBytes(TIOPeripheral peripheral, int bytesWritten) {
		STTrace.method("tioPeripheralDidWriteNumberOfUARTBytes", Integer.toString(bytesWritten));

	}

	@Override
	public void tioPeripheralUARTWriteBufferEmpty(TIOPeripheral peripheral) {
		STTrace.method("tioPeripheralUARTWriteBufferEmpty");

	}

	@Override
	public void tioPeripheralDidUpdateAdvertisement(TIOPeripheral peripheral) {
		STTrace.method("tioPeripheralDidUpdateAdvertisement");

		// display peripheral properties
		//this._mainTitleTextView.setText(this._peripheral.getName() + "  " + this._peripheral.getAddress());
		//this._subTitleTextView.setText(this._peripheral.getAdvertisementDisplayString());
	}

	@Override
	public void tioPeripheralDidUpdateRSSI(TIOPeripheral peripheral, int rssi) {
		STTrace.method("tioPeripheralDidUpdateRSSI", Integer.toString(rssi));

		//this._rssiTextView.setText(Integer.toString(rssi));
	}

	@Override
	public void tioPeripheralDidUpdateLocalUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
		STTrace.method("tioPeripheralDidUpdateLocalUARTCreditsCount", Integer.toString(creditsCount));

		//this._localCreditsTextView.setText(Integer.toString(creditsCount));
	}

	@Override
	public void tioPeripheralDidUpdateRemoteUARTCreditsCount(TIOPeripheral peripheral, int creditsCount) {
		STTrace.method("tioPeripheralDidUpdateRemoteUARTCreditsCount", Integer.toString(creditsCount));

		//this._remoteCreditsTextView.setText(Integer.toString(creditsCount));
	}


	//******************************************************************************
	// Internal methods 
	//******************************************************************************

	private void connectViews() {
		STTrace.method("connectViews");


		this._connectionButton = (ToggleButton)this.findViewById(R.id.toggleConnect);
		this._frequencyTextView = (TextView)this.findViewById(R.id.frequency);
		this._timeTextView = (TextView)this.findViewById(R.id.time);
		this._commandSpinner = (Spinner)findViewById(R.id.spinnerCommands);
		this._volumeBtn = (ToggleButton)findViewById(R.id.toggleVolume);
		this._flowBtn = (ToggleButton)findViewById(R.id.toggleFlow);
		this._CO2Btn = (ToggleButton)findViewById(R.id.toggleCO2);
		this._pbCO2 = (ProgressBar)findViewById(R.id.pbCO2);

		//this._clearButton.setEnabled(true);


		_connectionButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked && !_peripheral.isConnected() &&!_peripheral.isConnecting()){
					onConnectButtonPressed();
				} else if(_peripheral.isConnected()){
					onDisconnectButtonPressed();
				}
			}
		});

		_volumeBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					_flowBtn.setChecked(false);
					_CO2Btn.setChecked(false);
					chartFlag=true;
					chartFlag2=true;
					mChart.getAxisLeft().setAxisMaxValue(2f);
					mChart.getAxisLeft().setAxisMinValue(0f);
					mChart.invalidate();
					mChart.setData(dataVolume);
				}
				time = measurement1.getLastValue().getTime();
				_timeTextView.setText("Time: " + String.format("%1$,.2f", time) + " s");
			}
		});

		_flowBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					_volumeBtn.setChecked(false);
					_CO2Btn.setChecked(false);
					chartFlag = false;
					chartFlag2 = false;
					mChart.getAxisLeft().setAxisMaxValue(200f);
					mChart.getAxisLeft().setAxisMinValue(-200f);
					mChart.invalidate();
					mChart.setData(dataFlow);
				}
				time = measurement1.getLastValue().getTime();
				_timeTextView.setText("Time: " + String.format("%1$,.2f", time) + " s");
			}
		});

		_CO2Btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					_flowBtn.setChecked(false);
					_volumeBtn.setChecked(false);
					chartFlag = false;
					chartFlag2=true;
					mChart.getAxisLeft().setAxisMaxValue(150f);
					mChart.getAxisLeft().setAxisMinValue(-150f);
					mChart.invalidate();
					mChart.setData(dataCO2);
				}
				time = measurement1.getLastValue().getTime();
				_timeTextView.setText("Time: " + String.format("%1$,.2f", time) + " s");
			}
		});

		// Spinner click listener
		_commandSpinner.setOnItemSelectedListener(this);

		// Spinner Drop down elements
		List<String> categories = new ArrayList<String>();
		categories.add("Select command");
		categories.add("Command 1");
		categories.add("Command 2");
		categories.add("Command 3");
		categories.add("Command 4");

		// Creating adapter for spinner
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

		// Drop down layout style - list view with radio button
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// attaching data adapter to spinner
		_commandSpinner.setAdapter(dataAdapter);
	}

	private void connectPeripheral() {
		STTrace.method("connectPeripheral");

		// extract peripheral id (address) from intent
		Intent intent = this.getIntent();
		String address = intent.getStringExtra(TIOV2Sample.PERIPHERAL_ID_NAME);

		// retrieve peripheral instance from TIOManager
		this._peripheral = TIOManager.sharedInstance().findPeripheralByAddress(address);

		// register callback
		this._peripheral.setListener(this);

	}

	private void startRSSITimer() {
		STTrace.method("startRSSITimer");

		if (this._rssiRunnable == null) {
			this._rssiRunnable = new Runnable() {
				@Override
				public void run() {
					PeripheralActivity.this._peripheral.readRSSI();
					PeripheralActivity.this._rssiHandler.postDelayed(PeripheralActivity.this._rssiRunnable, PeripheralActivity.RSSI_INTERVAL);
				}
			};
		}
		this._peripheral.readRSSI();
		this._rssiHandler.postDelayed(this._rssiRunnable, PeripheralActivity.RSSI_INTERVAL);
	}

	private void stopRSSITimer() {
		STTrace.method("stopRSSITimer");

		this._rssiHandler.removeCallbacks(this._rssiRunnable);
	}

	private void updateUIState() {
		STTrace.method("updateUIState");

	}

	private void displayVersionNumber() {
		PackageInfo packageInfo;
		String version = "";
		try {
			packageInfo = this.getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (Exception ex) {
			STTrace.exception(ex);
		}
		this.setTitle(this.getTitle() + " " + version);
	}

	//---------------CHART METHODS--------------------------------------------------------------
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}


	private void setData(int i) {

		DecimalFormat df = new DecimalFormat("##0.0");

		time = measurement1.getLastValue().getTime();
		//xVals.add(String.format("%1$,.2f",time));
		xVals.add(String.valueOf(df.format(time)));
		//dataFlow.addXValue(Integer.toString((int) (time * 1000)));
		//dataVolume.addXValue(Integer.toString((int) (time * 1000)));
		//mChart.notifyDataSetChanged();

		//Log.i("Timek: ", xVals.get(xVals.size() - 1));
		//float val1 =  (float)newData.Channel1[i]/10f;
		float val1 = (float)measurement1.getLastValue().getVolume();

		yVals.add(new Entry(val1, contY1));

		//float val2 = (float)newData.Channel1[i]/10f;
		float val2 = (float)measurement1.getLastValue().getFlow();

		yVals2.add(new Entry(val2, contY2));

		//float val3 = (float)newData.Channel2[i]/10f;
		float val3 = (float)measurement2.getLastValue().getFlow();

		yVals3.add(new Entry(val3, contY3));

		doWork(i);

		contY1++;
		contY2++;
		contY3++;

		if(firstTime){
			initializeChart();
			firstTime=false;
		}

		// set data
		if (chartFlag){
			mChart.getAxisLeft().setAxisMaxValue(2f);
			mChart.getAxisLeft().setAxisMinValue(0f);
			mChart.setData(dataVolume);
		}else if(!chartFlag2){
			mChart.getAxisLeft().setAxisMaxValue(200f);
			mChart.getAxisLeft().setAxisMinValue(-200f);
			mChart.setData(dataFlow);
		} else{
			mChart.getAxisLeft().setAxisMaxValue(150f);
			mChart.getAxisLeft().setAxisMinValue(-150f);
			mChart.setData(dataCO2);
		}

		/*Let the view just show the first 60 values, then you have to scroll if you want to watch more
		We cannot do it at the beginning because every time the chart receive new values will move the view
		to show them, so we have to correct it here*/
		mChart.setVisibleXRangeMaximum(60);
		mChart.moveViewToX(xVals.size() -61);

		_timeTextView.setText("Time: " + String.format("%1$,.2f", time) + " s");
	}


	private void initializeChart(){
		// create a dataset and give it a type
		LineDataSet set1 = new LineDataSet(yVals, "Volume");
		LineDataSet set2 = new LineDataSet(yVals2, "Flow");
		LineDataSet set3 = new LineDataSet(yVals3, "CO2");

		set1.setDrawCircles(false);
		set1.disableDashedLine();
		set1.setLineWidth(2f);
		set1.setColor(Color.BLACK);
		set1.setValueTextSize(0f);

		set2.disableDashedLine();
		set2.setDrawCircles(false);
		set2.setLineWidth(2f);
		set2.setColor(Color.RED);
		set2.setValueTextSize(0f);

		set3.disableDashedLine();
		set3.setDrawCircles(false);
		set3.setLineWidth(2f);
		set3.setColor(Color.BLUE);
		set3.setValueTextSize(0f);

		ArrayList<LineDataSet> dataSets =  new ArrayList<LineDataSet>();
		dataSets.add(set1);
		dataSets.add(set2);
		dataSets.add(set3);

		dataVolume = new LineData(xVals, dataSets.set(0,set1));
		dataFlow = new LineData(xVals, dataSets.set(1,set2));
		dataCO2 = new LineData(xVals, dataSets.set(2,set3));
	}

	@Override
	public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
		Log.i("Gesture", "START");
	}

	@Override
	public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
		Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

		// un-highlight values after the gesture is finished and no single-tap
		if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
			mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
	}

	@Override
	public void onChartLongPressed(MotionEvent me) {
		Log.i("LongPress", "Chart longpressed.");
	}

	@Override
	public void onChartDoubleTapped(MotionEvent me) {
		Log.i("DoubleTap", "Chart double-tapped.");
	}

	@Override
	public void onChartSingleTapped(MotionEvent me) {
		Log.i("SingleTap", "Chart single-tapped.");
	}

	@Override
	public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
		Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
	}

	@Override
	public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
		Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
	}

	@Override
	public void onChartTranslate(MotionEvent me, float dX, float dY) {
		Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		Log.i("Entry selected", e.toString());
		Log.i("", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
	}

	@Override
	public void onNothingSelected() {
		Log.i("Nothing selected", "Nothing selected.");
	}


	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		switch (position){
			case 1:
				textToSend="cmd1"; break;
			case 2:
				textToSend="cmd2"; break;
			case 3:
				textToSend="cmd3"; break;
			case 4:
				textToSend="cmd4"; break;
			default:
				textToSend="Nothing Selected"; break;
		}

		STTrace.method("onSendButtonPressed");

		//Set the spinner to the "Select Command" position so that we can press again a command and receive the data
		parent.setSelection(0);

		try {
			clearChart();
			byte[] data = textToSend.getBytes("CP-1252");
			this._peripheral.writeUARTData(data);
		} catch (Exception ex) {
			STTrace.exception(ex);
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public void setFrequency(){

		if(cont==0) {
			startTime = SystemClock.uptimeMillis();
		}

		cont++;

		if (cont == NUM_PACKAGES){
			endTime = SystemClock.uptimeMillis();
			timeInMilliseconds = new BigDecimal(endTime-startTime);
			timeInSeconds = timeInMilliseconds.divide(thousand, 10, RoundingMode.CEILING);
			freq = new BigDecimal(String.valueOf(numP.multiply(one.divide(timeInSeconds, 2, RoundingMode.CEILING))));
			_frequencyTextView.setText("Frequency: " + String.valueOf(freq) + " Hz");
			cont=0;
		}
	}

	public void clearChart() {
		STTrace.method("onClearButtonPressed");

		if(!xVals.isEmpty()){ //If the chart has still no data, we don't want to do anything
			//set values of Volume to 0
			contY1=0;
			yVals.clear();
			xVals.clear();
			dataVolume.clearValues();
			mChart.setData(dataVolume);
			//set values of Flow to 0
			contY2=0;
			yVals2.clear();
			dataFlow.clearValues();
			mChart.setData(dataFlow);
			//set values of CO2 to 0
			contY3=0;
			yVals3.clear();
			dataCO2.clearValues();
			mChart.setData(dataCO2);

			//Clear the data and initialize the first values to 0 (with new SpiroMeasurement();)
			measurement1.dataPoints.clear();
			measurement2.dataPoints.clear();
			measurement1 = new SpiroMeasurement();
			measurement2 = new SpiroMeasurement();

			//Refresh chart, set time to 0 and initialize chart settings again
			mChart.invalidate();
			_timeTextView.setText("Time: " + String.format("%1$,.2f", time) + " s");
			initializeChart();
			firstTime=true;
		}

		mProgressStatus=0;
		_pbCO2.setProgress(mProgressStatus);

	}

	public void doWork(int i){
		DataCommand newData = new DataCommand(data2);
		mProgressStatus = (int)newData.Channel2[i];
		_pbCO2.setProgress(mProgressStatus);
		//Log.i("CO2", String.valueOf(mProgressStatus));
	}

	public void onFFTChartBtnPressed(View v){
		Intent i = new Intent(this, FFTChart.class);
		startActivity(i);
	}
}

