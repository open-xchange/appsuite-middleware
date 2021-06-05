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

package com.openexchange.mail.mime.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link DocumentDataSource} - A simple {@link DataSource data source} that encapsulates a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DocumentDataSource implements DataSource {

    private final Session session;
    private final String documentId;
    private final String name;
    private final String contentType;

    /**
     * Initializes a new {@link DocumentDataSource}.
     *
     * @param documentId The document identifier
     * @param contentType The MIME type
     * @param name The name
     * @param session The associated session
     */
    public DocumentDataSource(String documentId, String contentType, String name, Session session) {
        super();
        this.documentId = documentId;
        this.contentType = contentType;
        this.name = name;
        this.session = session;
    }

    private static IDBasedFileAccess fileAccess(Session session) throws OXException {
        return ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true).createAccess(session);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        IDBasedFileAccess fileAccess = null;
        try {
            fileAccess = fileAccess(session);
            return fileAccess.getDocument(documentId, FileStorageFileAccess.CURRENT_VERSION);
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException("Input stream cannot be retrieved", e);
        } finally {
            finishSafe(fileAccess);
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("DocumentDataSource.getOutputStream()");
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    private static void finishSafe(IDBasedFileAccess fileAccess) {
        if (fileAccess != null) {
            try {
                fileAccess.finish();
            } catch (Exception e) {
                // IGNORE
            }
        }
    }

}
