package com.openexchange.realtime.events.mail.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.push.PushEventConstants;
import com.openexchange.push.PushManagerService;
import com.openexchange.realtime.events.RTEventEmitterService;
import com.openexchange.realtime.events.mail.MailPushEventEmitter;
/**
 * 
 * The {@link RTMailEventsActivator} registers the glue class between OSGi mail push events and RT mail push events
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTMailEventsActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[]{EventAdmin.class, PushManagerService.class};
    }

    @Override
    protected void startBundle() throws Exception {
        MailPushEventEmitter emitter = new MailPushEventEmitter(this);

        // Register for Events
        final Dictionary<String, Object> d = new Hashtable<String, Object>(1);
        d.put(EventConstants.EVENT_TOPIC, new String[]{PushEventConstants.TOPIC});

        registerService(EventHandler.class, emitter, d);

        registerService(RTEventEmitterService.class, emitter);
        
    }


}
