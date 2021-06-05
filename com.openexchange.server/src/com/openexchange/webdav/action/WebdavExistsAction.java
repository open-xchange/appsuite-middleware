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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public class WebdavExistsAction extends AbstractAction {
    private boolean tolerateLockNull = false;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavExistsAction.class);

    private static final String NOT_FOUND = "There is nothing here, sorry.";

    @Override
	public void perform(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
		if (!req.getResource().exists()) {
		    notFound(req, res);
        }
        if (req.getResource().isLockNull() && !tolerateLockNull) {
            notFound(req, res);
        }
        yield(req,res);
	}

    private void notFound(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
        try {
            res.sendString(NOT_FOUND);
        } catch (IOException e) {
            LOG.debug("Client gone?", e);
        }
        throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_NOT_FOUND);
    }

    public void setTolerateLockNull(final boolean b) {
        tolerateLockNull = b;
    }
}
