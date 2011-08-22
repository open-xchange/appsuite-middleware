package com.openexchange.importexport.actions;

import java.util.HashMap;
import java.util.Map;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.importexport.formats.Format;

public class ImportActionFactory extends AbstractIEActionFactory{
	
    protected Map<Format, AJAXActionService> getActions(){
    	return new HashMap<Format, AJAXActionService>(){{
    		put(Format.CSV, new CsvImportAction());
    	}};
    }

}
