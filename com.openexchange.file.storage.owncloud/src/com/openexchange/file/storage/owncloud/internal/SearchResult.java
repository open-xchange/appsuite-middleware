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
package com.openexchange.file.storage.owncloud.internal;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * {@link SearchResult}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@XmlRootElement(name="multistatus", namespace = "DAV:")
public class SearchResult {

    @XmlElement(name = "response", namespace = "DAV:")
    List<Response> responses;

    public List<Response> getFiles() {
       return responses;
    }

    /**
     *
     * {@link Response}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.4
     */
    @XmlRootElement(name="response", namespace = "DAV:")
    public static class Response {
        @XmlElement(name = "href", namespace = "DAV:")
        String href;
        @XmlElement(name = "propstat", namespace = "DAV:")
        List<Propstat> propstats;

        /**
         * Gets the href
         *
         * @return The href
         */
        public String getHref() {
            return href;
        }

        /**
         * Gets the propstat
         *
         * @return The propstat
         */
        public Optional<Propstat> getPropstat() {
            return propstats.stream().filter((prop) -> prop.getStatus().contains("200 OK")).filter((prop) -> prop.getFile().isPresent()).findFirst();
        }

    }

    /**
     *
     * {@link Propstat}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.4
     */
    @XmlRootElement(name="propstat", namespace = "DAV:")
    public static class Propstat {
        @XmlElement(name = "prop", namespace = "DAV:")
        SimpleFile file;
        @XmlElement(name = "status", namespace = "DAV:")
        String status;

        /**
         * Gets the file
         *
         * @return The file
         */
        public Optional<SimpleFile> getFile() {
            return Optional.of(file);
        }

        /**
         * Gets the status
         *
         * @return The status
         */
        public String getStatus() {
            return status;
        }
    }

    /**
     * {@link SimpleFile}
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     * @since v7.10.4
     */
    @XmlRootElement(name="prop", namespace = "DAV:")
    public static class SimpleFile {
        @XmlElement(name = "getlastmodified", namespace = "DAV:")
        @XmlJavaTypeAdapter(DateAdapter.class)
        Date lastModified;
        @XmlElement(name = "getcontentlength", namespace = "DAV:", required = false)
        Long size;
        @XmlElement(name = "getetag", namespace = "DAV:")
        String etag;
        @XmlElement(name = "getcontenttype", namespace = "DAV:")
        String contentType;
        @XmlElement(name = "fileid", namespace = "http://owncloud.org/ns")
        String fileId;
        @XmlElement(name = "id", namespace = "http://owncloud.org/ns")
        String id;
        @XmlElement(name = "resourcetype", namespace = "DAV:")
        ResourceType resourcetype;

        /**
         * Gets the lastModified
         *
         * @return The lastModified
         */
        public Date getLastModified() {
            return lastModified;
        }

        /**
         * Gets the size
         *
         * @return The size
         */
        public Long getSize() {
            return size;
        }

        /**
         * Gets the etag
         *
         * @return The etag
         */
        public String getEtag() {
            return etag;
        }

        /**
         * Gets the contentType
         *
         * @return The contentType
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * Gets the fileid
         *
         * @return The file id
         */
        public String getFileId() {
            return fileId;
        }

        /**
         * Gets the id
         *
         * @return The id
         */
        public String getId() {
            return id;
        }

        /**
         * Checks whether this file is a collection
         *
         * @return <code>true</code> if this file is a collection, <code>false</code> otherwise
         */
        public boolean isCollection() {
            return resourcetype != null && resourcetype.isCollection();
        }

    }

    @XmlRootElement(name="resourcetype", namespace = "DAV:")
    public static class ResourceType {

        @XmlElement(name = "collection", namespace = "DAV:", nillable = true)
        String collection;

        public boolean isCollection() {
            return collection != null;
        }

    }
}
