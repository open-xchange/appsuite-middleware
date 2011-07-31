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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.subscribe;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.session.Session;


public class TargetFolderSession implements Session {
    private final TargetFolderDefinition target;
    private final Map<String, Object> params = new HashMap<String, Object>();

    public TargetFolderSession(final TargetFolderDefinition target){
        this.target = target;
    }

    //IMPLEMENTED:
    public int getContextId() {
        return target.getContext().getContextId();
    }

    public int getUserId() {
        return target.getUserId();
    }

    //NOT IMPLEMENTED AT ALL:
    public String getLocalIp() {
        throw new UnsupportedOperationException();
    }

    public String getLogin() {
        throw new UnsupportedOperationException();
    }

    public String getLoginName() {
        throw new UnsupportedOperationException();
    }

    public Object getParameter(final String name) {
        return params.get(name);
    }

    public boolean containsParameter(final String name) {
        return params.containsKey(name);
    }

    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    public String getRandomToken() {
        throw new UnsupportedOperationException();
    }

    public String getSecret() {
        throw new UnsupportedOperationException();
    }

    public String getSessionID() {
        throw new UnsupportedOperationException();
    }

    public String getUserlogin() {
        throw new UnsupportedOperationException();
    }

    public void removeRandomToken() {
        throw new UnsupportedOperationException();
    }

    public void setParameter(final String name, final Object value) {
        params.put(name, value);
    }

    public String getAuthId() {
        throw new UnsupportedOperationException();
    }

    public String getHash() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setLocalIp(String ip) {
        // Nothing to do here.
    }

    public void setHash(String hash) {
        // TODO Auto-generated method stub
    }

    public String getClient() {
        return null;
    }

    public void setClient(String client) {
        // Nothing to do.
    }
}
