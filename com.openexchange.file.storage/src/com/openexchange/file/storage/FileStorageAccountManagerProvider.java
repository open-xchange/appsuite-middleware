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

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageAccountManagerProvider} - Provides the {@link FileStorageAccountManager account manager} appropriate for a certain
 * {@link FileStorageService file storage service}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorageAccountManagerProvider {

    /**
     * The default ranking: <code>0</code>.
     */
    public static final int DEFAULT_RANKING = 0;

    /**
     * Whether this provider supports specified {@link FileStorageService file storage service}.
     *
     * @param serviceId The file storage service identifier
     * @return <code>true</code> if this provider supports specified file storage service; otherwise <code>false</code>
     */
    boolean supports(String serviceId);

    /**
     * Gets the appropriate file storage account manager for specified account identifier and session.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The file storage account manager or <code>null</code>
     * @throws OXException If retrieval fails
     */
    FileStorageAccountManager getAccountManager(String accountId, Session session) throws OXException;

    /**
     * Gets the appropriate account manager for specified {@link FileStorageService file storage service}.
     *
     * @param serviceId The file storage service identifier
     * @return The appropriate account manager for specified file storage service.
     * @throws OXException If an appropriate account manager cannot be returned
     */
    FileStorageAccountManager getAccountManagerFor(String serviceId) throws OXException;

    /**
     * Gets the ranking of this provider.
     * <p>
     * The ranking is used to determine the <i>natural order</i> of providers and the <i>default</i> provider to be returned.
     * <p>
     * A provider with a ranking of <code>Integer.MAX_VALUE</code> is very likely to be returned as the default service, whereas a provider
     * with a ranking of <code>Integer.MIN_VALUE</code> is very unlikely to be returned.
     *
     * @return The ranking
     */
    int getRanking();

}
