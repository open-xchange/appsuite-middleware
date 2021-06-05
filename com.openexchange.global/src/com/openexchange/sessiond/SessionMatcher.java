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

package com.openexchange.sessiond;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import com.openexchange.session.Session;

/**
 * {@link SessionMatcher}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SessionMatcher {

    /**
     * The constant set indicating no flags.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>: Might be an expensive operation since beside short-term container both - long-term container and session storage - are considered.
     * </div>
     */
    public static final Set<Flag> NO_FLAGS = Collections.unmodifiableSet(EnumSet.noneOf(Flag.class));

    /**
     * The constant set indicating only short-term container is considered.
     */
    public static final Set<Flag> ONLY_SHORT_TERM = Collections.unmodifiableSet(EnumSet.of(Flag.IGNORE_LONG_TERM, Flag.IGNORE_SESSION_STORAGE));

    /**
     * Flag enumeration for session matcher.
     */
    public static enum Flag {
        /**
         * Whether to ignore sessions kept in short-term container.
         */
        IGNORE_SHORT_TERM,
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
     * @see #ONLY_SHORT_TERM
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
