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

package com.openexchange.groupware.infostore.facade.impl;

import java.io.InputStream;
import java.util.Set;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;

class SaveParameters {

    private final Context context;
    private final ServerSession session;
    private final DocumentMetadata document;
    private final DocumentMetadata oldDocument;
    private final long sequenceNumber;
    private final Set<Metadata> updatedCols;
    private final int optFolderAdmin;

    private InputStream data = null;
    private long offset = -1L;
    private int fileCreatedBy = -1;
    private boolean ignoreVersion = false;

    SaveParameters(Context context, ServerSession session, DocumentMetadata document, DocumentMetadata oldDocument, long sequenceNumber, Set<Metadata> updatedCols, int optFolderAdmin) {
        super();
        this.context = context;
        this.session = session;
        this.document = document;
        this.oldDocument = oldDocument;
        this.sequenceNumber = sequenceNumber;
        this.updatedCols = updatedCols;
        this.optFolderAdmin = optFolderAdmin;
    }

    int getOptFolderAdmin() {
        return optFolderAdmin;
    }

    DocumentMetadata getDocument() {
        return document;
    }

    DocumentMetadata getOldDocument() {
        return oldDocument;
    }

    long getSequenceNumber() {
        return sequenceNumber;
    }

    Set<Metadata> getUpdatedCols() {
        return updatedCols;
    }

    void setData(InputStream data, long offset, int createdBy, boolean ignoreVersion) {
        this.data = data;
        this.offset = offset;
        this.fileCreatedBy = createdBy;
        this.ignoreVersion = ignoreVersion;
    }

    boolean hasData() {
        return data != null;
    }

    InputStream getData() {
        return data;
    }

    long getOffset() {
        return offset;
    }

    int getFileCreatedBy() {
        return fileCreatedBy;
    }

    boolean isIgnoreVersion() {
        return ignoreVersion;
    }

    Context getContext() {
        return context;
    }

    ServerSession getSession() {
        return session;
    }

}