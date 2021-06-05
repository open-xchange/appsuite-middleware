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

import static com.openexchange.webdav.action.WebdavOption.ACCESS_CONTROL;
import static com.openexchange.webdav.action.WebdavOption.ONE;
import static com.openexchange.webdav.action.WebdavOption.THREE;
import static com.openexchange.webdav.action.WebdavOption.TWO;
import java.util.EnumSet;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link DefaultWebdavOptionsAction} is the default webdav action which only returns the basic {@link WebdavOption}s. Other implementations can extend this class and overwrite {@link #getDAVOptions(ServerSession)}.
 */
public class DefaultWebdavOptionsAction extends AbstractAction {

    public static final Logger LOG = LoggerFactory.getLogger(DefaultWebdavOptionsAction.class);

    protected static final EnumSet<WebdavOption> GENERAL_DAV_OPTIONS = EnumSet.of(ONE, TWO, THREE, ACCESS_CONTROL);

	@Override
    public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
		response.setHeader("Content-Length", "0");
		response.setHeader("Allow", Strings.join(request.getResource().getOptions(), ","));
        try {
            response.setHeader("DAV", toCommaSeparatedList(getDAVOptions(ServerSessionAdapter.valueOf(request.getFactory().getSession()))));
        } catch (OXException e) {
            throw WebdavProtocolException.generalError(e, request.getDestinationUrl(), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
		response.setHeader("Accept-Ranges", "bytes");
		response.setHeader("MS-Author-Via", "DAV"); // Hack for Windows Webfolder
	}

	@SuppressWarnings("unused")
    protected EnumSet<WebdavOption> getDAVOptions(ServerSession session) throws OXException {
	    return GENERAL_DAV_OPTIONS;
	}

	/**
	 * Converts the available options into a comma separated string
	 *
	 * @param options The {@link EnumSet} of available options
	 * @return the options as a comma separated list
	 */
	protected static String toCommaSeparatedList(EnumSet<WebdavOption> options) {
	    return options.stream().map((o) -> o.getName()).collect(Collectors.joining(","));
	}

}
