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
import com.openexchange.java.Strings;
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
            boolean foundMatch = matchesAny(req.getResource(), header.split("\\s*,\\s*"));
            if (foundMatch != mustMatch) {
                throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), statusCode);
            }
        }
	}

    private static boolean matchesAny(WebdavResource resource, String[] tags) throws WebdavProtocolException {
        if (resource.exists()) {
            String eTag = resource.getETag();
            for (String tag : tags) {
                if (Strings.isEmpty(tag)) {
                    continue;
                }
                if ("*".equals(tag) || tag.equals(eTag)) {
                    return true;
                }
                if (tag.startsWith("\"") && tag.endsWith("\"") && 2 < tag.length() && eTag.equals(tag.substring(1, tag.length() - 1))) {
                    return true;
                }
            }
        }
        return false;
    }

}
