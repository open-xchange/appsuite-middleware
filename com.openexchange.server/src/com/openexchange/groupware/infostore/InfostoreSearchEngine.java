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

package com.openexchange.groupware.infostore;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAware;

/**
 * {@link InfostoreSearchEngine} - The Infostore search engine.
 */
@SingletonService
public interface InfostoreSearchEngine extends TransactionAware{

    /**
     * Constant for descending sort order
     */
	static final int DESC = -1;

	/**
	 * Constant for ascending sort order
	 */
	static final int ASC = 1;

	/**
	 * Constant for no specified sort order
	 */
	static final int UNORDERED = 0;

	/**
	 * Indicates no defined folder
	 */
	static final int NO_FOLDER = -10;

	/**
	 * Indicates a not set parameter
	 */
	static final int NOT_SET = -11;

    SearchIterator<DocumentMetadata> search(ServerSession session, String query, int folderId, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException;

    SearchIterator<DocumentMetadata> search(ServerSession session, String query, int folderId, boolean includeSubfolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException;

    SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, int[] folderIds, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException;

    SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, int folderId, boolean includeSubfolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException;

}
