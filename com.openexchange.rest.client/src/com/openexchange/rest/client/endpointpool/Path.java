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

package com.openexchange.rest.client.endpointpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import com.openexchange.java.Strings;

/**
 * {@link Path}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class Path implements Iterable<String> {

    private final List<String> segments;

    /**
     * Initializes a new {@link Path}.
     *
     * @param segments The segments that build-up the path
     * @throws IllegalArgumentException If <code>segments</code> argument is empty or an illegal segment is contained
     */
    public Path(String... segments) {
        super();
        if (null == segments || 0 == segments.length) {
            throw new IllegalArgumentException("segments must not be empty");
        }

        List<String> l = new ArrayList<String>(segments.length);
        for (String segment : segments) {
            l.add(checkSegment(segment));
        }
        this.segments = l;
    }

    /**
     * Initializes a new {@link Path}.
     *
     * @param segments The segments that build-up the path
     * @throws IllegalArgumentException If <code>segments</code> argument is empty or an illegal segment is contained
     */
    public Path(Collection<String> segments) {
        super();
        if (null == segments || segments.isEmpty()) {
            throw new IllegalArgumentException("segments must not be empty");
        }

        List<String> l = new ArrayList<String>(segments.size());
        for (String segment : segments) {
            l.add(checkSegment(segment));
        }
        this.segments = l;
    }

    private String checkSegment(String segment) {
        if (Strings.isEmpty(segment)) {
            throw new IllegalArgumentException("A segment must not be empty");
        }
        if (segment.indexOf('/') >= 0) {
            throw new IllegalArgumentException("A segment must not contain the path delimiter: " + segment);
        }
        return segment.trim();
    }

    /**
     * Gets the segments.
     *
     * @return The segments
     */
    public List<String> getSegments() {
        return segments;
    }

    @Override
    public Iterator<String> iterator() {
        return segments.iterator();
    }

    @Override
    public String toString() {
        return segments.toString();
    }

}
