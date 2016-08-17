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
 *    trademarks of the OX Software GmbH group of companies.
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

import javax.servlet.http.HttpServletResponse;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavIfMatchAction extends AbstractAction {

    private final int statusCode;

    /**
     * Initializes a new {@link WebdavIfMatchAction}, throwing a {@link HttpServletResponse#SC_PRECONDITION_FAILED} error if the checked
     * condition is not satisfied by the underlying resource.
     */
    public WebdavIfMatchAction() {
        this(HttpServletResponse.SC_PRECONDITION_FAILED);
    }

    /**
     * Initializes a new {@link WebdavIfMatchAction}.
     *
     * @param statusCode The HTTP response status code to use if the condition is not satisfied, usually either
     *                   {@link HttpServletResponse#SC_NOT_MODIFIED} for <code>GET</code> or <code>HEAD</code> request, or
     *                   {@link HttpServletResponse#SC_PRECONDITION_FAILED} for others
     */
    public WebdavIfMatchAction(int statusCode) {
        super();
        this.statusCode = statusCode;
    }

	@Override
	public void perform(WebdavRequest request, WebdavResponse response) throws WebdavProtocolException {
		check(request, true);
		check(request, false);
		yield(request, response);
	}

	private void check(WebdavRequest req, boolean mustMatch) throws WebdavProtocolException {
		String header = req.getHeader(mustMatch ? "If-Match" : "If-None-Match");
		if (null != header) {
			WebdavResource res = req.getResource();
			if (res.exists() && res.isCollection()) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_PRECONDITION_FAILED);
			}
			boolean foundMatch = false;
			if (res.exists()) {
				String eTag = res.getETag();
				for (String tag : header.split("\\s*,\\s*")) {
					if (null != tag && 0 < tag.length()) {
						if ("*".equals(tag) || eTag.equals(tag) ||
								(tag.startsWith("\"") && tag.endsWith("\"") && 2 < tag.length() &&
										eTag.equals( tag.substring(1, tag.length() - 1)))) {
							foundMatch = true;
							break;
						}
					}
				}
			}
			if (foundMatch != mustMatch) {
			    throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), statusCode);
		    }
		}
	}

}
