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

package com.openexchange.file.storage.webdav;

import java.net.URI;
import java.util.ArrayList;
import com.google.common.base.Splitter;
import com.google.common.net.UrlEscapers;
import com.openexchange.java.Strings;

/**
 * {@link WebDAVPath}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.4
 */
public class WebDAVPath {

    private final ArrayList<String> segments;
    private final boolean isCollection;

    /**
     * Initializes a new {@link WebDAVPath} from the given href string.
     *
     * @param href The href address string to construct the WebDAV path from
     * @throws IllegalArgumentException if the given string violates RFC 2396
     */
    public WebDAVPath(String href) {
        this(URI.create(href));
    }

    /**
     * Initializes a new {@link WebDAVPath}.
     *
     * @param uri The URI to construct the WebDAV path from
     */
    public WebDAVPath(URI uri) {
        super();
        String decodedPath = uri.getPath();
        this.segments = split(decodedPath);
        this.isCollection = endsWithSlash(decodedPath);
    }

    protected WebDAVPath(ArrayList<String> segments, boolean isCollection) {
        super();
        this.segments = segments;
        this.isCollection = isCollection;
    }

    /**
     * Gets the name of the WebDAV path, i.e. the last path segment.
     *
     * @return The name, or an empty string for a root path
     */
    public String getName() {
        return segments.isEmpty() ? "" : segments.get(segments.size() - 1);
    }

    /**
     * Gets the parent WebDAV path of this path.
     *
     * @return The parent WebDAV path
     * @throws UnsupportedOperationException when invoked on the root path
     */
    public WebDAVPath getParent() {
        if (segments.isEmpty()) {
            throw new UnsupportedOperationException("no parent for root collection");
        }
        return new WebDAVPath(new ArrayList<String>(segments.subList(0, segments.size() - 1)), true);
    }

    /**
     * Appends an additional path segment to this WebDAV path.
     *
     * @param segment The segment to append
     * @param asCollection <code>true</code> to make the resulting WebDAV path still reference a collection, <code>false</code>, otherwise
     * @return The enhanced WebDAV path
     * @throws UnsupportedOperationException when invoked on a non-collection path
     */
    public WebDAVPath append(String segment, boolean asCollection) {
        if (false == isCollection()) {
            throw new UnsupportedOperationException("append only available for collections");
        }
        ArrayList<String> extendedSegements = new ArrayList<String>(segments);
        extendedSegements.add(segment);
        return new WebDAVPath(extendedSegements, asCollection);
    }

    /**
     * Gets the {@link URI} representation of this WebDAV path.
     *
     * @return The URI
     */
    public URI toURI() {
        return URI.create(toString());
    }

    /**
     * Gets a value indicating whether this WebDAV path references a collection or not.
     *
     * @return <code>true</code> if this WebDAV path denotes a collection, <code>false</code>, otherwise
     */
    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isCollection ? 1231 : 1237);
        result = prime * result + ((segments == null) ? 0 : segments.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WebDAVPath other = (WebDAVPath) obj;
        if (isCollection != other.isCollection)
            return false;
        if (segments == null) {
            if (other.segments != null)
                return false;
        } else if (!segments.equals(other.segments))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String segment : segments) {
            String escapedSegment = UrlEscapers.urlPathSegmentEscaper().escape(segment);
            stringBuilder.append('/').append(escapedSegment);
        }
        if (isCollection) {
            stringBuilder.append('/');
        }
        return stringBuilder.toString();
    }


    private static ArrayList<String> split(String decodedPath) {
        ArrayList<String> segments = new ArrayList<String>();
        if (Strings.isNotEmpty(decodedPath)) {
            for (String segment : Splitter.on('/').omitEmptyStrings().split(decodedPath)) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private static boolean endsWithSlash(String value) {
        return '/' == value.charAt(value.length() - 1);
    }

}
