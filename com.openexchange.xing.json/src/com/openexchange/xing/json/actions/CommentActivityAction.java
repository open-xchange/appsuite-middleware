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

package com.openexchange.xing.json.actions;

import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;

/**
 * {@link CommentActivityAction}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CommentActivityAction extends AbstractXingAction {

	/**
	 * Initializes a new {@link CommentActivityAction}.
	 */
	public CommentActivityAction(ServiceLookup serviceLookup) {
		super(serviceLookup);
	}

	@Override
	protected AJAXRequestResult perform(XingRequest req) throws OXException, JSONException, XingException {
		String activityId = getMandatoryStringParameter(req, "activity_id");
		String text = getMandatoryStringParameter(req, "text");

		if (text.length() > 600) {
			throw XingExceptionCodes.COMMENT_SIZE_EXCEEDED.create();
		}

		String token = req.getParameter("testToken");
		String secret = req.getParameter("testSecret");
		final XingOAuthAccess xingOAuthAccess;
		{
			if (Strings.isNotEmpty(token) && Strings.isNotEmpty(secret)) {
				xingOAuthAccess = getXingOAuthAccess(token, secret, req.getSession());
			} else {
				xingOAuthAccess = getXingOAuthAccess(req);
			}
		}

		XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
		xingAPI.commentActivity(activityId, text);

		return new AJAXRequestResult(Boolean.TRUE, "native");
	}
}
