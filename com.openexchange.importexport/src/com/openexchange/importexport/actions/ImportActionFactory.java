package com.openexchange.importexport.actions;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.importexport.formats.Format;

public class ImportActionFactory extends AbstractIEActionFactory{

    private final Map<Format, AJAXActionService> map;
    
    /**
     * Initializes a new {@link ImportActionFactory}.
     */
    public ImportActionFactory() {
        super();
        final EnumMap<Format, AJAXActionService> map = new EnumMap<Format, AJAXActionService>(Format.class);
        map.put(Format.CSV, new CsvImportAction());
        this.map = Collections.unmodifiableMap(map);
    }

    @Override
    protected Map<Format, AJAXActionService> getActions(){
    	return map;
    }

}
