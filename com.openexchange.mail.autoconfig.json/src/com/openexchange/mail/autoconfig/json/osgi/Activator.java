
package com.openexchange.mail.autoconfig.json.osgi;

import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.mail.autoconfig.AutoconfigService;
import com.openexchange.mail.autoconfig.json.actions.AutoconfigActionFactory;
import com.openexchange.mail.autoconfig.json.converter.AutoconfigResultConverter;

/**
 * {@link Activator}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class Activator extends AJAXModuleActivator {

    /*
     * (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#getNeededServices()
     */
    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { AutoconfigService.class };
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.server.osgiservice.DeferredActivator#startBundle()
     */
    @Override
    protected void startBundle() throws Exception {
        registerModule(new AutoconfigActionFactory(this), "autoconfig");
        registerService(ResultConverter.class, new AutoconfigResultConverter());
    }

}
