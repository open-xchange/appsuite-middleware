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

package com.openexchange.mail.compose.old;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.session.Session;


/**
 * {@link OldCompositionSpaceRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class OldCompositionSpaceRegistry {

    private final Map<String, OldCompositionSpace> spaces;

    /**
     * Initializes a new {@link OldCompositionSpaceRegistry}.
     */
    OldCompositionSpaceRegistry() {
        super();
        spaces = new HashMap<String, OldCompositionSpace>(8);
    }

    /**
     * Removes all composition spaces.
     *
     * @return The removed composition spaces
     */
    synchronized List<OldCompositionSpace> removeAllCompositionSpaces() {
        List<OldCompositionSpace> l = new LinkedList<OldCompositionSpace>(spaces.values());
        for (OldCompositionSpace space : l) {
            space.markInactive();
        }
        spaces.clear();
        return l;
    }

    /**
     * Gets the composition space associated with given identifier.
     * <p>
     * A new composition space is created if absent.
     *
     * @param csid The composition space identifier
     * @param session The associated session
     * @return The associated composition space
     */
    synchronized OldCompositionSpace getCompositionSpace(String csid, Session session) {
        OldCompositionSpace space = spaces.get(csid);
        if (null == space) {
            OldCompositionSpace newSpace = new OldCompositionSpace(csid, session);
            spaces.put(csid, newSpace);
            space = newSpace;
            space.markActive();
        }
        return space;
    }

    /**
     * Optionally gets the composition space associated with given identifier.
     *
     * @param csid The composition space identifier
     * @return The associated composition space or <code>null</code>
     */
    synchronized OldCompositionSpace optCompositionSpace(String csid) {
        return spaces.get(csid);
    }

    /**
     * Removes the composition space associated with given identifier.
     *
     * @param csid The composition space identifier
     * @return The removed composition space or <code>null</code> if no such composition space was available
     */
    synchronized OldCompositionSpace removeCompositionSpace(String csid) {
        OldCompositionSpace space = spaces.remove(csid);
        if (null != space) {
            space.markInactive();
        }
        return space;
    }

}
