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

package com.openexchange.jslob;

import java.io.Serializable;

/**
 * {@link JSlobId} - The JSlob identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobId implements Serializable {

    private static final long serialVersionUID = -1733920133244012391L;

    private final int user;
    private final int context;
    private final String serviceId;
    private final String id;
    private final int hashCode;

    /**
     * Initializes a new {@link JSlobId}.
     *
     * @param serviceId The JSlob service identifier
     * @param id The JSlob identifier
     * @param user The user identifier
     * @param context The context identifier
     */
    public JSlobId(final String serviceId, final String id, final int user, final int context) {
        super();
        this.id = id;
        this.serviceId = serviceId;
        this.user = user;
        this.context = context;
        // Hash code
        final int prime = 31;
        int result = 1;
        result = prime * result + context;
        result = prime * result + user;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
        hashCode = result;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JSlobId)) {
            return false;
        }
        final JSlobId other = (JSlobId) obj;
        if (context != other.context) {
            return false;
        }
        if (user != other.user) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (serviceId == null) {
            if (other.serviceId != null) {
                return false;
            }
        } else if (!serviceId.equals(other.serviceId)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUser() {
        return user;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getContext() {
        return context;
    }

    /**
     * Gets the JSlob service identifier.
     *
     * @return The JSlob service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * Gets the JSlob identifier.
     *
     * @return The JSlob identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(96);
        builder.append("{user=").append(user).append(", context=").append(context);
        if (serviceId != null) {
            builder.append(", serviceId=").append(serviceId);
        }
        if (id != null) {
            builder.append(", id=").append(id);
        }
        builder.append('}');
        return builder.toString();
    }

}
