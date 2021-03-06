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

package com.openexchange.websockets.grizzly.remote.portable;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.Strings;
import com.openexchange.websockets.grizzly.GrizzlyWebSocketUtils;
import com.openexchange.websockets.grizzly.impl.DefaultGrizzlyWebSocketApplication;

/**
 * {@link PortableMessageDistributor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class PortableMessageDistributor extends AbstractCustomPortable implements Callable<Void> {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PortableMessageDistributor.class);
    private static final Logger WS_LOGGER = org.slf4j.LoggerFactory.getLogger("WEBSOCKET");

    private static final String DELIM = "?==?";
    private static final Pattern P_DELIM = Pattern.compile("\\?==\\?");
    private static final Pattern P_ESCAPE = Pattern.compile("\\?=3D=3D\\?");

    /**
     * Joins specified messages
     *
     * @param messages The messages
     * @return The joined message
     */
    private static String joinMessages(Collection<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        int size = messages.size();
        if (1 == size) {
            return escape(messages.iterator().next());
        }

        Iterator<String> it = messages.iterator();
        StringBuilder sb = new StringBuilder(size * 32);
        sb.append(escape(it.next()));
        for (int i = size - 1; i-- > 0;) {
            sb.append(DELIM).append(escape(it.next()));
        }
        return sb.toString();
    }

    /**
     * Splits joined message
     *
     * @param message The joined message
     * @return The split messages
     */
    private static String[] splitMessage(String message) {
        if (Strings.isEmpty(message)) {
            return null;
        }

        if (message.indexOf(DELIM) < 0) {
            return new String[] { unescape(message) };
        }

        String[] split = P_DELIM.split(message);
        for (int i = split.length; i-- > 0;) {
            split[i] = unescape(split[i]);
        }
        return split;
    }

    private static String escape(String toEscape) {
        if (toEscape.indexOf(DELIM) < 0) {
            return toEscape;
        }
        return P_DELIM.matcher(toEscape).replaceAll("?=3D=3D?");
    }

    private static String unescape(String toUnescape) {
        if (toUnescape.indexOf("?=3D=3D?") < 0) {
            return toUnescape;
        }
        return P_ESCAPE.matcher(toUnescape).replaceAll(DELIM);
    }

    // --------------------------------------------------------------------------------------------------------------------------------------

    /** The unique portable class ID of the {@link PortableMessageDistributor}: <code>600</code> */
    public static final int CLASS_ID = 600;

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CTX_ID = "contextId";
    private static final String FIELD_MESSAGE = "message";
    private static final String FIELD_FILTER = "filter";
    private static final String FIELD_ASYNC = "async";

    private int userId;
    private int contextId;
    private String message;
    private String filter;

    /**
     * Initializes a new {@link PortableMessageDistributor}.
     */
    public PortableMessageDistributor() {
        super();
    }

    /**
     * Initializes a new {@link PortableMessageDistributor}.
     *
     * @param messages The text messages to distribute
     * @param filter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public PortableMessageDistributor(Collection<String> messages, String filter, int userId, int contextId) {
        this(joinMessages(messages), filter, userId, contextId);
    }

    /**
     * Initializes a new {@link PortableMessageDistributor}.
     *
     * @param message The text message to distribute
     * @param filter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public PortableMessageDistributor(String message, String filter, int userId, int contextId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.message = message;
        this.filter = filter;
    }

    @Override
    public Void call() throws Exception {
        DefaultGrizzlyWebSocketApplication application = DefaultGrizzlyWebSocketApplication.getGrizzlyWebSocketApplication();
        if (null == application) {
            WS_LOGGER.warn("Found no Web Socket application on cluster member {}", DefaultGrizzlyWebSocketApplication.getLocalHost());
            return null;
        }

        String[] messages = splitMessage(message);
        if (messages == null || messages.length == 0) {
            WS_LOGGER.debug("Received no messages on cluster member {} for user {} in context {}", DefaultGrizzlyWebSocketApplication.getLocalHost(), I(userId), I(contextId));
            return null;
        }

        WS_LOGGER.debug("Received {} message(s) on cluster member {} for user {} in context {}", I(messages.length), DefaultGrizzlyWebSocketApplication.getLocalHost(), I(userId), I(contextId));

        try {
            for (String msg : messages) {
                application.sendToUserAsync(msg, null, filter, true, userId, contextId);
                WS_LOGGER.debug("Transmitted message \"{}\" to Web Socket application using path filter \"{}\" to user {} in context {}", GrizzlyWebSocketUtils.abbreviateMessageArg(msg), filter, I(userId), I(contextId));
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to handle {} message(s) on cluster member {} for user {} in context {}", I(messages.length), DefaultGrizzlyWebSocketApplication.getLocalHost(), I(userId), I(contextId), e);
            throw e;
        }
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeInt(FIELD_CTX_ID, contextId);
        writer.writeInt(FIELD_USER_ID, userId);
        writer.writeUTF(FIELD_MESSAGE, message);
        writer.writeUTF(FIELD_FILTER, filter);
        writer.writeBoolean(FIELD_ASYNC, true);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        this.contextId = reader.readInt(FIELD_CTX_ID);
        this.userId = reader.readInt(FIELD_USER_ID);
        this.message = reader.readUTF(FIELD_MESSAGE);
        this.filter = reader.readUTF(FIELD_FILTER);
        reader.readBoolean(FIELD_ASYNC);
    }

}
