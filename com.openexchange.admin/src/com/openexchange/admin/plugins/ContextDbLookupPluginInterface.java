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

package com.openexchange.admin.plugins;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link ContextDbLookupPluginInterface}
 *
 * Database lookup related plugin interface to be able to manipulate {@link Context} instances before or after they are looked up
 * from the database.
 *
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @since v7.10.3
 */
public interface ContextDbLookupPluginInterface {

    /**
     * Ability to manipulate the given {@link Context} as provided via provisioning API before it is being looked
     * up from the database.
     *
     * @param credentials The admin credentials
     * @param ctx The contexts
     * @throws PluginException When manipulating data fails
     */
    public void beforeContextDbLookup(final Credentials credentials, final Context[] ctx) throws PluginException;

    /**
     * Ability to manipulate the given {@link Context} loaded from the database before handing out via API
     *
     * @param credentials The admin credentials
     * @param ctx The contexts
     * @throws PluginException When manipulating data fails
     */
    public void afterContextDbLookup(final Credentials credentials, final Context[] ctx) throws PluginException;

    /**
     * Ability to manipulate the searchPattern before it is used to search Contexts within the database.
     *
     * @param credentials The admin credentials
     * @param searchPattern The search pattern
     * @return manipulated The manipulated search pattern
     * @throws PluginException When manipulating data fails
     */
    public String searchPatternDbLookup(final Credentials credentials, final String searchPattern) throws PluginException;
}
