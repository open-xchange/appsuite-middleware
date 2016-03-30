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
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.java.Streams;
import com.openexchange.rdiff.RdiffService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

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
                final InputStream requestStream = gen(request.getUploadStream(), closeables);
                final RdiffService rdiff = Services.getRdiffService();
                rdiff.rebuildFile(baseFile, requestStream, patchOut);
                patchOut.flush();
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
        } catch (final IOException e) {
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
            } catch (final Exception e) {
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
            java.io.File directory = new java.io.File(ServerConfig.getProperty(ServerConfig.Property.UploadDirectory));
            java.io.File tmpFile = java.io.File.createTempFile("open-xchange-dpa-", ".tmp", directory);
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
