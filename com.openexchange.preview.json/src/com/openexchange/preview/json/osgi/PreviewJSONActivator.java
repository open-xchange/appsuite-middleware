package com.openexchange.preview.json.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.json.PreviewActionFactory;

public class PreviewJSONActivator extends AJAXModuleActivator {
    
    private static final Class<?>[] NEEDED = new Class[] { 
                                    DocumentConverterService.class,
                                    IDBasedFileAccessFactory.class,
                                    PreviewService.class };
    
    final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PreviewJSONActivator.class));

    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.preview.json.");    
        registerModule(new PreviewActionFactory(this), "preview");
    }

}
