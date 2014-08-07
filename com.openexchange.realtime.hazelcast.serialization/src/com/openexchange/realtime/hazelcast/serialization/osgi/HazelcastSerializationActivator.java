
package com.openexchange.realtime.hazelcast.serialization.osgi;

import com.openexchange.hazelcast.serialization.CustomPortableFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.realtime.hazelcast.serialization.PortableContextPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableIDFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableMemberPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableNotInternalPredicateFactory;
import com.openexchange.realtime.hazelcast.serialization.PortablePresenceFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableResourceFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableRoutingInfoFactory;
import com.openexchange.realtime.hazelcast.serialization.PortableSelectorChoiceFactory;

public class HazelcastSerializationActivator extends HousekeepingActivator {

    @Override
    protected Class<?>[] getNeededServices() {
        return null;
    }

    @Override
    protected void startBundle() throws Exception {
        registerService(CustomPortableFactory.class, new PortableContextPredicateFactory());
        registerService(CustomPortableFactory.class, new PortableIDFactory());
        registerService(CustomPortableFactory.class, new PortableMemberPredicateFactory());
        registerService(CustomPortableFactory.class, new PortableNotInternalPredicateFactory());
        registerService(CustomPortableFactory.class, new PortablePresenceFactory());
        registerService(CustomPortableFactory.class, new PortableResourceFactory());
        registerService(CustomPortableFactory.class, new PortableRoutingInfoFactory());
        registerService(CustomPortableFactory.class, new PortableSelectorChoiceFactory());
    }

}
