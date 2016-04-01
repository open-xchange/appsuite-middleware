package com.openexchange.file.storage;

import com.openexchange.exception.OXException;

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

/**
 * Implementing the {@link FileStorageEfficientRetrieval} interface with the {@link FileStorageFileAccess} allows an implementation to bypass the
 * file metadata lookup in certain scenarios. In order for an etag based caching to continue working, usually the file metadata has to be retrieved to 
 * check and set an HTTP etag (essentially an arbitrary string). An etag is a token that changes when the file content changes. Clients detect a stale cache by supplying their etag, and expecting either
 * a 304 (Not Modified) status code, when the cache is still relevant (and no file data) or a regular response with the file. Implementing this interface allows higher levels to bypass
 * the metadata lookup by supplying the needed metadata along with a handle to retrieve the document. In parallel to the HTTP etag handling, two methods have to be implemented
 * one that deals with accesses without an etag, one that deals with accesses with an etag. If an etag is supplied and matches the etag of the stored file, #getInputStream is never called.
 * 
 * If an underlying system implements a similar mechanism, this can be used to bypass metadata retrieval.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface FileStorageEfficientRetrieval {

    /**
     * Retrieve document data and associated metadata from the underlying implementation. This is more efficient than a sequence of #getFileMetadata and #getDocument
     * but will have to provide all known metadata in the document instance
     * @param folderId The folder ID
     * @param fileId The File ID
     * @param version The Version
     * @return The Document or null
     * @throws OXException 
     */
    Document getDocumentAndMetadata(String folderId, String fileId, String version) throws OXException;

    /**
     * Retrieve document data and associated metadata from the underlying implementation. This is more efficient than a sequence of #getFileMetadata and #getDocument
     * but will have to provide all known metadata in the document instance. Also provides the etag of the document the client has cached. Only provide
     * an InputStream in the document, if the clientETag is not valid anymore. Always provide at least the eTag in the document, so calling classes know whether to
     * expect an input stream.
     * @param folderId The folder ID
     * @param fileId The File ID
     * @param version The Version
     * @param clientETag the client etag to compare the current etag to. Only load the input stream if the client has a stale etag.
     * @return The Document or null
     * @throws OXException 
     */
    Document getDocumentAndMetadata(String folderId, String fileId, String version, String clientETag) throws OXException;

}
