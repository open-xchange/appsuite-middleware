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
