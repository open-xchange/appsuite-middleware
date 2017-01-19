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
