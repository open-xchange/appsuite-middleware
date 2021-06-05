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

package com.openexchange.file.storage.json.actions.files;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.java.Streams;
import com.openexchange.rdiff.RdiffService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link DocumentPatchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DocumentPatchAction extends AbstractFileAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(AbstractFileAction.Param.ID);

        IDBasedFileAccess fileAccess = request.getFileAccess();

        List<Closeable> closeables = new ArrayList<Closeable>(4);
        java.io.File baseFile = null;
        java.io.File patchedFile = null;
        try {
            final int buflen = 8192;
            /*
             * Stream document to temporary file
             */
            baseFile = newTempFile();
            {
                final InputStream documentStream = gen(fileAccess.getDocument(request.getId(), request.getVersion()), closeables);
                final OutputStream baseOut = gen(new FileOutputStream(baseFile), closeables);
                // Buffer
                final byte[] buf = new byte[buflen];
                for (int read; (read = documentStream.read(buf, 0, buflen)) > 0;) {
                    baseOut.write(buf, 0, read);
                }
                baseOut.flush();
                dropFirstFrom(closeables);
                dropFirstFrom(closeables);
            }
            /*
             * Stream patch to patchOut
             */
            patchedFile = newTempFile();
            {
                final OutputStream patchOut = gen(new FileOutputStream(patchedFile), closeables);
                InputStream uploadStream = request.getUploadStream();
                if (uploadStream == null) {
                    throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
                }
                final InputStream requestStream = gen(uploadStream, closeables);
                final RdiffService rdiff = Services.getRdiffService();
                if (rdiff == null) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(RdiffService.class.getSimpleName());
                }
                rdiff.rebuildFile(baseFile, requestStream, patchOut);
                dropFirstFrom(closeables);
                dropFirstFrom(closeables);
            }
            /*
             * Update
             */
            final InputStream patchedIn = gen(new FileInputStream(patchedFile), closeables);
            fileAccess.saveDocument(
                fileAccess.getFileMetadata(request.getId(), request.getVersion()),
                patchedIn,
                FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
            /*
             * Return empty result
             */
            return new AJAXRequestResult(JSONObject.NULL, "json");
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            while (!closeables.isEmpty()) {
                Streams.close(closeables.remove(0));
            }
            delete(baseFile);
            delete(patchedFile);
        }
    }

    private static <C extends Closeable> C gen(final C newInstance, final List<Closeable> col) {
        col.add(newInstance);
        return newInstance;
    }

    private static void dropFirstFrom(final List<Closeable> col) {
        Streams.close(col.remove(0));
    }

    private static void delete(final java.io.File file) {
        if (null != file) {
            try {
                file.delete();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Creates a new temporary file.
     *
     * @return A new temporary file
     * @throws OXException If file creation fails
     */
    private static java.io.File newTempFile() throws OXException {
        try {
            UploadDirService uploadDirService = Services.getUploadDirService();
            if (uploadDirService == null) {
                throw ServiceExceptionCode.absentService(UploadDirService.class);
            }

            java.io.File directory = uploadDirService.getUploadDir();
            java.io.File tmpFile = java.io.File.createTempFile("open-xchange-dpa-", ".tmp", directory);
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
