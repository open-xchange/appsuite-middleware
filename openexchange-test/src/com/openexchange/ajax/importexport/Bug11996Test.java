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

package com.openexchange.ajax.importexport;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.task.actions.DeleteRequest;
import com.openexchange.ajax.task.actions.GetRequest;
import com.openexchange.ajax.task.actions.GetResponse;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Bug11996Test extends AbstractAJAXSession {

    /**
     * Default constructor.
     * @param name test name
     */
    public Bug11996Test(final String name) {
        super(name);
    }

    public void testNotMatchingStatusAndPercentComplete() throws Throwable {
        final AJAXClient client = getClient();
        final int folderId = client.getValues().getPrivateTaskFolder();
        final ICalImportResponse iResponse = Executor.execute(client,
            new ICalImportRequest(folderId, ICAL));
        final ImportResult result = iResponse.getImports()[0];
        final int objectId = Integer.parseInt(result.getObjectId());
        try {
            final GetResponse gResponse = Executor.execute(client,
                new GetRequest(folderId, objectId));
            assertFalse(gResponse.hasError());
        } finally {
            Executor.execute(client, new DeleteRequest(folderId, objectId,
                result.getDate()));
        }
    }

    private static final String ICAL =
        "BEGIN:VCALENDAR\n" +
        "VERSION:2.0\n" +
        "PRODID:-//Minter Software//EdgeDesk 4.03 MIMEDIR//EN\n" +
        "METHOD:REQUEST\n" +
        "BEGIN:VTODO\n" +
        "STATUS:COMPLETED\n" +
        "ORGANIZER:MAILTO:david12@maxoxtest.com\n" +
            "ATTENDEE;RSVP=TRUE:MAILTO:david13@maxoxtest.com\n" +
        "DTSTART:20080116T000000Z\n" +
        "DUE:20080716T000100Z\n" +
        "TRANSP:OPAQUE\n" +
        "UID:167791216223452@visualmail4.webmail10\n" +
        "DTSTAMP:20080812T215000Z\n" +
        "SUMMARY:Completed task\n" +
        "DESCRIPTION:Only finished 75% but it's done!\n" +
        "PRIORITY:9\n" +
        "PERCENT-COMPLETE:75\n" +
        "BEGIN:VALARM\n" +
        "TRIGGER:-PT\n" +
        "ACTION:DISPLAY\n" +
        "DESCRIPTION:Reminder\n" +
        "END:VALARM\n" +
        "END:VTODO\n" +
        "END:VCALENDAR";
}
