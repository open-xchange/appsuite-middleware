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

package com.openexchange.chronos.itip.performers;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipAttributes;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class MailPerformer extends AbstractActionPerformer {

    public MailPerformer(ITipIntegrationUtility util, MailSenderService sender, ITipMailGeneratorFactory generators) {
        super(util, sender, generators);
    }

    @Override
    public Collection<ITipAction> getSupportedActions() {
        return EnumSet.of(ITipAction.DECLINECOUNTER, ITipAction.REFRESH, ITipAction.SEND_APPOINTMENT);
    }

    @Override
    public List<Event> perform(AJAXRequestData request, ITipAction action, ITipAnalysis analysis, CalendarSession session, ITipAttributes attributes) throws OXException {
        List<ITipChange> changes = analysis.getChanges();
        int owner = getOwner(session, analysis);
        for (ITipChange change : changes) {
            Event event = change.getNewEvent();
            // TODO: appointment.setNotification(true);
            writeMail(request, action, null, event, session, owner);
        }

        List<ITipAnnotation> annotations = analysis.getAnnotations();
        for (ITipAnnotation annotation : annotations) {
            Event event = annotation.getEvent();
            // TODO: appointment.setNotification(true);
            writeMail(request, action, null, event, session, owner);
        }
        return Collections.emptyList();
    }

    private int getOwner(CalendarSession session, ITipAnalysis analysis) {
        if (analysis.getMessage().getOwner() > 0) {
            return analysis.getMessage().getOwner();
        }
        return session.getUserId();
    }

}
