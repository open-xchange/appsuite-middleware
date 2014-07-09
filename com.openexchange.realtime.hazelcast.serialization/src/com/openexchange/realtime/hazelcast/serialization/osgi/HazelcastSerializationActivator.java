package com.openexchange.realtime.hazelcast.serialization.osgi;

import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.hazelcast.serialization.PortableIDFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableMemberPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableNotInternalPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableSelectorChoiceFactory;


public class HazelcastSerializationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CustomPortableFactory.class, new PortableIDFactory());
        registerService(CustomPortableFactory.class, new PortableSelectorChoiceFactory());
        registerService(CustomPortableFactory.class, new PortableNotInternalPredicateFactory());
        registerService(CustomPortableFactory.class, new PortableMemberPredicateFactory());
    }
    
}
