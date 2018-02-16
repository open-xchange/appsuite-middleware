/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
