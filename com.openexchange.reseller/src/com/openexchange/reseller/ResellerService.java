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

package com.openexchange.reseller;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.reseller.data.ResellerAdmin;

/**
 * {@link ResellerService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.3
 */
public interface ResellerService {

    /**
     * Retrieves the reseller administrator for the given context.
     *
     * @param cid The context id
     * @return The reseller administrator
     * @throws OXException If reseller administrator cannot be returned
     */
    ResellerAdmin getReseller(int cid) throws OXException;

    /**
     * Retrieves the reseller administrator with the specified identifier.
     *
     * @param resellerId The reseller identifier
     * @return The reseller administrator
     * @throws OXException If reseller administrator cannot be returned
     */
    ResellerAdmin getResellerById(int resellerId) throws OXException;

    /**
     * Retrieves the reseller administrator with the specified name.
     *
     * @param resellerName The reseller name
     * @return The reseller administrator
     * @throws OXException If reseller administrator cannot be returned
     */
    ResellerAdmin getResellerByName(String resellerName) throws OXException;

    /**
     * Retrieves the reseller administrator path for the specified context.
     * <p>
     * First in list is root reseller administrator, last one in list is the reseller administrator for given context.
     *
     * @param cid The context identifier
     * @return A {@link List} with the path of the reseller sub-administrators
     * @throws OXException If reseller administrator path cannot be returned
     */
    List<ResellerAdmin> getResellerAdminPath(int cid) throws OXException;

    /**
     * Retrieves all reseller sub-administrators for the specified parent reseller administrator.
     *
     * @param parentId The parent identifier
     * @return A list with all reseller sub-administrators
     * @throws OXException If sub-administrator cannot be returned
     */
    List<ResellerAdmin> getSubResellers(int parentId) throws OXException;

    /**
     * Retrieves all reseller administrators.
     *
     * @return The reseller administrators
     * @throws OXException If reseller administrators cannot be returned
     */
    List<ResellerAdmin> getAll() throws OXException;

}
