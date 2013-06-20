package com.openexchange.realtime.events.osgi;

import org.osgi.framework.ServiceReference;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.events.RTEventEmitterService;
import com.openexchange.realtime.events.impl.RTEventManager;
import com.openexchange.realtime.events.json.EventsActionFactory;
/**
 * 
 * The {@link EventsActivator} collects {@link RTEventEmitterService} instances from the OSGi system and exposes
 * the AJAX actions from the {@link EventsActionFactory} via the Dispatcher
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class EventsActivator extends AJAXModuleActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{MessageDispatcher.class};
    }

    @Override
    protected void startBundle() throws Exception {
        final RTEventManager manager = new RTEventManager(this);
        
        track(RTEventEmitterService.class, new SimpleRegistryListener<RTEventEmitterService>() {

            @Override
            public void added(ServiceReference<RTEventEmitterService> ref, RTEventEmitterService service) {
                manager.addEmitter(service);
            }

            @Override
            public void removed(ServiceReference<RTEventEmitterService> ref, RTEventEmitterService service) {
                manager.removeEmitter(service);
            }
        });
        openTrackers();
        
        registerModule(new EventsActionFactory(manager), "events");
    }


}
