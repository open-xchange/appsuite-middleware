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

package com.openexchange.mail.compose;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.session.Session;


/**
 * {@link CompositionSpaceRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class CompositionSpaceRegistry {

    private final Map<String, CompositionSpace> spaces;

    /**
     * Initializes a new {@link CompositionSpaceRegistry}.
     */
    CompositionSpaceRegistry() {
        super();
        spaces = new HashMap<String, CompositionSpace>(8);
    }

    /**
     * Removes all composition spaces.
     *
     * @return The removed composition spaces
     */
    synchronized List<CompositionSpace> removeAllCompositionSpaces() {
        List<CompositionSpace> l = new LinkedList<CompositionSpace>(spaces.values());
        for (CompositionSpace space : l) {
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
    synchronized CompositionSpace getCompositionSpace(String csid, Session session) {
        CompositionSpace space = spaces.get(csid);
        if (null == space) {
            CompositionSpace newSpace = new CompositionSpace(csid, session);
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
    synchronized CompositionSpace optCompositionSpace(String csid) {
        return spaces.get(csid);
    }

    /**
     * Removes the composition space associated with given identifier.
     *
     * @param csid The composition space identifier
     * @return The removed composition space or <code>null</code> if no such composition space was available
     */
    synchronized CompositionSpace removeCompositionSpace(String csid) {
        CompositionSpace space = spaces.remove(csid);
        if (null != space) {
            space.markInactive();
        }
        return space;
    }

}
