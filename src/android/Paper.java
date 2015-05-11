package org.apache.cordova.gpPrint;

public class Paper {
	static public String roomCode="";
	static public int paperIndex=0;
	static public int paperTotal=0;
    static public Enum<?> errorState;   // 错误状态
	static private Paper p;
	static public boolean isFinished = false;
	static public boolean isCanceled = false;
	static public boolean isPaused = false;
	static public int flag =0;  //路由错误信息标志
	static public String runingState="正常";
	static public String operState="";
	static public boolean isError =false;

	static public String[] PAPER_STATE= new String[]{"NORMAL","READING","RUNNING","ERROR"};
	private Paper()
	{
		
	}
	static Paper getInstance()
	{
		if(p ==null)
		{
			p =new Paper();
		}
		return p;
	}
	

	public static Enum<?> getErrorState() {
		return errorState;
	}
	public  void setErrorState(Enum<?> errorState) {
		Paper.errorState = errorState;
	}
	public String getRoomCode() {
		return roomCode;
	}

	public void setRoomCode(String roomCode) {
		Paper.roomCode = roomCode;
	}

	public int getPaperIndex() {
		return paperIndex;
	}

	public void setPaperIndex(int paperIndex) {
		Paper.paperIndex = paperIndex;
	}

	public int getPaperTotal() {
		return paperTotal;
	}

	public void setPaperTotal(int paperTotal) {
		Paper.paperTotal = paperTotal;
	}
	
}
