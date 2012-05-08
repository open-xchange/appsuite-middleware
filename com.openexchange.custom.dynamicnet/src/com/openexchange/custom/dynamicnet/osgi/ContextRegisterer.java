
package com.openexchange.custom.dynamicnet.osgi;

import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.context.ContextService;

/**
 * @author community
 *
 */
public class ContextRegisterer implements ServiceTrackerCustomizer<ContextService,ContextService> {

    private static final AtomicReference<ContextService> cs = new AtomicReference<ContextService>();

    private final BundleContext context ;

    public static ContextService getContextService(){
        return cs.get();
    }

    public ContextRegisterer (final BundleContext context){
        super();
        this.context = context;
    }


    @Override
    public ContextService addingService(final ServiceReference<ContextService> arg0) {
        final ContextService service = context.getService(arg0);
        cs.set(service);
        return service;
    }


    @Override
    public void modifiedService(final ServiceReference<ContextService> arg0, final ContextService arg1) {
        // not needed here
    }


    @Override
    public void removedService(final ServiceReference<ContextService> arg0, final ContextService arg1) {
        // needed for counting service usage correctly
        this.context.ungetService(arg0);
    }

}
