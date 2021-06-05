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

package com.openexchange.webdav.action;

import static com.openexchange.java.Strings.isEmpty;
import com.openexchange.groupware.infostore.utils.InfostoreConfigUtils;
import com.openexchange.session.SessionHolder;

/**
 * {@link OXWebdavMaxUploadSizeAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXWebdavMaxUploadSizeAction extends WebdavMaxUploadSizeAction {

    private final SessionHolder sessionHolder;

	/**
	 * Initializes a new {@link OXWebdavMaxUploadSizeAction}.
	 *
	 * @param sessionHolder A reference to the session holder to use
	 */
	public OXWebdavMaxUploadSizeAction(SessionHolder sessionHolder) {
	    super();
	    this.sessionHolder = sessionHolder;
	}

	@Override
	public boolean fits(WebdavRequest req) {
		if (sessionHolder == null) {
			return true;
		}

		final long maxSize = InfostoreConfigUtils.determineRelevantUploadSize();
		if (maxSize < 1) {
			return true;
		}

		final String sContentLength = req.getHeader("content-length");
        return isEmpty(sContentLength) ? true : maxSize >= Long.parseLong(sContentLength);
	}

}
