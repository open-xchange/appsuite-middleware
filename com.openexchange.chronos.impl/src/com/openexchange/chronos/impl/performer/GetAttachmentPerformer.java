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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import java.util.List;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link GetAttachmentPerformer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GetAttachmentPerformer extends AbstractQueryPerformer {

    /**
     * Initialises a new {@link GetAttachmentPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public GetAttachmentPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the get attachment operation.
     *
     * @param folderId The identifier of the parent folder the event is located in
     * @param eventId The {@link Event} identifier
     * @param managedId The managed identifier of the {@link Attachment}
     * @return The {@link IFileHolder} of the attachment
     */
    public IFileHolder performGetAttachment(String folderId, String eventId, int managedId) throws OXException {
        /*
         * load event data & check permissions
         */
        CalendarFolder folder = getFolder(session, folderId, false);
        EventField[] fields = getFields(session, EventField.ORGANIZER, EventField.ATTENDEES, EventField.ATTACHMENTS);
        Event event = storage.getEventStorage().loadEvent(eventId, fields);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        event = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), event, fields);
        event = Check.eventIsVisible(folder, event);
        Check.eventIsInFolder(event, folder);

        // Search for the attachment with the specified managed id
        List<Attachment> attachments = event.getAttachments();
        if (attachments == null) {
            throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(managedId, eventId, folder.getId());
        }
        for (Attachment attachment : attachments) {
            if (attachment.getManagedId() == managedId) {
                return new FileHolder(storage.getAttachmentStorage().loadAttachmentData(managedId), attachment.getSize(), attachment.getFormatType(), attachment.getFilename());
            }
        }
        throw CalendarExceptionCodes.ATTACHMENT_NOT_FOUND.create(managedId, eventId, folder.getId());
    }

}
