package com.openexchange.document.converter.json.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.document.converter.json.DocumentConverterActionFactory;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;

public class DocumentConverterJSONActivator extends AJAXModuleActivator {
    
    private static final Class<?>[] NEEDED = new Class[] { 
                                    DocumentConverterService.class,
                                    IDBasedFileAccessFactory.class };
    
    final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DocumentConverterJSONActivator.class));

    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.document.converter.json.");    
        registerModule(new DocumentConverterActionFactory(this), "documentconverter");
    }

}
