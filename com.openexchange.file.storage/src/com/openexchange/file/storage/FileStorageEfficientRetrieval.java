package com.openexchange.file.storage;

import com.openexchange.exception.OXException;

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
