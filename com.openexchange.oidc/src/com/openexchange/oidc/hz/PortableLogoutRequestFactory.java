package com.openexchange.oidc.hz;

import com.hazelcast.nio.serialization.Portable;
import com.openexchange.hazelcast.serialization.AbstractCustomPortableFactory;
import com.openexchange.hazelcast.serialization.CustomPortable;

public class PortableLogoutRequestFactory extends AbstractCustomPortableFactory{

    @Override
    public Portable create() {
        return new PortableLogoutRequest();
    }
    
    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_OIDC_LOGOUT_REQUEST_INFO;
    }
}
