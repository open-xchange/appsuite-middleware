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

package com.openexchange.folderstorage.messaging;

import com.openexchange.exception.OXException;

/**
 * {@link DefaultFolderFullnameProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DefaultFolderFullnameProvider {

    /**
     * Gets the fullname of default INBOX folder
     *
     * @return The fullname of default INBOX folder
     * @throws OXException If INBOX folder's fullname cannot be returned
     */
    public String getINBOXFolder() throws OXException;

    /**
     * Gets the fullname of default confirmed ham folder
     *
     * @return The fullname of default confirmed ham folder
     * @throws OXException If confirmed ham folder's fullname cannot be returned
     */
    public String getConfirmedHamFolder() throws OXException;

    /**
     * Gets the fullname of default confirmed spam folder
     *
     * @return The fullname of default confirmed spam folder
     * @throws OXException If confirmed spam folder's fullname cannot be returned
     */
    public String getConfirmedSpamFolder() throws OXException;

    /**
     * Gets the fullname of default drafts folder
     *
     * @return The fullname of default drafts folder
     * @throws OXException If draft folder's fullname cannot be returned
     */
    public String getDraftsFolder() throws OXException;

    /**
     * Gets the fullname of default spam folder
     *
     * @return The fullname of default spam folder
     * @throws OXException If spam folder's fullname cannot be returned
     */
    public String getSpamFolder() throws OXException;

    /**
     * Gets the fullname of default sent folder
     *
     * @return The fullname of default sent folder
     * @throws OXException If sent folder's fullname cannot be returned
     */
    public String getSentFolder() throws OXException;

    /**
     * Gets the fullname of default trash folder
     *
     * @return The fullname of default trash folder
     * @throws OXException If trash folder's fullname cannot be returned
     */
    public String getTrashFolder() throws OXException;

}
