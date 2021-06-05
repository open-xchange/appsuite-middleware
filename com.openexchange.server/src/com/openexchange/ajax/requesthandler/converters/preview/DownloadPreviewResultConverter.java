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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DownloadPreviewResultConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class DownloadPreviewResultConverter extends AbstractPreviewResultConverter {


    /**
     * Initializes a new {@link DownloadPreviewResultConverter}.
     */
    public DownloadPreviewResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "preview";
    }

    @Override
    public String getOutputFormat() {
        return "preview_download";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public PreviewOutput getOutput() {
        return PreviewOutput.HTML;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        super.convert(requestData, result, session, converter);

        /*
         * Provide URL to document
         */
        final PreviewDocument previewDocument = (PreviewDocument) result.getResultObject();
        final ManagedFile managedFile;
        try {
            final ManagedFileManagement fileManagement = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            final File tempFile = fileManagement.newTempFile();
            final FileOutputStream fos = new FileOutputStream(tempFile);
            try {
                if (previewDocument.hasContent()) {
                    fos.write(previewDocument.getContent().get(0).getBytes(com.openexchange.java.Charsets.UTF_8));
                    fos.flush();
                }
            } finally {
                Streams.close(fos);
            }
            managedFile = fileManagement.createManagedFile(tempFile, -1, true);
        } catch (IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        /*
         * Set meta data
         */
        final Map<String, String> metaData = previewDocument.getMetaData();
        managedFile.setContentType(metaData.get("content-type"));
        managedFile.setFileName(metaData.get("resourcename"));
        /*
         * Set result object
         */
        result.setResultObject(managedFile.constructURL(session, false), getOutputFormat());
    }

}
