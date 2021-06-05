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
import com.openexchange.saml.state.DefaultLogoutRequestInfo;
import com.openexchange.saml.state.LogoutRequestInfo;


/**
 * {@link PortableLogoutRequestInfo}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class PortableLogoutRequestInfo extends AbstractCustomPortable  {

    private static final String REQUEST_ID = "requestId";

    private static final String DOMAIN_NAME = "domainName";

    private static final String SESSION_ID = "sessionId";

    private LogoutRequestInfo delegate;

    public PortableLogoutRequestInfo() {
        super();
    }

    PortableLogoutRequestInfo(LogoutRequestInfo delegate) {
        super();
        setDelegate(delegate);
    }

    void setDelegate(LogoutRequestInfo delegate) {
        this.delegate = delegate;
    }

    LogoutRequestInfo getDelegate() {
        return delegate;
    }

    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_SAML_LOGOUT_REQUEST_INFO;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(REQUEST_ID, delegate.getRequestId());
        writer.writeUTF(DOMAIN_NAME, delegate.getDomainName());
        writer.writeUTF(SESSION_ID, delegate.getSessionId());
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        DefaultLogoutRequestInfo dlri = new DefaultLogoutRequestInfo();
        dlri.setSessionId(reader.readUTF(SESSION_ID));
        dlri.setDomainName(reader.readUTF(DOMAIN_NAME));
        dlri.setRequestId(reader.readUTF(REQUEST_ID));
        setDelegate(dlri);
    }


}
