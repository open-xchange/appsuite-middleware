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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.plugins;

import java.util.List;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

/**
 * @author choeger
 * 
 */
public interface OXContextPluginInterface {

    public void change(final Context ctx, final Credentials auth) throws PluginException;

    public Context create(final Context ctx, final User admin_user,final UserModuleAccess access, final Credentials auth) throws PluginException;

    public Context create(final Context ctx, final User admin_user, final Credentials auth) throws PluginException;

    public void delete(final Context ctx, final Credentials auth) throws PluginException;

    public void disable(final Context ctx, final Credentials auth) throws PluginException;

    public void disableAll(final Credentials auth) throws PluginException;

    public void enable(final Context ctx, final Credentials auth) throws PluginException;

    public void enableAll(final Credentials auth) throws PluginException;

    public List<Context> getData(final List<Context> ctx, final Credentials auth) throws PluginException;

    /**
     * This method only returns how the core list query must be extended, the final data for a context is fetched by
     * the {@link #getData(Context, Credentials)} method which must eventually enhance the data in the context object.
     * This method must return null if no changes must be made to the core sql query.
     * Note that each list method must enhance and eventually given old {@link SQLQueryExtension}. Hence a given old
     * {@link SQLQueryExtension} must pass through this method without being discarded.
     * 
     * @param search_pattern
     * @param queryex The former SQLQueryExtension
     * @param auth
     * @return
     * @throws PluginException
     */
    public SQLQueryExtension list(final String search_pattern, final SQLQueryExtension queryex, final Credentials auth) throws PluginException;

    public void changeModuleAccess(final Context ctx, final UserModuleAccess access,final Credentials auth) throws PluginException; 

    public void changeModuleAccess(final Context ctx, final String access_combination_name,final Credentials auth) throws PluginException;

    public void downgrade(final Context ctx, final Credentials auth) throws PluginException;

    public String getAccessCombinationName(final Context ctx, final Credentials auth) throws PluginException;

    public UserModuleAccess getModuleAccess(final Context ctx, final Credentials auth) throws PluginException;
}
