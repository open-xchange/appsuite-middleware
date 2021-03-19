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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl.scheduling;

import static com.openexchange.chronos.common.CalendarUtils.isOpaqueTransparency;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;

/**
 * {@link SchedulingUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class SchedulingUtils {

    private SchedulingUtils() {
        super();
    }

    /**
     * Filters the updated attachments for new items and replaces them with their binary representation
     * 
     * @param originalAttachments The original attachments
     * @param updatedAttachments The updated attachments
     * @param message The message to get the (binary) attachments from
     * @return The filtered attachments
     */
    public static List<Attachment> filterAttachments(List<Attachment> originalAttachments, List<Attachment> updatedAttachments, IncomingSchedulingMessage message) {
        SimpleCollectionUpdate<Attachment> attachmentUpdates = CalendarUtils.getAttachmentUpdates(originalAttachments, updatedAttachments);
        List<Attachment> binaryAttachments = getBinaryAttachments(attachmentUpdates.getAddedItems(), message);
        List<Attachment> newAttachments = new ArrayList<>(updatedAttachments.size());
        for (Attachment attachment : updatedAttachments) {
            Attachment binaryAttachment = CalendarUtils.findByUri(binaryAttachments, attachment);
            if (null == binaryAttachment) {
                newAttachments.add(attachment);
            } else {
                newAttachments.add(binaryAttachment);
            }
        }
        return newAttachments;
    }

    /**
     * Get attachments transmitted with the incoming object
     *
     * @param attachments The attachments to find
     * @param message The message to get the (binary) attachments from
     * @return The filtered and existing attachments
     */
    public static List<Attachment> getBinaryAttachments(List<Attachment> attachments, IncomingSchedulingMessage message) {
        List<Attachment> binaryAttachments = new ArrayList<>(attachments.size());
        for (Attachment attachment : attachments) {
            Optional<Attachment> binaryAttachment = message.getSchedulingObject().getAttachment(attachment.getUri());
            if (binaryAttachment.isPresent()) {
                binaryAttachments.add(binaryAttachment.get());
            }
        }
        return binaryAttachments;
    }

    /**
     * Gets a value indicating whether conflict checks should take place along with the update or not.
     * 
     * @param session {@link CalendarSession} to evaluate the {@link CalendarParameters#PARAMETER_CHECK_CONFLICTS} from
     * @param eventUpdate The event update to evaluate
     * @return <code>true</code> if conflict checks should take place, <code>false</code>, otherwise
     * @throws OXException In case of error
     */
    public static boolean needsConflictCheck(CalendarSession session, EventUpdate eventUpdate) throws OXException {
        Boolean checkConflicts = session.get(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.class);
        if (null != checkConflicts && Boolean.FALSE.equals(checkConflicts)) {
            return false;
        }
        if (Utils.coversDifferentTimePeriod(eventUpdate.getOriginal(), eventUpdate.getUpdate())) {
            /*
             * (re-)check conflicts if event appears in a different time period
             */
            return true;
        }
        if (eventUpdate.getUpdatedFields().contains(EventField.TRANSP) && false == isOpaqueTransparency(eventUpdate.getOriginal())) {
            /*
             * (re-)check conflicts if transparency is now opaque
             */
            return true;
        }
        if (0 < eventUpdate.getAttendeeUpdates().getAddedItems().size()) {
            /*
             * (re-)check conflicts if there are new attendees
             */
            return true;
        }
        return false;
    }

    /**
     * Generates an attachment update respecting transmitted attachments from the scheduling message
     *
     * @param delegatee The collection update to delegate to
     * @param message The message to get the attachments binaries from
     * @return A collection containing newly added binary attachments
     */
    public static SimpleCollectionUpdate<Attachment> getAttachmentUpdates(SimpleCollectionUpdate<Attachment> delegatee, IncomingSchedulingMessage message) {
        return new ComositingAttachmentCollectionUpdate(delegatee, message);
    }

    /**
     * {@link ComositingAttachmentCollectionUpdate} - Get the new added attachments from a scheduling object
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v8.0.0
     */
    private static class ComositingAttachmentCollectionUpdate implements SimpleCollectionUpdate<Attachment> {

        private final SimpleCollectionUpdate<Attachment> delegatee;
        private final List<Attachment> addedItems;

        /**
         * Initializes a new {@link SimpleCollectionUpdateImplementation}.
         * 
         * @param delegatee The collection update to delegate to
         * @param message The message to get the attachments binaries from
         */
        ComositingAttachmentCollectionUpdate(SimpleCollectionUpdate<Attachment> delegatee, IncomingSchedulingMessage message) {
            this.delegatee = delegatee;
            this.addedItems = getBinaryAttachments(delegatee.getAddedItems(), message);
        }

        @Override
        public List<Attachment> getAddedItems() {
            return addedItems;
        }

        @Override
        public List<Attachment> getRemovedItems() {
            return delegatee.getRemovedItems();
        }

        @Override
        public boolean isEmpty() {
            return 0 == addedItems.size() && 0 == getRemovedItems().size();
        }
    }

}
