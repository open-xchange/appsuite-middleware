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

package com.openexchange.ajax.request;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

public class CalendarRequest {

    protected ServerSession session;

    protected Date timestamp;

    protected TimeZone timeZone;

    protected void convertExternalToInternalUsersIfPossible(final CalendarObject appointmentObj, final Context ctx, final org.slf4j.Logger log){
		final Participant[] participants = appointmentObj.getParticipants();
		if (participants == null) {
            return;
        }

		final UserService us = ServerServiceRegistry.getInstance().getService(UserService.class);

		for(int pos = 0; pos < participants.length; pos++){
			final Participant part = participants[pos];
			if (part.getType() == Participant.EXTERNAL_USER){
				User foundUser;
				try {
					foundUser = us.searchUser(part.getEmailAddress(), ctx);
					if (foundUser == null) {
                        continue;
                    }
					participants[pos] = new UserParticipant(foundUser.getId());
				} catch (OXException e) {
				    log.debug("Couldn't resolve external participant \"{}\" to an internal user", part.getEmailAddress(), e); //...and continue doing this for the remaining users
				}
			}
		}

		appointmentObj.setParticipants(participants);
	}
}
