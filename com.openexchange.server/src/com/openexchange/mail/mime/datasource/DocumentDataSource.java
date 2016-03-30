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

    private static IDBasedFileAccess fileAccess(final Session session) throws OXException {
        return ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class, true).createAccess(session);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        IDBasedFileAccess fileAccess = null;
        try {
            fileAccess = fileAccess(session);
            return fileAccess.getDocument(documentId, FileStorageFileAccess.CURRENT_VERSION);
        } catch (final OXException e) {
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
            } catch (final Exception e) {
                // IGNORE
            }
        }
    }

}
