/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
