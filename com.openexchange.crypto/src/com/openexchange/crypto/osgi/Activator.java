
package com.openexchange.crypto.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.crypto.CryptoService;
import com.openexchange.crypto.CryptoServiceImpl;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Activator implements BundleActivator {
    
    private ServiceRegistration cryptoRegistration;

    public void start(BundleContext context) throws Exception {
        cryptoRegistration = context.registerService(CryptoService.class.getName(), new CryptoServiceImpl(), null);
    }

    public void stop(BundleContext context) throws Exception {
        cryptoRegistration.unregister();
    }

}
