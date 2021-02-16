package com.isban.gravity.logs;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;

public class LogJsonReader {
	
	static String fileIn = "/home/angel/tmp/20210204-3270.logs";
	static String fileOut = "/home/angel/tmp/";
	static String sheetName = "3270 Activity";

	public static void main(String args[]) {
		
		FileReader fr = null;
		LogJsonReader jsonReader = new LogJsonReader();

		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet sheet = (Sheet) workbook.createSheet("3270 Activity"); 
		jsonReader.initSheet(workbook, sheet);
		List<Entry3270> lstEntry = new ArrayList<Entry3270>();

		try {
			fr = new FileReader(fileIn);
			BufferedReader br = new BufferedReader(fr);
			String masterName = "Partenon";
			String secondaryName = "Secondary";

			String json;
			String cabeceraExcel = "nombre Trx\tIguales\t\'timestamp Partenon\'\ttimestamp OHE\tMaster";
			int linC = 0;
			// System.out.println(cabeceraExcel);
			while ((json = br.readLine()) != null) {
				JSONObject jsonObject = new JSONObject(json);
				JSONObject jsonObjectCommon = (JSONObject) jsonObject.get("hdrs");
				JSONObject jsonObjectDiff = (JSONObject) jsonObject.get("diffs");
				JSONObject masterMsg = (JSONObject) jsonObjectCommon.get("masterMsg");
				JSONObject secondaryMsg = (JSONObject) jsonObjectCommon.get("secondaryMsg");

				String resultadoComparacion = jsonObjectCommon.getString("resultadoComparacion");
				
				String trx = jsonObjectCommon.getString("trx");
				JSONObject hdrsMaster = (JSONObject) masterMsg.get("hdrs");
				JSONObject hdrsSecondary = (JSONObject) secondaryMsg.get("hdrs");

				String tsIniPartenon = ((JSONObject) hdrsMaster.get("tsIni")).getString("value");
				String tsIniOhe = ((JSONObject) hdrsSecondary.get("tsIni")).getString("value");

				String hxInSecond = (String) secondaryMsg.get("dumpAreas");
				String hxInMaster = (String) masterMsg.get("dumpAreas");
//				
				List<String> lstSecond = getInDataArea(hxInSecond);
				List<String> lstMaster = getInDataArea(hxInMaster);

				if (resultadoComparacion.equals("IGUALES")) {
					resultadoComparacion = "OK";
					lstMaster.removeAll(lstMaster);
					lstSecond.removeAll(lstSecond);
				} else {
					resultadoComparacion = "KO";
				}

				Entry3270 entry = new Entry3270(trx, resultadoComparacion, tsIniPartenon, tsIniOhe, masterName,
						jsonObjectDiff.toString(), lstMaster.toString(), lstSecond.toString());
				lstEntry.add(entry);
			}
			jsonReader.create3270EntriesRow(lstEntry, sheet);

			jsonReader.generateSheet(workbook);
		} catch (Exception e) {
			System.out.println("Excepcion leyendo fichero " + fileIn + ": " + e);
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void generateSheet(Workbook workbook) {
		try {
			Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
			int year = calendar.get(Calendar.YEAR);
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH) + 1;
			fileOut +=""+year+"0"+month+"0"+day+"-3270_Activity.xlsx";
			FileOutputStream fileOutstr = new FileOutputStream(fileOut);
			workbook.write(fileOutstr);
			fileOutstr.close();
		} catch (Exception e) {

		}

	}

	
	
	
	public void create3270EntriesRow(List<Entry3270> entries, Sheet sheet) {
	
		int rowNum = 1;
		for (Entry3270 entry : entries) {
			Row row = sheet.createRow(rowNum++);
			row.createCell(0).setCellValue(entry.getTrxName());
			row.createCell(1).setCellValue(entry.getResult());
			row.createCell(2).setCellValue(entry.getTimestampMasrer());
			row.createCell(3).setCellValue(entry.getTimestampSecondary());
			row.createCell(4).setCellValue(entry.getMasterName());
			row.createCell(5).setCellValue(entry.getDifferences());
			row.createCell(6).setCellValue(entry.getInDataMaster());
			row.createCell(7).setCellValue(entry.getInDataSecondary());
		}
		for (int i = 1; i < 8; i++)
			sheet.autoSizeColumn(i);

	}

	public static List<String> getInDataArea(String data) {
		int mapInIdx = data.indexOf("MAPA_IN");
		int mapOutIdx = data.indexOf("MAPA_OUT");

		int arqInIdx = data.indexOf("ARQ_IN");
		int arqOutIdx = data.indexOf("ARQ_OUT");

		String dataInHx = data.substring(mapInIdx, mapOutIdx - 2);
		String dataOutHx = data.substring(mapOutIdx, arqOutIdx);
		List<String> lines = new ArrayList<String>();

		String[] dataInParts = dataInHx.split("\n");

		for (int i = 1; i < dataInParts.length; i++) {
			try {
				if (dataInParts[i].length() <= 0)
					continue;
				dataInParts[i] = dataInParts[i].substring(2);

				lines.add(decodeHexx(dataInParts[i]).replaceAll("[^a-zA-Z0-9]", " "));
			} catch (Exception e) {

				e.printStackTrace();
			}

		}
		return lines;
	}

	public void initSheet(Workbook workbook, Sheet sheet) {
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 18);
		headerFont.setColor(IndexedColors.BLACK.getIndex());

		

		XSSFColor color = new XSSFColor(new Color(220,220,220));
		XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
		cellStyle.setFillForegroundColor(color);
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setFont(headerFont);
		Row headerRow = sheet.createRow(0);
		String[] columns = { "TRX Name", "Result", "Timestamp Master", "Timestamp Secondary", "Master", "Differences",
				"inData Master", "inData Secondary" };
		for (int i = 0; i < 8; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(cellStyle);
			
		}
	}

	public static String decodeHexx(String hex) {
		byte[] bytes;
		String decStr = null;
		try {
			bytes = Hex.decodeHex(hex.toCharArray());
			decStr = new String(bytes, "cp285");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decStr;
	}
}
