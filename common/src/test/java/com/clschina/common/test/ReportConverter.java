package com.clschina.common.test;

import java.util.HashMap;

import com.clschina.common.report.CellDataConverterInterface;

public class ReportConverter implements CellDataConverterInterface {
	private HashMap<String, String> idNamePaire;
		
	public ReportConverter(){
		idNamePaire = new HashMap<String, String>();
		String tiangan = "甲乙丙丁戊己庚辛壬癸";
		String dizhi = "子丑寅卯辰巳午未申酉戌亥";
		for(int i=0; i<tiangan.length(); i++){
			for(int j=0; j<dizhi.length(); j++){
				int l = i * tiangan.length() + j;
				String k = String.valueOf(l);
				String v = String.valueOf(tiangan.charAt(l % tiangan.length())) + String.valueOf(dizhi.charAt(l % dizhi.length()));
				idNamePaire.put(k, v);
			}
		}
	}
	
	public String excelCellConverter(Object data, int column, int row) {
		if(column == 4){
			if(idNamePaire.containsKey(data)){
				return idNamePaire.get(data);
			}
		}
		return data.toString();
	}

	public String htmlCellConverter(Object data, int column, int row) {
		if(column == 4){
			if(idNamePaire.containsKey(data)){
				return idNamePaire.get(data);
			}
		}
		return data.toString();
	}

	@Override
	public Object excelCellConverter(Object data, int column, int row,
			Object[] rowData) {
		return excelCellConverter(data, column, row);
	}

}
