package com.openexchange.preview.json.osgi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.ajax.requesthandler.DispatcherServlet;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.json.HTMLResponseRenderer;
import com.openexchange.preview.json.PreviewActionFactory;

public class PreviewJSONActivator extends AJAXModuleActivator {
    
    private static final Class<?>[] NEEDED = new Class[] { 
                                    IDBasedFileAccessFactory.class,
                                    PreviewService.class,
                                    ManagedFileManagement.class };
    
    final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(PreviewJSONActivator.class));

    
    @Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.preview.json.");    
        registerModule(new PreviewActionFactory(this), "preview");
        DispatcherServlet.registerRenderer(new HTMLResponseRenderer());
    }

}
