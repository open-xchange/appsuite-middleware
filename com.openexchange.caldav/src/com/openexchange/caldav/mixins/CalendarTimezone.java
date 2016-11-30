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

package com.openexchange.caldav.mixins;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.data.conversion.ical.SimpleMode;
import com.openexchange.data.conversion.ical.ZoneInfo;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.CommonFolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link CalendarTimezone}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class CalendarTimezone extends SingleXMLPropertyMixin {

    private final GroupwareCaldavFactory factory;
    private final CommonFolderCollection<?> collection;

    /**
     * Initializes a new {@link CalendarTimezone}.
     *
     * @param factory The CalDAV factory
     * @param collection The collection
     */
    public CalendarTimezone(GroupwareCaldavFactory factory, CommonFolderCollection<?> collection) {
        super(DAVProtocol.CAL_NS.getURI(), "calendar-timezone");
        this.factory = factory;
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        try {
            User user = collection.getOwner();
            if (null == user || Strings.isEmpty(user.getTimeZone())) {
                user = factory.getUser();
            }
            if (Strings.isNotEmpty(user.getTimeZone())) {
                ICalEmitter icalEmitter = factory.getIcalEmitter();
                ICalSession iCalSession = icalEmitter.createSession(new SimpleMode(ZoneInfo.OUTLOOK, null));
                if (icalEmitter.writeTimeZone(iCalSession, user.getTimeZone(), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>())) {
                    ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream(4096);
                    icalEmitter.writeSession(iCalSession, outputStream);
                    return new String(outputStream.toByteArray(), "UTF-8");
                }
            }
        } catch (OXException | UnsupportedEncodingException e) {
            org.slf4j.LoggerFactory.getLogger(CalendarTimezone.class).warn("Error serializing calendar-timezone", e);
        }
        return null;
    }

}
