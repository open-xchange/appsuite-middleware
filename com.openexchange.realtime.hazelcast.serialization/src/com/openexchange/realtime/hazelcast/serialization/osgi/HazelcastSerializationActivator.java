package com.openexchange.realtime.hazelcast.serialization.osgi;

import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.hazelcast.serialization.PortableIDFactory;


public class HazelcastSerializationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CustomPortableFactory.class, new PortableIDFactory());
        
    }
    
}
