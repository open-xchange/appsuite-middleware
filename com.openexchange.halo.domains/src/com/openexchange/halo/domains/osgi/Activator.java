package com.openexchange.halo.domains.osgi;

import com.openexchange.halo.TrustedDomainHalo;
import com.openexchange.halo.domains.impl.TrustedDomainHaloImpl;
import com.openexchange.mail.authenticity.impl.handler.domain.TrustedDomainService;
import com.openexchange.osgi.HousekeepingActivator;

public class Activator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] {TrustedDomainService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(TrustedDomainHalo.class, new TrustedDomainHaloImpl(getService(TrustedDomainService.class)));
    }

}
