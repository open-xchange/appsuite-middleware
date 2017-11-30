package com.openexchange.halo.pictures.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.TrustedDomainHalo;
import com.openexchange.halo.pictures.PictureHaloActionFactory;
import com.openexchange.halo.pictures.TrustedDomainPictureHaloActionFactory;

public class PictureHaloActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ContactHalo.class, TrustedDomainHalo.class};
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new PictureHaloActionFactory(this), "halo/contact/picture");
        registerModule(new TrustedDomainPictureHaloActionFactory(this), "halo/trustedDomain/picture");
    }

}
