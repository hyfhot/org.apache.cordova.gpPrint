package org.apache.cordova.gpPrint;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.gprinterio.GpDevice;
import com.printer.EscCommand;
import com.printer.EscCommand.JUSTIFICATION;
import com.printer.EscCommand.UNDERLINE_MODE;

public class RentInfo {

	private GpStatus status;
	private GpDevice mDevice;
	UserInfo userinfo;
	EscCommand esc;

	public void setOnListenGpStatus(GpStatus state) {
		status = state;
	}

	public void queryGpState(int i) {
		if (esc == null)
			esc = new EscCommand();

		switch (i) {
		case 4:
			esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_PAPER);
			break;
		case 1:
			esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_STATUS);
			break;
		case 2:
			esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_OFFLINE);
			break;
		case 3:
			esc.queryRealtimeStatus(EscCommand.STATUS.PRINTER_ERROR);
			break;

		}
		Vector<Byte> Command = new Vector<Byte>(4096, 1024);
		Command = esc.getCommand();// 获得编辑的命令数据
		mDevice.sendDataImmediately(Command);// 发送命令
	}

	@SuppressWarnings("rawtypes")
	public void toprint(GpDevice mDevice) {
		this.mDevice = mDevice;
		int t = 0;
		Paper.isFinished = false;
		Paper.isPaused = false;

		Paper.flag = 2; // 脱机标志
		queryGpState(2); // 脱机检测

		for (int i = 0; i < userinfo.getBulids().size(); i++) {
			Build build = userinfo.getBulids().get(i);

			for (int r = 0; r < 3; r++) {//build.getRoom().size()
				Room room = build.getRoom().get(0); // r

				for (int j = 0; j < room.getRentItemList().size(); j++) {

					while (Paper.isPaused) {
						try {
							Paper.operState = "已暂停";
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					while (Paper.isCanceled) {
						Paper.operState = "已取消";
						return;
					}
					Paper.flag = 4;
					queryGpState(4); // 走纸状态查询
					Enum e = mDevice.sendDataImmediately(printRentInfo(build,
							room, room.getRentItemList().get(j), 1));
					status.Message(e, room.code, ++t, build.getRoom().size());
					try { 
						Thread.sleep(2000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// status.Message(mDevice.sendDataImmediately(printRentInfo(build,
					// room, room.getRentItemList().get(j), 2)));

				}
			}

		}

		Paper.isFinished = true;

		// return Command;
	}

	public UserInfo parseJson(String json) {

		JSONArray array;
		try {
			// Build build =new Build();
			JSONObject obj = new JSONObject(json);
			userinfo = new UserInfo();
			userinfo.setAppid(obj.getLong("appid"));
			userinfo.setAppname(obj.getString("appname"));

			array = obj.getJSONArray("_builds");
			List<Build> buildList = new ArrayList<Build>();

			for (int i = 0; i < array.length(); i++) {
				Build build = new Build();
				JSONObject jobj = array.getJSONObject(i);
				build.setBuildid(jobj.getLong("buildid"));
				build.setName(jobj.getString("name"));
				build.setAddress(jobj.getString("address"));
				build.setFloors(jobj.getLong("floors"));
				build.setRooms(jobj.getLong("rooms"));
				build.setImages(jobj.getString("images"));
				build.setDescription(jobj.getString("description"));

				JSONArray roomArray = jobj.getJSONArray("_rooms");

				buildList.add(build);

				List<Room> roomList = new ArrayList<Room>();
				for (int j = 0; j < roomArray.length(); j++) { // roomArray.length()
					JSONObject roomObj = roomArray.getJSONObject(j);
					Room room = new Room();
					room.setAppid(roomObj.getLong("appid"));
					room.setRoomid(roomObj.getLong("roomid"));
					room.setBuildid(roomObj.getLong("buildid"));
					room.setCode(roomObj.getString("code"));
					room.setRoomtype(roomObj.getString("roomtype"));
					room.setPacklevel(roomObj.getString("packlevel"));
					room.setDirection(roomObj.getString("direction"));
					room.setFloor(roomObj.getLong("floor"));
					room.setArea(roomObj.getString("area"));
					room.setPrice(roomObj.getLong("price"));
					room.setDeploy(roomObj.getString("deploy"));
					room.setImages(roomObj.getString("images"));
					room.setPledge(roomObj.getString("pledge"));
					room.setPayment(roomObj.getString("payment"));
					room.setDescription(roomObj.getString("description"));

					JSONArray arr = roomObj.getJSONArray("RentItemList");

					roomList.add(room);

					List<RentItemList> rentItemList = new ArrayList<RentItemList>();

					for (int r = 0; r < arr.length(); r++) {
						RentItemList rent = new RentItemList();
						JSONObject rentObj = arr.getJSONObject(r);
						rent.setYear(rentObj.getLong("year"));
						rent.setMonth(rentObj.getLong("month"));
						rent.setDemo(rentObj.getString("demo"));
						rent.setTotalmoney(rentObj.getLong("totalmoney"));

						JSONArray financeArray = rentObj
								.getJSONArray("financelist");

						rentItemList.add(rent);

						List<Financelist> finList = new ArrayList<Financelist>();

						for (int f = 0; f < financeArray.length(); f++) {
							Financelist finan = new Financelist();
							JSONObject finObj = financeArray.getJSONObject(f);
							finan.setCalclogid(finObj.getLong("calclogid"));
							finan.setClassid(finObj.getLong("classid"));
							finan.setClassname(finObj.getString("classname"));
							finan.setCalctype(finObj.getLong("calctype"));
							finan.setUnit(finObj.getString("unit"));
							finan.setPrice(finObj.getLong("price"));
							finan.setCustomprice(finObj
									.getString("customprice"));
							finan.setValue_init(finObj.getLong("value_init"));
							finan.setValue_end(finObj.getLong("value_end"));
							finan.setValue_real(finObj.getLong("value_real"));
							finan.setMoney(finObj.getLong("money"));
							finList.add(finan);
						}
						rent.setFinancelist(finList);
					}
					room.setRentItemList(rentItemList);
				}

				build.setRoom(roomList);

			}

			userinfo.setBulids(buildList);

			System.out.println("---------------------");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userinfo;
	}

	public Vector<Byte> printRentInfo(Build build, Room room,
			RentItemList rent, int type) {
		EscCommand esc = new EscCommand();
		esc.addLineSpacing((byte) 80);
		esc.addSetUnderLineMode(UNDERLINE_MODE.OFF);
		if (type == 1) {
			esc.addText("--------------------------房东联");
		} else {
			esc.addText("--------------------------客户联");

		}

		esc.addText("包租婆 - 您的星级物业管家 \n");// 打印文字

		esc.addTurnEmphasizedModeOnOrOff(EscCommand.ENABLE.ON);// 加粗模式有效
		esc.addText(rent.getYear() + "年" + rent.getMonth() + "月 "
				+ build.getName() + " " + room.getCode() + "\n");
		esc.addTurnEmphasizedModeOnOrOff(EscCommand.ENABLE.OFF);// 加粗模式有效
		esc.addText("--------------------------------");

		for (int f = 0; f < rent.getFinancelist().size(); f++) {
			StringBuilder sb = new StringBuilder();
			Financelist fian = rent.getFinancelist().get(f);
			sb.append(fian.getClassname() + ":" + fian.getMoney() + "\n");
			switch ((int) fian.getCalctype()) {
			case 2:
				sb.append("  月初:" + fian.value_init + " 月底:" + fian.value_end
						+ "\n");
				sb.append("  用量:" + fian.value_real + fian.unit + "  单价:"
						+ fian.price + "元/" + fian.unit + "\n");
				break;
			case 3:
				sb.append("  月初:" + fian.value_init + " 月底:" + fian.value_end
						+ "\n");
				sb.append("  用量:" + fian.value_real + fian.unit + "  单价:"
						+ fian.customprice + "\n");
				break;

			}
			esc.addText(sb.toString());
		}
		esc.addTurnEmphasizedModeOnOrOff(EscCommand.ENABLE.ON);
		esc.addText("合计：1302元    收款人：________\n");
		esc.addTurnEmphasizedModeOnOrOff(EscCommand.ENABLE.OFF);

		if (type == 2) {
			StringBuilder sb = new StringBuilder();
			sb.append("--------------------------------\n");
			sb.append("尊敬的租户,您可以通过以下方式交租:\n");
			sb.append("1，现金交租\n");
			sb.append("  请于1日-5日晚上7点-10点到保安室处交租\n");
			sb.append("2，银行转账\n");
			sb.append("  招商银行深圳分行车公庙支行\n");
			sb.append("  账号：4339 2882 9389 234\n");
			sb.append("  户名：张三\n");

			sb.append("3，在线支付\n");
			sb.append("  安装包租婆APP，使用交租功能在线支付\n");
			esc.addText(sb.toString());
			sb = sb.delete(0, sb.length());
			Bitmap b = QRcodeCreater.createQRCodeBitmap("您该交房租了 额。。。。");
			esc.addSetJustification(JUSTIFICATION.CENTER);
			esc.addRastBitImage(b);
			esc.addSetJustification(JUSTIFICATION.LEFT);

			sb.append("4，微信支付\n");
			sb.append("  关注微信公众号：包租婆\n");
			esc.addText(sb.toString());
			esc.addSetJustification(JUSTIFICATION.CENTER);

			b = QRcodeCreater.createQRCodeBitmap("关注微信号吧。。。。");
			esc.addRastBitImage(b);
			esc.addSetJustification(JUSTIFICATION.LEFT);
		}
	//	esc.addCutPaperAndFeed((byte) 40);
		Vector<Byte> Command = new Vector<Byte>(4096, 1024);
		Command = esc.getCommand();// 获得编辑的命令数据
		return Command;
	}

}
