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

import javax.servlet.http.HttpServletResponse;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class WebdavStructureAction extends AbstractAction {

	private final WebdavFactory factory;

	public WebdavStructureAction(final WebdavFactory factory) {
		this.factory = factory;
	}

	// Returns the status for a successful move/copy
	protected void checkOverwrite(final WebdavRequest req) throws WebdavProtocolException{
		if (req.getHeader("Overwrite") != null && "F".equals(req.getHeader("Overwrite"))){
			final LoadingHints loadingHints = new LoadingHints();
			loadingHints.setUrl(req.getDestinationUrl());
			loadingHints.setDepth(WebdavCollection.INFINITY);
			loadingHints.setProps(LoadingHints.Property.NONE);
			preLoad(loadingHints);

			final WebdavResource dest = req.getDestination();

			if (dest==null || !dest.exists()) {
				return;
			}

			if (dest.isCollection()) {
				final int depth = req.getDepth(WebdavCollection.INFINITY);

				final int sourceUrlLength = req.getUrl().size();
				final WebdavPath destUrl = req.getDestinationUrl();

				for(final WebdavResource res : req.getCollection().toIterable(depth)) {
					WebdavPath url = res.getUrl();
					url = destUrl.dup().append(url.subpath(sourceUrlLength));
					final WebdavResource d = factory.resolveResource(url);
					if (d.exists() && !d.isCollection()) {
						throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
					}
				}

			} else {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
			}

		}
		return;
	}

	protected int chooseReturnCode(final WebdavRequest req) throws WebdavProtocolException {
        return (req.getDestination() != null && req.getDestination().exists()) ? HttpServletResponse.SC_NO_CONTENT : HttpServletResponse.SC_CREATED;
	}

	protected void checkSame(final WebdavRequest req) throws WebdavProtocolException {
		if (req.getUrl().equals(req.getDestinationUrl())) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_FORBIDDEN);
		}
	}

}
