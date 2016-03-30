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

package com.openexchange.sessiond;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.session.Session;

/**
 * {@link SessionMatcher}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface SessionMatcher {

    /**
     * The constant set indicating no flags.
     */
    public static final Set<Flag> NO_FLAGS = Collections.unmodifiableSet(EnumSet.noneOf(Flag.class));

    /**
     * The constant set indicating all flags.
     */
    public static final Set<Flag> ALL_FLAGS = Collections.unmodifiableSet(EnumSet.allOf(Flag.class));

    /**
     * Flag enumeration for session matcher.
     */
    public static enum Flag {
        /**
         * Whether to ignore sessions kept in long-term container.
         */
        IGNORE_LONG_TERM,
        /**
         * Whether to ignore sessions kept in distributed session storage.
         */
        IGNORE_SESSION_STORAGE,
    }

    /**
     * Gets the matcher's behavioral flags.
     * 
     * @return The flags or <code>null</code> for no flags at all
     * @see #NO_FLAGS
     * @see #ALL_FLAGS
     */
    Set<Flag> flags();

    /**
     * Checks whether passed session is accepted; meaning it fulfills matcher's condition.
     * 
     * @param session The session to check
     * @return <code>true</code> if accepted; otherwise <code>false</code> if not
     */
    boolean accepts(Session session);
}
