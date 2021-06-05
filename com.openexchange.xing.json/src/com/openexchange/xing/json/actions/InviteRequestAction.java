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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.xing.InvitationStats;
import com.openexchange.xing.User;
import com.openexchange.xing.XingAPI;
import com.openexchange.xing.access.XingExceptionCodes;
import com.openexchange.xing.access.XingOAuthAccess;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.json.XingRequest;
import com.openexchange.xing.session.WebAuthSession;


/**
 * {@link InviteRequestAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class InviteRequestAction extends AbstractXingAction {

	/**
	 * Initializes a new {@link InviteRequestAction}.
	 */
	public InviteRequestAction(final ServiceLookup services) {
		super(services);
	}

	@Override
	protected AJAXRequestResult perform(final XingRequest req) throws OXException, JSONException, XingException {
		// Get & validate E-Mail address
		String address = getMandatoryStringParameter(req, "email");
		address = validateMailAddress(address);

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

		final XingAPI<WebAuthSession> xingAPI = xingOAuthAccess.getXingAPI();
		final InvitationStats invitationStats = xingAPI.invite(Collections.<String> singletonList(address), null, null);

		if (invitationStats.getInvitationsSent() > 0) {
			return new AJAXRequestResult(Boolean.TRUE, "native");
		}

		final List<String> alreadyInvited = invitationStats.getAlreadyInvited();
		if (null != alreadyInvited && new HashSet<String>(alreadyInvited).contains(address)) {
			throw XingExceptionCodes.ALREADY_INVITED.create(address);
		}

		final List<String> invalidAddresses = invitationStats.getInvalidAddresses();
		if (null != invalidAddresses && new HashSet<String>(invalidAddresses).contains(address)) {
			throw XingExceptionCodes.INVALID_EMAIL_ADDRESS.create(address);
		}

		final String xingId = xingAPI.findByEmail(address);

		final List<User> alreadyMember = invitationStats.getAlreadyMember();
		if (null != alreadyMember) {
			for (final User member : alreadyMember) {
				if (member.getId().equals(xingId)) {
					throw XingExceptionCodes.ALREADY_MEMBER.create(address);
				}
			}
		}

		throw XingExceptionCodes.INVITATION_FAILED.create();
	}

}
