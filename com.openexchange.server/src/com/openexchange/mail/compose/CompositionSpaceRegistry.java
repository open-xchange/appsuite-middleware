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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * {@link CompositionSpaceRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class CompositionSpaceRegistry {

    private final ConcurrentMap<String, CompositionSpace> spaces;

    /**
     * Initializes a new {@link CompositionSpaceRegistry}.
     */
    CompositionSpaceRegistry() {
        super();
        spaces = new ConcurrentHashMap<String, CompositionSpace>(8);
    }

    /**
     * Gets the composition spaces.
     *
     * @return The composition spaces
     */
    ConcurrentMap<String, CompositionSpace> getCompositionSpaces() {
        return spaces;
    }

    /**
     * Clears the composition spaces.
     *
     * @return The cleared composition spaces
     */
    List<CompositionSpace> clearCompositionSpaces() {
        List<CompositionSpace> l = new LinkedList<CompositionSpace>(spaces.values());
        spaces.clear();
        return l;
    }

    /**
     * Gets the composition space associated with given identifier.
     * <p>
     * A new composition space is created if absent.
     *
     * @param csid The composition space identifier
     * @return The associated composition space
     */
    CompositionSpace getCompositionSpace(String csid) {
        CompositionSpace space = spaces.get(csid);
        if (null == space) {
            CompositionSpace newSpace = new CompositionSpace(csid);
            space = spaces.putIfAbsent(csid, newSpace);
            if (null == space) {
                space = newSpace;
            }
        }
        return space;
    }

    /**
     * Optionally gets the composition space associated with given identifier.
     *
     * @param csid The composition space identifier
     * @return The associated composition space or <code>null</code>
     */
    CompositionSpace optCompositionSpace(String csid) {
        return spaces.get(csid);
    }

    /**
     * Removes the composition space associated with given identifier.
     *
     * @param csid The composition space identifier
     * @return The removed composition space or <code>null</code> if no such composition space was available
     */
    CompositionSpace removeCompositionSpace(String csid) {
        return spaces.remove(csid);
    }

}
