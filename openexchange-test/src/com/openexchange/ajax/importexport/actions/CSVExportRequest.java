package com.openexchange.ajax.importexport.actions;

import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.importexport.actions.AbstractImportRequest.Action;


public class CSVExportRequest extends AbstractExportRequest<CSVExportResponse> {
	boolean failOnError = true;
	
	public CSVExportRequest(int folderId) {
		super(Action.CSV, folderId);

	}

	@Override
	public AbstractAJAXParser<? extends CSVExportResponse> getParser() {
		return new CSVExportParser(failOnError);
	}

}
