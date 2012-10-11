package com.openexchange.preview.jodconverter.osgi;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.document.converter.DocumentConverterService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.preview.InternalPreviewService;
import com.openexchange.preview.jodconverter.internal.JODCPreviewService;

public class JODCActivator extends HousekeepingActivator {

	private static final Class<?>[] NEEDED = new Class[] { DocumentConverterService.class };

	private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(JODCActivator.class));


	public JODCActivator() {
        super();
    }

	@Override
    protected Class<?>[] getNeededServices() {
        return NEEDED;
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("Starting bundle com.openexchange.preview.jodconverter.");
        registerService(InternalPreviewService.class, new JODCPreviewService(this));
    }



}
