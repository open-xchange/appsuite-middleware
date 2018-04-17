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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.authentication;

/**
 * {@link ContextAndUserInfo} - Provides the context and user information that is supposed to be used to resolve to a (numeric) context/user identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ContextAndUserInfo {

    private final String userInfo;
    private final String contextInfo;
    private int hash; // No need to be synchronized

    /**
     * Initializes a new {@link ContextAndUserInfo} with <code>"defaultcontext"</code> as context information.
     *
     * @param userInfo The user information
     */
    public ContextAndUserInfo(String userInfo) {
        this(userInfo, "defaultcontext");
    }

    /**
     * Initializes a new {@link ContextAndUserInfo}.
     *
     * @param userInfo The user information
     * @param contextInfo The context information
     */
    public ContextAndUserInfo(String userInfo, String contextInfo) {
        super();
        this.userInfo = userInfo;
        this.contextInfo = contextInfo;
        hash = 0;
    }

    /**
     * Gets the user information
     *
     * @return The user information
     */
    public String getUserInfo() {
        return userInfo;
    }

    /**
     * Gets the context information
     *
     * @return The context information
     */
    public String getContextInfo() {
        return contextInfo;
    }

    @Override
    public int hashCode() {
        int h = this.hash;
        if (h == 0) {
            int prime = 31;
            h = prime * 1 + ((contextInfo == null) ? 0 : contextInfo.hashCode());
            h = prime * h + ((userInfo == null) ? 0 : userInfo.hashCode());
            this.hash = h;
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContextAndUserInfo)) {
            return false;
        }
        ContextAndUserInfo other = (ContextAndUserInfo) obj;
        if (contextInfo == null) {
            if (other.contextInfo != null) {
                return false;
            }
        } else if (!contextInfo.equals(other.contextInfo)) {
            return false;
        }
        if (userInfo == null) {
            if (other.userInfo != null) {
                return false;
            }
        } else if (!userInfo.equals(other.userInfo)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        if (userInfo != null) {
            builder.append("userInfo=").append(userInfo).append(", ");
        }
        if (contextInfo != null) {
            builder.append("contextInfo=").append(contextInfo);
        }
        return builder.toString();
    }

}
