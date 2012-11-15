package com.openexchange.realtime.presence.hazelcast.osgi;

import com.hazelcast.core.HazelcastInstance;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.MessageDispatcher;
import com.openexchange.realtime.presence.PresenceStatusService;
import com.openexchange.realtime.presence.hazelcast.impl.HazelcastPresenceStatusServiceImpl;

public class RealtimeHazelcastPresenceActivator extends HousekeepingActivator {


    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { HazelcastInstance.class, MessageDispatcher.class };
    }

    @Override
    protected void startBundle() throws Exception {
        HazelcastInstance hazelcastInstance = getService(HazelcastInstance.class);
        HazelcastPresenceStatusServiceImpl statusService = new HazelcastPresenceStatusServiceImpl(hazelcastInstance);
        registerService(PresenceStatusService.class, statusService);
    }

}
