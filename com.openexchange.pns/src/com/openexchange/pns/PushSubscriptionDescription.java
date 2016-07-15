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

package com.openexchange.pns;

/**
 * {@link PushSubscriptionDescription}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class PushSubscriptionDescription implements PushSubscription {

    private int userId;
    private int contextId;
    private PushAffiliation affiliation;
    private String transportId;
    private String token;

    /**
     * Initializes a new {@link PushSubscriptionDescription}.
     */
    public PushSubscriptionDescription() {
        super();
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public PushAffiliation getAffiliation() {
        return affiliation;
    }

    @Override
    public String getTransportId() {
        return transportId;
    }

    @Override
    public String getToken() {
        return token;
    }

    /**
     * Sets the user identifier
     *
     * @param userId The user identifier to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Sets the context identifier
     *
     * @param contextId The context identifier to set
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Sets the affiliation
     *
     * @param affiliation The affiliation to set
     */
    public void setAffiliation(PushAffiliation affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * Sets the transport identifier
     *
     * @param transportId The transport identifier to set
     */
    public void setTransportId(String transportId) {
        this.transportId = transportId;
    }

    /**
     * Sets the token
     *
     * @param token The token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

}
