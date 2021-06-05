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

package com.openexchange.messaging;

import com.openexchange.exception.OXException;


/**
 * {@link SimAccountAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimAccountAccess implements MessagingAccountAccess {

    private int accountId;
    private MessagingFolderAccess folderAccess;
    private MessagingMessageAccess messageAccess;

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public MessagingFolderAccess getFolderAccess() throws OXException {
        return folderAccess;
    }

    @Override
    public MessagingMessageAccess getMessageAccess() throws OXException {
        return messageAccess;
    }

    @Override
    public void close() {

    }

    @Override
    public void connect() throws OXException {

    }

    @Override
    public boolean ping() throws OXException {
        return true;
    }

    public void setMessageAccess(final SimMessageAccess access) {
        messageAccess = access;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public MessagingFolder getRootFolder() throws OXException {
        return getFolderAccess().getRootFolder();
    }

    @Override
    public boolean cacheable() {
        return true;
    }

}
