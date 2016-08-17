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

package com.openexchange.oauth.provider.impl.authcode.portable;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;

/**
 * {@link PortableAuthCodeInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableAuthCodeInfo implements CustomPortable {

    /** The unique portable class ID of the {@link PortableAuthCodeInfo} */
    public static final int CLASS_ID = 20;

    public static final String PARAMETER_CLIENT_ID = "clientId";
    private static final String PARAMETER_REDIRECT_URI = "redirectURI";
    public static final String PARAMETER_SCOPE = "scope";
    public static final String PARAMETER_USER_ID = "userId";
    public static final String PARAMETER_CONTEXT_ID = "contextId";
    public static final String PARAMETER_NANOS = "nanos";

    // --------------------------------------------------------------------------------------------------------------------

    private String clientId;
    private String redirectURI;
    private String scope;
    private int userId;
    private int contextId;
    private long nanos;

    /**
     * Initializes a new {@link PortableAuthCodeInfo}.
     */
    public PortableAuthCodeInfo() {
        super();
    }

    /**
     * Initializes a new {@link PortableAuthCodeInfo}.
     */
    public PortableAuthCodeInfo(String clientId, String redirectURI, Scope scope, int userId, int contextId, long nanos) {
        super();
        this.clientId = clientId;
        this.redirectURI = redirectURI;
        this.scope = null == scope ? "" : scope.toString();
        this.userId = userId;
        this.contextId = contextId;
        this.nanos = nanos;
    }

    @Override
    public int getFactoryId() {
        return FACTORY_ID;
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeUTF(PARAMETER_CLIENT_ID, clientId);
        writer.writeUTF(PARAMETER_REDIRECT_URI, redirectURI);
        writer.writeUTF(PARAMETER_SCOPE, scope);
        writer.writeInt(PARAMETER_USER_ID, userId);
        writer.writeInt(PARAMETER_CONTEXT_ID, contextId);
        writer.writeLong(PARAMETER_NANOS, nanos);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        clientId = reader.readUTF(PARAMETER_CLIENT_ID);
        redirectURI = reader.readUTF(PARAMETER_REDIRECT_URI);
        scope = reader.readUTF(PARAMETER_SCOPE);
        userId = reader.readInt(PARAMETER_USER_ID);
        contextId = reader.readInt(PARAMETER_CONTEXT_ID);
        nanos = reader.readLong(PARAMETER_NANOS);
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the redirectURI
     *
     * @return The redirectURI
     */
    public String getRedirectURI() {
        return redirectURI;
    }

    /**
     * Gets the scope
     *
     * @return The scope
     */
    public String getScope() {
        return scope;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the nanos
     *
     * @return The nanos
     */
    public long getNanos() {
        return nanos;
    }

}
