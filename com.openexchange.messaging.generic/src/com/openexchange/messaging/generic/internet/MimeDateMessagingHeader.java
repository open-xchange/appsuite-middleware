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
            } catch (ParseException e) {
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
