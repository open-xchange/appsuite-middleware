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

package com.openexchange.xing;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;

/**
 * {@link Message} - Represent a single XING message.
 * <p>
 *
 * <pre>
 * {
 *   "id": "104401_09361f",
 *   "created_at": "2012-04-04T16:30:00Z",
 *   "content": "Yes of course!",
 *   "read": false,
 *   "sender": {
 *   "id": "146234_dc52a7",
 *   "display_name": "Hans"
 * }
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Message {

    private static final DateFormat DATE_FORMAT;
    static {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss'Z'", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_FORMAT = df;
    }

    /**
     * Gets the messaging date format.
     *
     * @return The date format.
     */
    public static DateFormat getDateFormat() {
        return DATE_FORMAT;
    }

    private final String id;
    private final Date createdAt;
    private final String content;
    private final boolean read;
    private User sender;

    /**
     * Initializes a new {@link Message}.
     *
     * @throws XingException If initialization fails
     */
    public Message(final JSONObject messageInformation) throws XingException {
        super();
        this.id = messageInformation.optString("id", null);
        {
            final String sdate = messageInformation.optString("created_at", null);
            if (null == sdate) {
                createdAt = null;
            } else {
                synchronized (DATE_FORMAT) {
                    Date d;
                    try {
                        d = DATE_FORMAT.parse(sdate);
                    } catch (final ParseException e) {
                        d = null;
                    }
                    this.createdAt = d;
                }
            }
        }
        this.content = messageInformation.optString("content", null);
        this.read = messageInformation.optBoolean("read", false);
        final JSONObject userInformation = messageInformation.optJSONObject("sender");
        if (null != userInformation) {
            this.sender = new User(userInformation);
        }
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the created-at time stamp.
     *
     * @return The created-at time stamp
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the content
     *
     * @return The content
     */
    public String getContent() {
        return content;
    }

    /**
     * Gets the read
     *
     * @return The read
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Gets the sender
     *
     * @return The sender
     */
    public User getSender() {
        return sender;
    }

}
