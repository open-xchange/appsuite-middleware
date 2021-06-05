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

package com.openexchange.oidc.hz;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.oidc.state.LogoutRequestInfo;
import com.openexchange.oidc.state.impl.DefaultLogoutRequestInfo;

/**
 * {@link PortableLogoutRequest} Contains all needed information to load a {@link LogoutRequestInfo}
 * identified by its state.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class PortableLogoutRequest extends AbstractCustomPortable {

    private static final String STATE = "state";
    private static final String DOMAINNAME = "domainname";
    private static final String SESSIONID = "sessionId";
    private static final String REQUEST_URI = "requestURI";
    
    private LogoutRequestInfo delegate;
    
    
    public PortableLogoutRequest() {
        super();
    }
    
    public PortableLogoutRequest(LogoutRequestInfo delegate) {
        super();
        this.delegate = delegate;
    }
    
    public LogoutRequestInfo getDelegate() {
        return delegate;
    }
    
    
    public void setDelegate(LogoutRequestInfo delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public int getClassId() {
        return CustomPortable.PORTABLE_OIDC_LOGOUT_REQUEST_INFO;
    }
    
    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(STATE, delegate.getState());
        writer.writeUTF(DOMAINNAME, delegate.getDomainName());
        writer.writeUTF(SESSIONID, delegate.getSessionId());
        writer.writeUTF(REQUEST_URI, delegate.getRequestURI());
    }
    
    @Override
    public void readPortable(PortableReader reader) throws IOException {
        DefaultLogoutRequestInfo logoutRequestInfo = new DefaultLogoutRequestInfo(reader.readUTF(STATE), reader.readUTF(DOMAINNAME), reader.readUTF(SESSIONID), reader.readUTF(REQUEST_URI));
        this.setDelegate(logoutRequestInfo);
    }
}
