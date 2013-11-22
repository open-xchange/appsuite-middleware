package com.openexchange.halo.pictures.osgi;

import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.pictures.PictureHaloActionFactory;

public class PictureHaloActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{ContactHalo.class};
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new PictureHaloActionFactory(this), "halo/contact/picture");
    }

}
