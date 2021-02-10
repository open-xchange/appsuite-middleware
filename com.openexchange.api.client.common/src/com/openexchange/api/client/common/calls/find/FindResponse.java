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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls.find;

import java.util.List;

/**
 * {@link FindResponse}
 *
 * @param <O> The type of the result objects
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class FindResponse<O> {

    private final int found;
    private final int start;
    private final int size;
    private final List<O> resultObjects;

    /**
     * Initializes a new {@link FindResponse}.
     *
     * @param found The number of found items
     * @param start The start of the pagination
     * @param size The page size
     * @param resultObjects The result objects
     */
    public FindResponse(int found, int start, int size, List<O> resultObjects) {
        this.found = found;
        this.start = start;
        this.size = size;
        this.resultObjects = resultObjects;
    }

    /**
     * Gets the number of found items
     *
     * @return The number of found items
     */
    public int getFound() {
        return found;
    }

    /**
     * Gets the start of the pagination
     *
     * @return The start of the pagination
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the size of the page
     *
     * @return The size of the page
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the result objects
     *
     * @return The list of result objects
     */
    public List<O> getResultObjects() {
        return resultObjects;
    }
}
