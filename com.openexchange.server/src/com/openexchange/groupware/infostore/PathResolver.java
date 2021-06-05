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
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tx.TransactionAware;
import com.openexchange.webdav.protocol.WebdavPath;

public interface PathResolver extends TransactionAware {
	public Resolved resolve(int relativeToFolder, WebdavPath path, ServerSession session) throws OXException;
	public WebdavPath getPathForFolder(int relativeToFolder, int folderId, ServerSession session) throws OXException;
	public WebdavPath getPathForDocument(int relativeToFolder, int documentId, ServerSession session) throws OXException;
}
