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

package com.openexchange.mail;

import java.util.HashMap;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link FlaggingMode} - Specifies how color labels and special \Flagged system flag are connected (or not).
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public enum FlaggingMode {

    /**
     * Only color flags are available. The special \Flagged system flag is not touched.
     */
    COLOR_ONLY("colorOnly"),
    /**
     * Only special \Flagged system flag is used. Color labels are not published.
     */
    FLAGGED_ONLY("flaggedOnly"),
    /**
     * Both - color flags and special \Flagged system flag - are available and set independently.
     */
    FLAGGED_AND_COLOR("flaggedAndColor"),
    /**
     * Both - color flags and special \Flagged system flag - are available. A certain color label is linked with the \Flagged system flag.
     * <p>
     * That is to add a color to colorless flagged mails and to add flagged to unflagged but colored mails.
     */
    FLAGGED_IMPLICIT("flaggedImplicit");

    private final String name;

    private FlaggingMode(String name) {
        this.name = name;
    }

    /**
     * Gets the name for this flagging mode.
     *
     * @return The name
     */
    public String getName(){
        return this.name;
    }


    // ------------------------------------------------- Helpers ------------------------------------------------

    private static final Map<String, FlaggingMode> MODES;
    static {
        FlaggingMode[] modes = FlaggingMode.values();
        Map<String, FlaggingMode> m = new HashMap<>(modes.length);
        for (FlaggingMode mode : modes) {
            m.put(mode.name, mode);
            m.put(Strings.asciiLowerCase(mode.name), mode);
        }
        MODES = ImmutableMap.copyOf(m);
    }

    /**
     * Resolves the specified name to a flagging mode.
     *
     * @param name The name to resolve
     * @return The resolved flagging mode or {@link #COLOR_ONLY} in case name does not match any
     */
    public static FlaggingMode getModeByName(String name) {
        return getModeByName(name, FlaggingMode.COLOR_ONLY);
    }

    /**
     * Resolves the specified name to a flagging mode.
     *
     * @param name The name to resolve
     * @param def The default mode to return if name cannot be mapped
     * @return The resolved flagging mode or <code>def</code> in case name does not match any
     */
    public static FlaggingMode getModeByName(String name, FlaggingMode def) {
        FlaggingMode mode = null == name ? null : MODES.get(name);
        return null == mode ? def : mode;
    }

    private final static String FLAGGING_COLOR_PROPERTY = "com.openexchange.mail.flagging.color";

    /**
     * Retrieves the configured color for colorless flagged mails.
     *
     * @param session The session
     * @return The numeric identifier for configured color
     * @throws NullPointerException If specified session is <code>null</code>
     */
    public static final int getFlaggingColor(Session session) {
        ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        return getFlaggingColor(session, factory);
    }

    /**
     * Retrieves the configured color for colorless flagged mails.
     *
     * @param session The session
     * @param factory The factory to use
     * @return The numeric identifier for configured color
     * @throws NullPointerException If specified session is <code>null</code>
     */
    public static final int getFlaggingColor(Session session, ConfigViewFactory factory) {
        int def = 1;
        if (factory != null) {
            try {
                ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                Integer color = view.opt(FLAGGING_COLOR_PROPERTY, Integer.class, Integer.valueOf(def));
                return null == color ? def : color.intValue();
            } catch (OXException e) {
                // Fallback to default
            }
        }
        return def;
    }

    private final static String FLAGGING_MODE_PROPERTY = "com.openexchange.mail.flagging.mode";

    /**
     * Retrieves the configured flagging mode for the user. Falls back to {@link #COLOR_ONLY} in case an error occurs.
     *
     * @param session The session
     * @return The configured flagging mode for the user
     * @throws NullPointerException If specified session is <code>null</code>
     */
    public static FlaggingMode getFlaggingMode(Session session) {
        ConfigViewFactory factory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
        return getFlaggingMode(session, factory);
    }

    /**
     * Retrieves the configured flagging mode for the user. Falls back to {@link #COLOR_ONLY} in case an error occurs.
     *
     * @param session The session
     * @param factory The factory to use
     * @return The configured flagging mode for the user
     * @throws NullPointerException If specified session is <code>null</code>
     */
    public static FlaggingMode getFlaggingMode(Session session, ConfigViewFactory factory) {
        if (factory != null) {
            try {
                ConfigView view = factory.getView(session.getUserId(), session.getContextId());
                String modeName = view.opt(FLAGGING_MODE_PROPERTY, String.class, null);
                return null == modeName ? FlaggingMode.COLOR_ONLY : FlaggingMode.getModeByName(modeName);
            } catch (OXException e) {
                // fall back to default
            }
        }
        return FlaggingMode.COLOR_ONLY;
    }

}
