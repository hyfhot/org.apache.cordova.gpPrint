package org.apache.cordova.gpPrint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.AssetManager;
import android.util.Log;
import com.gprinterio.GpCom.ERROR_CODE;
import com.gprinterio.GpDevice;
import com.gprinterio.PrinterRecieveListener;
import com.printer.EscCommand;

/**
 * This class echoes a string called from JavaScript.
 */
public class GpPrint extends CordovaPlugin implements PrinterRecieveListener {
	private GpDevice mDevice;
	private final String TAG = "gpprint";
	// private Paper paper = Paper.getInstance();
	static private JSONObject jsonObject = new JSONObject();
	RentInfo printInfo;
	EscCommand esc;
	Vector<Byte> Command = new Vector<Byte>(4096, 1024);

	/*public GpPrint()
	{	
		if (mDevice == null) {
			mDevice = new GpDevice();
			mDevice.registerCallback(this);
		}
		if (!mDevice.isDeviceOpen())
			mDevice.openBluetoothPort(cordova.getActivity(),
					"00:15:FF:F2:65:0B");

		if (esc == null)
			esc = new EscCommand();
	}*/
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		// TODO Auto-generated method stub
		super.initialize(cordova, webView);
	}
	
	public void initCom()
	{
		if (mDevice == null) {
			mDevice = new GpDevice();
			mDevice.registerCallback(this);
		}
		if (!mDevice.isDeviceOpen())
			mDevice.openBluetoothPort(cordova.getActivity(),
					"00:15:FF:F2:65:0B");

		if (esc == null)
			esc = new EscCommand();
	}
	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {

		if (action.equals("print")) {
			Paper.isCanceled = false;
			Paper.isFinished = false;
			final String message = args.getString(0);
		/*	if (mDevice == null) {
				mDevice = new GpDevice();
				mDevice.registerCallback(this);
			}
			if (!mDevice.isDeviceOpen())
				mDevice.openBluetoothPort(cordova.getActivity(),
						"00:15:FF:F2:65:0B");*/
			initCom();
			if (printInfo == null)
				printInfo = new RentInfo();

			printInfo.setOnListenGpStatus(new GpStatus() {
				@SuppressWarnings("rawtypes")
				@Override
				public void Message(Enum statu, String roomCode, int index,
						int total) {
					// TODO Auto-generated method stub
					Paper.errorState = statu;
					Paper.paperIndex = index;
					Paper.paperTotal = total;
					Paper.roomCode = roomCode;

					if (!Paper.errorState.toString().equals("SUCCESS")) {
						Paper.isError = true; // 有错误发生
						if (esc == null)
							esc = new EscCommand();
						Paper.flag = 4;
						esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_PAPER);
						Command = esc.getCommand();// 获得编辑的命令数据
						mDevice.sendDataImmediately(Command);// 发送命令
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Paper.flag = 1;
						esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_STATUS);
						Command = esc.getCommand();// 获得编辑的命令数据
						mDevice.sendDataImmediately(Command);// 发送命令
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Paper.flag = 2;
						esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_OFFLINE);
						Command = esc.getCommand();// 获得编辑的命令数据
						mDevice.sendDataImmediately(Command);// 发送命令
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Paper.flag = 3;
						esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_ERROR);
						Command = esc.getCommand();// 获得编辑的命令数据
						mDevice.sendDataImmediately(Command);// 发送命令

					} else//*/
						backMsgToJs();
					Log.i(TAG, "send state: " + Paper.errorState);
				}
			});
			if(esc ==null)
			esc = new EscCommand();
			cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub

					printInfo.parseJson(message);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					printInfo.toprint(mDevice);// 获得编辑的命令数据
				}
			});
			callbackContext.success();
			return true;

		}
		else if(action.equals("pauseprint"))
		{
			Paper.isPaused =!Paper.isPaused;
			callbackContext.success();
				return true;
		}
		else if (action.equals("getstatus")) {
			
		//	 webView.postMessage("paperInfo", paper);
			callbackContext.success(backMsgToJs());
		//	jsonObject =new JSONObject();
		//	jsonObject.put("hello", "hello");
			callbackContext.success(jsonObject);
			return true;
		} else if (action.equals("cancelprint")) {
			Paper.isCanceled = true;
			// mDevice.
			if(mDevice.isDeviceOpen())
			mDevice.closePort();
			callbackContext.success(backMsgToJs());
             return true;
		} 
		else
			return false;
	}

	void parseJson() {
		AssetManager am = cordova.getActivity().getAssets();
		try {
			InputStream instream = am.open("baozupo.json");
			readTextFile(instream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String readTextFile(InputStream inputStream) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte buf[] = new byte[2048];
		int len;
		try {

			while ((len = inputStream.read(buf)) != -1) {
				outputStream.write(buf, 0, len);
			}
			outputStream.close();
			inputStream.close();
		} catch (IOException e) {
		}
		return outputStream.toString();
	}

	@Override
	public ERROR_CODE ReceiveData(Vector<Byte> vec) {
		// TODO Auto-generated method stub
		showReceive(vec);
		return null; // GpCom.ERROR_CODE.SUCCESS
		// null;
	}

	
	private void showReceive(Vector<Byte> vector) {
		// TODO Auto-generated method stub
		Log.i(TAG, "rec: " + vector.toString());
		long tmp = Long.valueOf(vector.get(0).toString());
		Log.i(TAG, "operState: " + Paper.operState);
		Log.i(TAG, " 发送的标志 ： " + Paper.flag + "  --- " + Paper.isCanceled);
		Log.i(TAG, "格式化 的 " + tmp + "  状态比较 ： " + (tmp & 108));
		if (!Paper.isError) // 出错前 诊断
		{
			switch (Paper.flag) {
			case 2:
				offlineCheck(tmp);
				break;
			case 4:
				printerPaerCheck(tmp);
				break;
			}
		}

		else { // 出错了 诊断 错误原因
			switch (Paper.flag) {
			case 2:
				offlineCheck(tmp);
				break;
			case 4:
				printerPaerCheck(tmp);
				break;
			case 1:
				printerStatusCheck(tmp);
				break;
			case 3:
				printerErrorCheck(tmp);
				break;
			}
		}
		Log.i(TAG, "runningState: " + Paper.runingState);

		// Toast.makeText(cordova.getActivity(), Paper.runingState,
		// Toast.LENGTH_SHORT).show();

		// String tmp =new String(b);
	}
	private JSONObject backMsgToJs() {
		try {
			jsonObject.put("paperIndex", Paper.paperIndex);
			jsonObject.put("roomCode", Paper.roomCode);
			jsonObject.put("paperTotal", Paper.paperTotal);
			jsonObject.put("runingState", Paper.runingState);
		/*	Log.i(TAG, " index : " + Paper.paperIndex + " code: "
					+ Paper.roomCode + " total: " + Paper.paperTotal
					+ " state: " + Paper.errorState.toString());*/
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(TAG, "operState: " + Paper.operState);

		return jsonObject;
	}

	private void printerPaerCheck(long tmp) { // 传送纸状态检测 4
		// TODO Auto-generated method stub
		long t = tmp & 108;
		if (t != 0) {
			Paper.runingState = "缺纸状态";
		}
	}

	private void offlineCheck(long tmp) { // 脱机 2
		// TODO Auto-generated method stub
		long t = tmp & 4;
		if (t != 0) {
			Paper.runingState = "上盖开";
		} else {
			t = tmp & 32;
			if (t != 0)
				Paper.runingState = "打印机缺纸";
		}
	}

	private void printerStatusCheck(long tmp) // 打印进状态检测 1
	{
		long t = tmp & 8;
		if (t != 0) {
			Paper.runingState = "打印机已脱机";
		}
	}

	private void printerErrorCheck(long tmp) // 错误状态检测 3
	{
		long t = tmp & 8;
		if (t != 0) {
			Paper.runingState = "切刀有错误";
		} else {
			t = tmp & 32;
			if (t != 0) {
				Paper.runingState = "有不可恢复错误";
			} else {
				t = tmp & 64;
				if (t != 0) {
					Paper.runingState = "打印头温度或电压超出范围";

				}
			}
		}
	}
	
}