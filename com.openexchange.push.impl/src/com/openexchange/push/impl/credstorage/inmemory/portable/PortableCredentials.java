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

package com.openexchange.push.impl.credstorage.inmemory.portable;

import java.io.IOException;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.CustomPortable;
import com.openexchange.push.credstorage.Credentials;

/**
 * {@link PortableCredentials}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PortableCredentials implements CustomPortable {

    /** The unique portable class ID of the {@link PortableSession} */
    public static final int CLASS_ID = 101;

    public static final String PARAMETER_CONTEXT_ID = "contextId";
    public static final String PARAMETER_USER_ID = "userId";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_LOGIN = "login";

    private int contextId;
    private int userId;
    private String password;
    private String login;

    /**
     * Initializes a new {@link PortableReservation}.
     */
    public PortableCredentials() {
        super();
    }

    /**
     * Initializes a new {@link PortableReservation}.
     */
    public PortableCredentials(Credentials source) {
        super();
        contextId = source.getContextId();
        userId = source.getUserId();
        password = source.getPassword();
        login = source.getLogin();
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
        writer.writeInt(PARAMETER_CONTEXT_ID, contextId);
        writer.writeInt(PARAMETER_USER_ID, userId);
        writer.writeUTF(PARAMETER_PASSWORD, password);
        writer.writeUTF(PARAMETER_LOGIN, login);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        contextId = reader.readInt(PARAMETER_CONTEXT_ID);
        userId = reader.readInt(PARAMETER_USER_ID);
        password = reader.readUTF(PARAMETER_PASSWORD);
        login = reader.readUTF(PARAMETER_LOGIN);
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
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the login
     *
     * @return The login
     */
    public String getLogin() {
        return login;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = prime * 1 + contextId;
        result = prime * result + ((login == null) ? 0 : login.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PortableCredentials)) {
            return false;
        }
        PortableCredentials other = (PortableCredentials) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (login == null) {
            if (other.login != null) {
                return false;
            }
        } else if (!login.equals(other.login)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
    }

}
