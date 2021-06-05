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

import java.util.Date;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavHeadAction extends AbstractAction {

	@Override
    public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavProtocolException {
		head(res,req.getResource(),-1);
	}

	protected final void head(final WebdavResponse res, final WebdavResource resource, final long overrideLength) throws WebdavProtocolException {
		if (resource == null) {
			return;
		}
		res.setHeader("Content-Type", resource.getContentType());
		if (!resource.isCollection()) {
			res.setHeader("Content-Length", (overrideLength == -1) ? resource.getLength().toString() : Long.toString(overrideLength));
		}
        String eTag = resource.getETag();
        if (null != eTag) {
            res.setHeader("ETag", Strings.quote(eTag, true));
        }
		res.setHeader("Accept-Ranges", "bytes");

        Date lastModified = resource.getLastModified();
        if (null != lastModified) {
            res.setHeader("Last-Modified", Tools.formatHeaderDate(lastModified));
        }
	}

}
