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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAttributes;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.generators.ITipMailGeneratorFactory;
import com.openexchange.chronos.itip.sender.MailSenderService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;

/**
 * 
 * {@link ITipChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class CancelPerformer extends AbstractActionPerformer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CancelPerformer.class);


    public CancelPerformer(ITipIntegrationUtility util, MailSenderService sender, ITipMailGeneratorFactory generators) {
        super(util, sender, generators);
    }

    @Override
    public Collection<ITipAction> getSupportedActions() {
        return EnumSet.of(ITipAction.DELETE);
    }

    @Override
    public List<Event> perform(AJAXRequestData request, ITipAction action, ITipAnalysis analysis, CalendarSession session, ITipAttributes attributes) throws OXException {
        // Suppress iTip
        session.set(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.NONE);

        List<ITipChange> changes = analysis.getChanges();
        List<Event> deleted = new ArrayList<Event>();

        for (ITipChange change : changes) {
            Event event = change.getDeletedEvent();
            if (event == null) {
                continue;
            }
            // TODO: appointment.setNotification(true);
            if (change.getType() == ITipChange.Type.CREATE_DELETE_EXCEPTION) {
                if (null != change.getCurrentEvent()) {
                    event = change.getCurrentEvent();
                    event.setRecurrenceId(change.getDeletedEvent().getRecurrenceId());
                } else {
                    LOGGER.debug("Skipping the deletion of a single occurrence. Corresponding event not found.");
                    session.addWarning(CalendarExceptionCodes.EVENT_NOT_FOUND.create("null"));
                    continue;
                }
            }
            deleted.add(event);
            util.deleteEvent(event, session, new Date(Long.MAX_VALUE));
        }
        return deleted;
    }
}
