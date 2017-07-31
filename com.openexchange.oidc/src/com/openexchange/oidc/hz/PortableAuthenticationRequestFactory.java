package com.openexchange.oidc.hz;

import com.hazelcast.nio.serialization.Portable;
import com.openexchange.hazelcast.serialization.AbstractCustomPortableFactory;
import com.openexchange.hazelcast.serialization.CustomPortable;

public class PortableAuthenticationRequestFactory extends AbstractCustomPortableFactory{

    @Override
    public Portable create() {
        return new PortableAuthenticationRequest();
    }

    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_OIDC_AUTHN_REQUEST_INFO;
    }

}
