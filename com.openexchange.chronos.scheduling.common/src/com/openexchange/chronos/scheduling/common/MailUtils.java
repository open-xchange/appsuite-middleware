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

package com.openexchange.chronos.scheduling.common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.Nullable;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.tools.mappings.Mapping;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.mime.utils.MimeMessageUtility;

/**
 * {@link MailUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.3
 */
public class MailUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailUtils.class);

    private static volatile String staticHostName;

    private static volatile UnknownHostException warnSpam;

    static {
        // Host name initialization
        try {
            staticHostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (final UnknownHostException e) {
            staticHostName = "localhost";
            warnSpam = e;
        }
    }

    /**
     * Initializes a new {@link MailUtils}.
     */
    private MailUtils() {
        super();
    }

    /**
     * Build the value for a header
     * 
     * @param uid The event unique identifier
     * @param timestamp <code>true></code> to add a timestamp to the value, <code>false</code> otherwise
     * @return The header value
     */
    public static String generateHeaderValue(String uid, boolean timestamp) {
        StringBuilder builder = new StringBuilder("<Appointment.");
        builder.append(uid);
        if (timestamp) {
            builder.append(".");
            builder.append(String.valueOf(System.currentTimeMillis()));
        }
        builder.append("@");
        builder.append("open-xchange.com");
        builder.append(">");
        return builder.toString();
    }

    /**
     * Performs {@link MimeMessage#saveChanges() saveChanges()} on specified message with sanitizing for a possibly corrupt/wrong Content-Type header.
     * <p>
     * Aligns <i>Message-Id</i> header to the host name.
     * 
     * @param hostnameService The {@link HostnameService} to get the host name from
     * @param mimeMessage The {@link MimeMessage} to save
     * @param context The identifier of the context
     * @param user The identifier of the user
     * @throws OXException In case service is missing or saving fails
     */
    public static void saveChangesSafe(@Nullable HostnameService hostnameService, MimeMessage mimeMessage, int context, int user) throws OXException {
        String hostName = null;
        if (null != hostnameService) {
            hostName = hostnameService.getHostname(user, context);
        }
        if (null == hostName) {
            hostName = getHostName();
        }
        MimeMessageUtility.saveChanges(mimeMessage, hostName, true);
    }

    private static String getHostName() {
        final String serverName = LogProperties.getLogProperty(LogProperties.Name.GRIZZLY_SERVER_NAME);
        if (null == serverName) {
            UnknownHostException warning = warnSpam;
            if (warning != null) {
                LOGGER.error("Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want!", warning);
            }
            return staticHostName;
        }
        return serverName;
    }

    private final static String SENT_BY = "SENT-BY";

    /**
     * Get the optional sent-by parameter from a calendar user
     *
     * @param calendarUser THe calendar user to get the sent-by from
     * @return The acting calendar user represented as URI string or <code>null</code> if not set
     */
    public static String getSentBy(CalendarUser calendarUser) {
        if (false == Attendee.class.isAssignableFrom(calendarUser.getClass())) {
            return null;
        }
        Attendee attendee = (Attendee) calendarUser;
        if (null == attendee.getExtendedParameters() || attendee.getExtendedParameters().isEmpty()) {
            return null;
        }

        for (ExtendedPropertyParameter parameter : attendee.getExtendedParameters()) {
            if (SENT_BY.equalsIgnoreCase(parameter.getName())) {
                return parameter.getValue();
            }
        }

        return null;
    }

    /**
     * Get the unique identifier of a specific event.
     *
     * @param events The events to search in
     * @return The unique identifier or <code>null</code> if not found
     * @throws OXException If mapping for field {@link EventField#UID} can't be found
     */
    public static String getSummary(List<Event> events) throws OXException {
        Object field = getEventField(events, EventField.SUMMARY);
        if (null != field) {
            return field.toString();
        }
        return null;
    }

    private static Object getEventField(List<Event> events, EventField field) throws OXException {
        Mapping<? extends Object, Event> mapping = EventMapper.getInstance().get(field);
        if (null != events) {
            for (Event e : events) {
                if (mapping.isSet(e)) {
                    return mapping.get(e);
                }
            }
        }
        return null;
    }
}
