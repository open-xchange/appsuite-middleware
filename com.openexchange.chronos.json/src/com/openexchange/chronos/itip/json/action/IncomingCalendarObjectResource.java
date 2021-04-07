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

package com.openexchange.chronos.itip.json.action;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.arrays.Collections;

/**
 * 
 * {@link IncomingCalendarObjectResource}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class IncomingCalendarObjectResource extends DefaultCalendarObjectResource {

    private final IncomingSchedulingMail mail;

    /**
     * Initializes a new {@link IncomingCalendarObjectResource} for a single event.
     * 
     * @param event The event of the calendar object resource
     * @param mail The mail to load attacments from
     */
    public IncomingCalendarObjectResource(Event event, IncomingSchedulingMail mail) {
        super(event);
        this.mail = mail;
        loadAttachments();
    }

    /**
     * Initializes a new {@link IncomingCalendarObjectResource} from one specific and further events.
     * 
     * @param event One event of the calendar object resource
     * @param events Further events of the calendar object resource
     * @param mail The mail to load attacments from
     * 
     * @throws IllegalArgumentException If passed events do not represent a valid calendar object resource
     */
    public IncomingCalendarObjectResource(Event event, List<Event> events, IncomingSchedulingMail mail) {
        super(event, events);
        this.mail = mail;
        loadAttachments();
    }

    /**
     * Initializes a new {@link IncomingCalendarObjectResource}.
     * 
     * @param events The events of the calendar object resource
     * @param mail The mail to load attacments from
     * @throws IllegalArgumentException If passed events do not represent a valid calendar object resource
     */
    public IncomingCalendarObjectResource(List<Event> events, IncomingSchedulingMail mail) {
        super(events);
        this.mail = mail;
        loadAttachments();
    }

    /**
     * loadAttachments
     *
     */
    private void loadAttachments() {
        for (ListIterator<Event> iterator = events.listIterator(); iterator.hasNext();) {
            Event event = iterator.next();
            if (false == Collections.isNullOrEmpty(event.getAttachments())) {
                try {
                    Event copy = EventMapper.getInstance().copy(event, null, (EventField[]) null);
                    copy.setAttachments(prepareBinaryAttachments(copy.getAttachments()));
                    iterator.set(copy);
                } catch (OXException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Prepares attachments transmitted with the incoming message
     *
     * @param originalAttachments The attachments to find
     * @param in The object to get the (binary) attachments from
     * @return The filtered and existing attachments
     */
    private List<Attachment> prepareBinaryAttachments(List<Attachment> originalAttachments) {
        List<Attachment> attachments = new ArrayList<>(originalAttachments.size());
        for (Attachment attachment : originalAttachments) {
            Optional<Attachment> binaryAttachment = mail.getAttachment(attachment.getUri());
            if (binaryAttachment.isPresent()) {
                attachments.add(binaryAttachment.get());
            } else {
                attachments.add(attachment);
            }
        }
        return attachments;
    }

    @Override
    public Event getSeriesMaster() {
        Event firstEvent = events.get(0);
        return Strings.isNotEmpty(firstEvent.getRecurrenceRule()) ? firstEvent : null;
    }

    @Override
    public List<Event> getChangeExceptions() {
        List<Event> changeExceptions = new ArrayList<Event>(events.size());
        for (Event event : events) {
            if (null != event.getRecurrenceId()) {
                changeExceptions.add(event);
            }
        }
        return changeExceptions;
    }

}
