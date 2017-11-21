package com.openexchange.mail.authentication.osgi;

import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.mail.authentication.TrustedMailDomainService;
import com.openexchange.mail.authentication.impl.TrustedMailDomainServiceImpl;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceExceptionCode;

public class Activator extends HousekeepingActivator{

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ConfigurationService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        ConfigurationService configurationService = getService(ConfigurationService.class);
        if(configurationService == null){
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }
        TrustedMailDomainServiceImpl trustedMailDomainService = new TrustedMailDomainServiceImpl(configurationService);
        registerService(TrustedMailDomainService.class, trustedMailDomainService);
        registerService(Reloadable.class, trustedMailDomainService);
    }



}
