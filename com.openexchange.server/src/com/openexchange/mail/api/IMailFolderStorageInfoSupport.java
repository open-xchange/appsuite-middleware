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

package com.openexchange.mail.api;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailFolderInfo;

/**
 * {@link IMailFolderStorageInfoSupport} - Extends basic folder storage by mailbox info support.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IMailFolderStorageInfoSupport extends IMailFolderStorage {

    /**
     * Indicates if mailbox info is supported.
     *
     * @return <code>true</code> if supported; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    boolean isInfoSupported() throws OXException;

    /**
     * Gets the folder information for specified full name.
     *
     * @param fullName the folder full name
     * @return The folder information
     * @throws OXException If an error occurs
     */
    MailFolderInfo getFolderInfo(String fullName) throws OXException;

    /**
     * Gets the folder information listing for this folder storage
     *
     * @param subscribedOnly <code>true</code> to return only subscribed folder information; otherwise <code>false</code> to return all regardless of subscribed flag
     * @return The folder information listing
     * @throws OXException If an error occurs
     */
    List<MailFolderInfo> getAllFolderInfos(boolean subscribedOnly) throws OXException;

    /**
     * Gets the folder information listing for this folder storage
     *
     * @param optParentFullName The optional full name of the parent; if missing complete folder information is returned
     * @param subscribedOnly <code>true</code> to return only subscribed folder information; otherwise <code>false</code> to return all regardless of subscribed flag
     * @return The folder information listing
     * @throws OXException If an error occurs
     */
    List<MailFolderInfo> getFolderInfos(String optParentFullName, boolean subscribedOnly) throws OXException;

}
