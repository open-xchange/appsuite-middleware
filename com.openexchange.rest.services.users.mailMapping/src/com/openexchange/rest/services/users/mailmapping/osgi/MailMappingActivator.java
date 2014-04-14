package com.openexchange.rest.services.users.mailmapping.osgi;

import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.osgiservice.OSGIMailMappingService;
import com.openexchange.rest.services.osgiservice.OXRESTActivator;
import com.openexchange.rest.services.users.mailmapping.MailMappingService;

public class MailMappingActivator extends OXRESTActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        OSGIMailMappingService mapper = new OSGIMailMappingService();
        track(MailResolver.class, mapper);
        
        registerWebService(MailMappingService.class, mapper);
    }


}
