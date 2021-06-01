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

package com.openexchange.chronos.scheduling.changes.impl;

import java.util.Locale;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.itip.ContextSensitiveMessages;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;

/**
 * {@link ParticipantHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class ParticipantHelper {

    private final Locale recipientLocale;

    public ParticipantHelper(Locale recipientLocale) {
        super();
        this.recipientLocale = recipientLocale;
    }

    public String participantLine(NotificationParticipant participant) {
        final String sConfirmStatus;
        ParticipationStatus status = participant.getConfirmStatus();
        if (status == null) {
            sConfirmStatus = StringHelper.valueOf(recipientLocale).getString(Messages.WAITING);
        } else if (status.matches(ParticipationStatus.ACCEPTED)) {
            sConfirmStatus = ContextSensitiveMessages.accepted(recipientLocale, ContextSensitiveMessages.Context.ADJECTIVE);
        } else if (status.matches(ParticipationStatus.DECLINED)) {
            sConfirmStatus = ContextSensitiveMessages.declined(recipientLocale, ContextSensitiveMessages.Context.ADJECTIVE);
        } else if (status.matches(ParticipationStatus.TENTATIVE)) {
            sConfirmStatus = ContextSensitiveMessages.tentative(recipientLocale, ContextSensitiveMessages.Context.ADJECTIVE);
        } else {
            sConfirmStatus = StringHelper.valueOf(recipientLocale).getString(Messages.WAITING);
        }
        final String comment = participant.getComment();
        if (com.openexchange.java.Strings.isEmpty(comment)) {
            return new StringBuilder(24).append(participant.getDisplayName()).append(" (").append(sConfirmStatus).append(')').toString();
        }
        return new StringBuilder(24).append(participant.getDisplayName()).append(" (").append(sConfirmStatus).append(") (\"").append(comment).append("\")").toString();
    }

    public String conferenceLine(NotificationConference conference) {
        if (Strings.isEmpty(conference.getLabel())) {
            return conference.getUri();
        }
        return conference.getLabel() + ": " + conference.getUri();
    }

}
