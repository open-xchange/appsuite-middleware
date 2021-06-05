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

package com.openexchange.sessiond.impl.util;

import java.util.Collections;
import java.util.List;
import com.openexchange.sessiond.impl.container.SessionControl;

/**
 * {@link RotateShortResult} - The result of invoking {@code rotateShort()} from <code>com.openexchange.sessiond.impl.SessionData</code>
 * providing a listing of <b>non-transient</b> sessions for:
 * <ul>
 * <li>Sessions moved to long-term container</li>
 * <li>Timed out sessions</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class RotateShortResult {

    private final List<SessionControl> movedToLongTerm;
    private final List<SessionControl> removed;

    /**
     * Initializes a new {@link RotateShortResult}.
     *
     * @param movedToLongTerm A listing of sessions moved to long-term container or <code>null</code>
     * @param removed A listing of timed out sessions or <code>null</code>
     */
    public RotateShortResult(List<SessionControl> movedToLongTerm, List<SessionControl> removed) {
        super();
        this.movedToLongTerm = movedToLongTerm == null ? Collections.emptyList() : movedToLongTerm;
        this.removed = removed == null ? Collections.emptyList() : removed;
    }

    /**
     * Gets the <b>non-transient</b> sessions, which were moved to long-term container.
     *
     * @return The <b>non-transient</b>sessions, which were moved to long-term container
     */
    public List<SessionControl> getMovedToLongTerm() {
        return movedToLongTerm;
    }

    /**
     * Gets the <b>non-transient</b> sessions, which were removed
     *
     * @return The <b>non-transient</b> sessions, which were removed
     */
    public List<SessionControl> getRemoved() {
        return removed;
    }

    /**
     * Checks if both collections are empty.
     *
     * @return <code>true</code> if both are empty; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return movedToLongTerm.isEmpty() && removed.isEmpty();
    }
}
