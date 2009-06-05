
package com.openexchange.crypto.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.crypto.CryptoErrorMessage;
import com.openexchange.crypto.CryptoService;
import com.openexchange.crypto.internal.CryptoServiceImpl;
import com.openexchange.exceptions.osgi.ComponentRegistration;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {
    
    private ServiceRegistration cryptoRegistration;
    private ComponentRegistration componentRegistration;

    public void start(BundleContext context) throws Exception {
        cryptoRegistration = context.registerService(CryptoService.class.getName(), new CryptoServiceImpl(), null);
        componentRegistration = new ComponentRegistration(context, "CRP", "com.openexchange.crypto", CryptoErrorMessage.EXCEPTIONS);
    }

    public void stop(BundleContext context) throws Exception {
        cryptoRegistration.unregister();
        componentRegistration.unregister();
    }

}
