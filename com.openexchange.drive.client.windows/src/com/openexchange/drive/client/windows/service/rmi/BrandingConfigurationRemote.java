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

package com.openexchange.drive.client.windows.service.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link BrandingConfigurationRemote} is a rmi interface to reload the current branding configuration's.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public interface BrandingConfigurationRemote extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = BrandingConfigurationRemote.class.getSimpleName();

    /**
     * This method reloads the branding configurations and the corresponding update files.
     * It uses the last used path. In the major of cases this will be the path specified in <code>com.openexchange.drive.updater.path</code>.
     * 
     * @return A list of the loaded branding identifiers.
     * @throws OXException if the branding folder is missing
     * @throws RemoteException
     */
    public List<String> reload() throws OXException, RemoteException;

    /**
     * This method reloads the branding configurations and the corresponding update files for given path.
     * It must be pointed out that <code>com.openexchange.drive.updater.path</code> will be ignored and the given path is used instead.
     * 
     * @param path The path to be used.
     * @return A list of the loaded branding identifiers.
     * @throws OXException if the branding folder is missing
     * @throws RemoteException
     */
    public List<String> reload(String path) throws RemoteException, OXException;

    /**
     * Retrieves all available branding's.
     *
     * @param validate If true retrieves only the branding's which include all the necessary files.
     * @param invalid_only Retrieves only the invalid branding's. The parameter validate must be true for this to take effect.
     * @return a list of branding identifiers
     * @throws OXException if it is unable to validate the branding's
     * @throws RemoteException
     */
    public List<String> getBrandings(boolean validate, boolean invalid_only) throws RemoteException, OXException;

}
