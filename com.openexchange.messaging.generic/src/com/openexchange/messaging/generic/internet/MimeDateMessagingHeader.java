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

package com.openexchange.messaging.generic.internet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.DateMessagingHeader;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.generic.Utility;

/**
 * {@link MimeDateMessagingHeader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class MimeDateMessagingHeader implements DateMessagingHeader {

    private final String name;

    private final long time;

    private final String value;

    /**
     * Initializes a new {@link MimeDateMessagingHeader}.
     *
     * @param name The name
     * @param time The number of milliseconds since January 1, 1970, 00:00:00 GMT
     */
    public MimeDateMessagingHeader(final String name, final long time) {
        super();
        this.name = name;
        this.time = time;
        final SimpleDateFormat mdf = Utility.getDefaultMailDateFormat();
        synchronized (mdf) {
            value = mdf.format(new Date(time));
        }
    }

    /**
     * Initializes a new {@link MimeDateMessagingHeader}.
     *
     * @param name The name
     * @param formattedDate The formatted date as per RFC822 pattern
     *            (&quot;EEE,&nbsp;d&nbsp;MMM&nbsp;yyyy&nbsp;HH:mm:ss&nbsp;'XXXXX'&nbsp;(z)&quot;)
     * @throws OXException If parsing specified formatted date fails
     */
    public MimeDateMessagingHeader(final String name, final String formattedDate) throws OXException {
        super();
        this.name = name;
        value = formattedDate;
        final SimpleDateFormat mdf = Utility.getDefaultMailDateFormat();
        synchronized (mdf) {
            try {
                final Date parsedDate = mdf.parse(formattedDate);
                time = parsedDate.getTime();
            } catch (final ParseException e) {
                throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    /**
     * Initializes a new {@link MimeDateMessagingHeader}.
     *
     * @param name The name
     * @param date The date
     */
    public MimeDateMessagingHeader(final String name, final Date date) {
        super();
        this.name = name;
        time = date.getTime();
        final SimpleDateFormat mdf = Utility.getDefaultMailDateFormat();
        synchronized (mdf) {
            value = mdf.format(date);
        }
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.DATE;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

}
