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


package com.openexchange.file.storage;

import com.openexchange.session.Session;

/**
 * {@link FileStorageServiceFactory} - A factory for creating needed instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorageServiceFactory {

	/**
	 * Gets the account access.
	 *
	 * @param accountId The account identifier
	 * @param session The session
	 * @return The appropriate account access
	 */
	FileStorageAccountAccess getAccountAccess(String accountId, Session session);

	/**
	 * Gets the account manager.
	 *
	 * @return The account manager.
	 */
	FileStorageAccountManager getAccountManager();

	/**
	 * Gets the file access.
	 *
	 * @param session The session
	 * @param accountId The account identifier
	 * @return The file access
	 */
	FileStorageFileAccess getFileAccess(Session session, String accountId);

	/**
     * Gets the folder access.
     *
     * @param session The session
     * @param accountId The account identifier
     * @return The folder access
     */
	FileStorageFolderAccess getFolderAccess(Session session, String accountId);

	/**
	 * Gets the associated file storage service.
	 *
	 * @return The file storage service
	 */
	FileStorageService getFileStorageService();

	/**
	 * Gets mandatory service.
	 *
	 * @param clazz The service's class
	 * @return The service
	 */
	<S extends Object> S getService(final Class<? extends S> clazz);

	/**
     * Gets optional service.
     *
     * @param clazz The service's class
     * @return The service or <code>null</code>
     */
	<S extends Object> S getOptionalService(Class<? extends S> clazz);

}
