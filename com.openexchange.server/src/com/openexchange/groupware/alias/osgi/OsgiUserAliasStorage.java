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

package com.openexchange.groupware.alias.osgi;

import java.sql.Connection;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.server.ServiceExceptionCode;


/**
 * {@link OsgiUserAliasStorage}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.8.0
 */
public class OsgiUserAliasStorage extends ServiceTracker<UserAliasStorage, UserAliasStorage> implements UserAliasStorage {

    public OsgiUserAliasStorage(BundleContext context) {
        super(context, UserAliasStorage.class, null);
    }

    @Override
    public Set<String> getAliases(int contextId, int userId) throws OXException {
        return getUserAliasStorage().getAliases(contextId, userId);
    }

    @Override
    public int getUserId(int contextId, String alias) throws OXException {
        return getUserAliasStorage().getUserId(contextId, alias);
    }

    @Override
    public boolean createAlias(Connection con, int contextId, int userId, String alias) throws OXException {
        return getUserAliasStorage().createAlias(con, contextId, userId, alias);
    }

    @Override
    public boolean updateAlias(Connection con, int contextId, int userId, String oldAlias, String newAlias) throws OXException {
        return getUserAliasStorage().updateAlias(con, contextId, userId, oldAlias, newAlias);
    }

    @Override
    public boolean deleteAlias(Connection con, int contextId, int userId, String alias) throws OXException {
        return getUserAliasStorage().deleteAlias(con, contextId, userId, alias);
    }

    @Override
    public boolean deleteAliase(Connection con, int contextId, int userId) throws OXException {
        return getUserAliasStorage().deleteAliase(con, contextId, userId);
    }

    private UserAliasStorage getUserAliasStorage() throws OXException {
        UserAliasStorage userAlias = getService();
        if(userAlias == null) {
            throw ServiceExceptionCode.absentService(UserAliasStorage.class);
        }
        return userAlias;
    }
}
