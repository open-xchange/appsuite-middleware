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

package com.openexchange.admin.reseller.plugins;

import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link OXResellerPluginInterface}
 *
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @since v7.10.3
 */
public interface OXResellerPluginInterface {

    /**
     * Hook into the creation process; at this point, ResellerAdmin has already been created in the database
     *
     * @param adm The ResellerAdmin account
     * @param creds The credentials
     * @return pass through of the adm parameter
     * @throws PluginException
     */
    public ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws PluginException;

    /**
     * Hook into the change process; at this point, the change already has happened in the database
     *
     * @param adm The ResellerAdmin account
     * @param creds The credentials
     * @throws PluginException
     */
    public void change(final ResellerAdmin adm, final Credentials creds) throws PluginException;

    /**
     * Hook into the change process before the change in the database has happened
     *
     * @param adm The ResellerAdmin account
     * @param creds The credentials
     * @throws PluginException
     */
    public void beforeChange(final ResellerAdmin adm, final Credentials creds) throws PluginException;

    /**
     * Hook into the deletion process before the deletion in the database
     *
     * @param adm The ResellerAdmin account
     * @param creds The credentials
     * @throws PluginException
     */
    public void delete(final ResellerAdmin adm, final Credentials creds) throws PluginException;
}
