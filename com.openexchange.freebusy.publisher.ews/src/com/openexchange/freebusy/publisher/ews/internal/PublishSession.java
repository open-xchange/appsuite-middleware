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

package com.openexchange.freebusy.publisher.ews.internal;

import java.util.Collections;
import java.util.Set;
import com.openexchange.session.Session;

/**
 * {@link PublishSession}
 *
 * Simulated session used to read internal free/busy data.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class PublishSession implements Session {

    private final int contextID;
    private final int userID;

    /**
     * Initializes a new {@link PublishSession}.
     *
     * @param contextID The context ID
     * @param userID The user ID
     */
    public PublishSession(int contextID, int userID) {
        super();
        this.contextID = contextID;
        this.userID = userID;
    }

    @Override
    public int getContextId() {
        return this.contextID;
    }

    @Override
    public int getUserId() {
        return this.userID;
    }

    @Override
    public String getLocalIp() {
        // Nothing to do
        return null;
    }

    @Override
    public void setLocalIp(String ip) {
        // Nothing to do

    }

    @Override
    public String getLoginName() {
        // Nothing to do
        return null;
    }

    @Override
    public boolean containsParameter(String name) {
        // Nothing to do
        return false;
    }

    @Override
    public Object getParameter(String name) {
        // Nothing to do
        return null;
    }

    @Override
    public String getPassword() {
        // Nothing to do
        return null;
    }

    @Override
    public String getRandomToken() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSecret() {
        // Nothing to do
        return null;
    }

    @Override
    public String getSessionID() {
        // Nothing to do
        return null;
    }

    @Override
    public String getUserlogin() {
        // Nothing to do
        return null;
    }

    @Override
    public String getLogin() {
        // Nothing to do
        return null;
    }

    @Override
    public void setParameter(String name, Object value) {
        // Nothing to do
    }

    @Override
    public String getAuthId() {
        // Nothing to do
        return null;
    }

    @Override
    public String getHash() {
        // Nothing to do
        return null;
    }

    @Override
    public void setHash(String hash) {
        // Nothing to do
    }

    @Override
    public String getClient() {
        // Nothing to do
        return null;
    }

    @Override
    public void setClient(String client) {
        // Nothing to do
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        return Collections.emptySet();
    }
}
