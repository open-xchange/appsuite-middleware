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

package com.openexchange.chronos.itip.generators;

import java.util.Locale;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.itip.ContextSensitiveMessages;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.i18n.tools.StringHelper;

/**
 * {@link ParticipantHelper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ParticipantHelper {

    private final Locale recipientLocale;

    public ParticipantHelper(final Locale recipientLocale) {
        super();
        this.recipientLocale = recipientLocale;
    }

    public String participantLine(final NotificationParticipant participant) {
        // TODO: Same width
        final String sConfirmStatus;
        ParticipationStatus status = participant.getConfirmStatus();
        if (status == null) {
            sConfirmStatus = StringHelper.valueOf(recipientLocale).getString(Messages.WAITING);
        } else if (status.equals(ParticipationStatus.ACCEPTED)) {
            sConfirmStatus = ContextSensitiveMessages.accepted(recipientLocale, ContextSensitiveMessages.Context.ADJECTIVE);
        } else if (status.equals(ParticipationStatus.DECLINED)) {
            sConfirmStatus = ContextSensitiveMessages.declined(recipientLocale, ContextSensitiveMessages.Context.ADJECTIVE);
        } else if (status.equals(ParticipationStatus.TENTATIVE)) {
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
}
