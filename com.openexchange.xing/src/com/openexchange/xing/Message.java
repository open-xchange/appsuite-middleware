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
                    } catch (ParseException e) {
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
