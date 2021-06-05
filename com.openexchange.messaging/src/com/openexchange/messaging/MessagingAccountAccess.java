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
 * {@link MessagingAccountAccess} - Provides access to a messaging account.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface MessagingAccountAccess extends MessagingResource {

    /**
     * Gets the account identifier of this access.
     *
     * @return The account identifier
     */
    public int getAccountId();

    /**
     * Gets the message access for associated account.
     *
     * @return The message access
     * @throws OXException If message access cannot be returned
     */
    public MessagingMessageAccess getMessageAccess() throws OXException;

    /**
     * Gets the folder access for associated account.
     *
     * @return The folder access
     * @throws OXException If folder access cannot be returned
     */
    public MessagingFolderAccess getFolderAccess() throws OXException;

    /**
     * Convenience method to obtain root folder in a fast way; meaning no default folder check is performed which is not necessary to return
     * the root folder.
     * <p>
     * The same result is yielded through calling <code>getFolderAccess().getRootFolder()</code> on a connected
     * {@link MessagingFolderAccess}.
     * <p>
     * Since this account access instance is connected if not already done before, the {@link #close()} operation should be invoked
     * afterwards:
     *
     * <pre>
     * final MessagingMessageAccess access = MailAccess.getInstance(session);
     * final MessagingFolder rootFolder = access.getRootFolder();
     * try {
     *     // Do something with root folder
     * } finally {
     *     access.close();
     * }
     * </pre>
     *
     * @throws OXException If returning the root folder fails
     */
    public MessagingFolder getRootFolder() throws OXException;

}
