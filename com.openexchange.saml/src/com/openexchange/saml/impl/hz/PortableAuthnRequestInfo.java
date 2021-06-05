/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.saml.impl.hz;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.saml.state.AuthnRequestInfo;
import com.openexchange.saml.state.DefaultAuthnRequestInfo;

/**
 * {@link PortableAuthnRequestInfo}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class PortableAuthnRequestInfo extends AbstractCustomPortable {

    private static final String REQUEST_ID = "requestId";

    private static final String DOMAIN_NAME = "domainName";

    private static final String LOGIN_PATH = "loginPath";

    private static final String CLIENT = "client";

    private static final String DEEP_LINK = "deepLink";

    private AuthnRequestInfo delegate;

    public PortableAuthnRequestInfo() {
        super();
    }

    PortableAuthnRequestInfo(AuthnRequestInfo delegate) {
        super();
        setDelegate(delegate);
    }

    void setDelegate(AuthnRequestInfo delegate) {
        this.delegate = delegate;
    }

    AuthnRequestInfo getDelegate() {
        return delegate;
    }

    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_SAML_AUTHN_REQUEST_INFO;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(REQUEST_ID, delegate.getRequestId());
        writer.writeUTF(DOMAIN_NAME, delegate.getDomainName());
        writer.writeUTF(LOGIN_PATH, delegate.getLoginPath());
        writer.writeUTF(CLIENT, delegate.getClientID());
        writer.writeUTF(DEEP_LINK, delegate.getUriFragment());
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        DefaultAuthnRequestInfo dari = new DefaultAuthnRequestInfo();
        dari.setDomainName(reader.readUTF(DOMAIN_NAME));
        dari.setRequestId(reader.readUTF(REQUEST_ID));
        dari.setLoginPath(reader.readUTF(LOGIN_PATH));
        dari.setClientID(reader.readUTF(CLIENT));
        dari.setUriFragment(reader.readUTF(DEEP_LINK));
        setDelegate(dari);
    }

}
