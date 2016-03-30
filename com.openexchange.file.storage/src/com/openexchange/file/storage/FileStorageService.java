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

import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link FileStorageService} - The file storage service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public interface FileStorageService extends FileStorageConstants {

    /**
     * Gets the identifier of this file storage service.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Get the form description.
     *
     * @return The form description
     */
    DynamicFormDescription getFormDescription();

    /**
     * Gets those properties from configuration which should be encrypted.
     *
     * @return Those properties from configuration which should be encrypted
     */
    Set<String> getSecretProperties();

    /**
     * Gets the account manager for this file storage service.
     *
     * @return The account manager
     * @throws OXException
     */
    FileStorageAccountManager getAccountManager() throws OXException;

    /**
     * Gets the account access for specified account identifier.
     *
     * @param accountId The account identifier
     * @param session The session providing needed user data
     * @return The account access for specified account identifier
     * @throws OXException If account access cannot be returned for given account identifier
     */
    FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException;

}
