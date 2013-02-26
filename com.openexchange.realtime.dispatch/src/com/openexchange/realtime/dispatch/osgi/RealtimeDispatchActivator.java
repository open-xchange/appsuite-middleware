
package com.openexchange.realtime.dispatch.osgi;

import org.apache.commons.logging.Log;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.dispatch.ResourceRegistry;
import com.openexchange.realtime.dispatch.impl.MessageDispatcherImpl;
import com.openexchange.realtime.dispatch.impl.ResourceRegistryImpl;

public class RealtimeDispatchActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RealtimeDispatchActivator.class);

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { EventAdmin.class };
    }

    /*
     * Register the MessageDispatcher as Service and listen for new Channels being added to the OSGi service registry. When new Channels are
     * added/removed to/from the service registry inform the MessageDispatcher about it.
     */
    @Override
    protected void startBundle() throws Exception {
        context.addFrameworkListener(new FrameworkListener() {

            @Override
            public void frameworkEvent(FrameworkEvent event) {
                if (event.getBundle().getSymbolicName().toLowerCase().startsWith("com.openexchange.realtime.dispatch")) {
                    int eventType = event.getType();
                    if (eventType == FrameworkEvent.ERROR) {
                        LOG.error(event.toString(), event.getThrowable());
                    } else {
                        LOG.info(event.toString(), event.getThrowable());
                    }
                }
            }
        });

        final MessageDispatcher dispatcher = new MessageDispatcherImpl();

        registerService(MessageDispatcher.class, dispatcher);
        registerService(ResourceRegistry.class, new ResourceRegistryImpl(getService(EventAdmin.class)));

        track(Channel.class, new SimpleRegistryListener<Channel>() {

            @Override
            public void added(final ServiceReference<Channel> ref, final Channel service) {
                dispatcher.addChannel(service);
            }

            @Override
            public void removed(final ServiceReference<Channel> ref, final Channel service) {
                dispatcher.removeChannel(service);
            }
        });

        openTrackers();
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
    }

}
