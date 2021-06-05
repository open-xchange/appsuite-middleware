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
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProtocolException;


/**
 * {@link WebdavReportAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class WebdavReportAction extends AbstractAction {

    private final Protocol protocol;

    public WebdavReportAction(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
	public void perform(WebdavRequest req, WebdavResponse res) throws WebdavProtocolException {
        try {
            Document reportQuery = req.getBodyAsDocument();
            Element root = reportQuery.getRootElement();
            String ns = root.getNamespace().getURI();
            String name = root.getName();

            WebdavAction reportAction = protocol.getReportAction(ns, name);

            if (reportAction == null) {
            	throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
            }

            reportAction.perform(req, res);

        } catch (JDOMException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_BAD_REQUEST, e);
        } catch (IOException e) {
            throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e);
        }
    }
}
