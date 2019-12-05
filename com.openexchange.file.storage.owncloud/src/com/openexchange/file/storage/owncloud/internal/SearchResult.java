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
