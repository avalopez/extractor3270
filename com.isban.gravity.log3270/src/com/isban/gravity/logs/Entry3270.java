package com.isban.gravity.logs;

public class Entry3270 {
	private String trxName;
	private String result;
	private String timestampMasrer;
	private String timestampSecondary;
	private String masterName;
	private String differences;
	private String inDataMaster;
	private String inDataSecondary;
	
	public Entry3270(String trx, String result, String tsMaster, String tsSecondary, 
			String master, String differences, String inMaster, String inSecondary) {
		this.differences = (differences.length() < 32760? differences:differences.substring(0, 32760));;
		this.trxName = trx;
		this.inDataMaster = (inMaster.length() < 32760? inMaster:inMaster.substring(0, 32760));
		this.inDataSecondary = (inSecondary.length() < 32760? inMaster:inSecondary.substring(0, 32760));
		this.result = result;
		this.timestampMasrer = tsMaster;
		this.timestampSecondary = tsSecondary;
		this.masterName = master;
	}
	public String getInDataMaster() {
		return inDataMaster;
	}
	public String getInDataSecondary() {
		return inDataSecondary;
	}
	public String getTrxName() {
		return trxName;
	}
	public String getResult() {
		return result;
	}
	public String getTimestampMasrer() {
		return timestampMasrer;
	}
	public String getTimestampSecondary() {
		return timestampSecondary;
	}
	public String getMasterName() {
		return masterName;
	}
	public String getDifferences() {
		return differences;
	}
	
}
