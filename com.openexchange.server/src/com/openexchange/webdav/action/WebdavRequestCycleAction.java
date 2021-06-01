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

import com.openexchange.webdav.protocol.WebdavProtocolException;

public class WebdavRequestCycleAction extends AbstractAction {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavRequestCycleAction.class);

	@Override
    public void perform(final WebdavRequest req, final WebdavResponse res)
			throws WebdavProtocolException {


		req.getFactory().beginRequest();
		boolean stopped = false;
		try {
			yield(req,res);
			req.getFactory().endRequest(200);
			stopped = true;
		} catch (WebdavProtocolException x) {
			LOG.debug("Got Webdav Exception", x);
			req.getFactory().endRequest(x.getStatus());
			stopped = true;
			throw x;
		} finally {
			if (!stopped) {
				req.getFactory().endRequest(500);
			}
		}
	}

}
