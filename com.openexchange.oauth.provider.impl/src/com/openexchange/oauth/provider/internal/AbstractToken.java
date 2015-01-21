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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.internal;

import java.util.Date;
import com.openexchange.oauth.provider.OAuthToken;
import com.openexchange.oauth.provider.Scope;

/**
 * {@link AbstractToken}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public abstract class AbstractToken implements OAuthToken {

    private int contextID;
    private int userID;
    private String token;
    private Date expirationDate;
    private Scope scope;

    public AbstractToken(int contextId, int userId, String token, Long lifetime, Scope scope) {
        this.contextID = contextId;
        this.userID = userId;
        this.token = token;
        this.expirationDate = new Date(System.currentTimeMillis() + lifetime);
        this.scope = scope;
    }

    @Override
    public int getContextID() {
        return contextID;
    }

    @Override
    public int getUserID() {
        return userID;
    }

    @Override
    public String getAccessToken() {
        return token;
    }

    @Override
    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    public void setContextID(int contextID) {
        this.contextID = contextID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

}
