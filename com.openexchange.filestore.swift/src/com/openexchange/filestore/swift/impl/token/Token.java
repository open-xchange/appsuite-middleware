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

package com.openexchange.filestore.swift.impl.token;

import java.util.Date;

/**
 * {@link Token}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Token {

    private final String id;
    private final Date expires;
    private final int hash;

    /**
     * Initializes a new {@link Token}.
     *
     * @param id The token identifier
     * @param expires The token expiry date
     * @throws IllegalArgumentException If <code>id</code> is <code>null</code>
     */
    public Token(String id, Date expires) {
        super();
        if (null == id) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
        this.expires = expires;
        hash = 31 * 1 + id.hashCode();
    }

    /**
     * Checks if this token is expired
     * <p>
     * Its expiry ended before now minus 5 seconds.
     *
     * @return <code>true</code> if expired; otherwise <code>false</code>
     */
    public boolean isExpired() {
        return expires.getTime() < System.currentTimeMillis() - 5000L;
    }

    /**
     * Gets the token identifier
     *
     * @return The token identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the expiry date
     *
     * @return The expiry date
     */
    public Date getExpires() {
        return expires;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Token)) {
            return false;
        }
        Token other = (Token) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Token [");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (expires != null) {
            builder.append("expires=").append(expires).append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

}
