/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
