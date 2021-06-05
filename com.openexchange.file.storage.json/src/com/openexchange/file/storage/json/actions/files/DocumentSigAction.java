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

import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.json.services.Services;
import com.openexchange.java.Streams;
import com.openexchange.rdiff.RdiffService;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link DocumentSigAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@DispatcherNotes(defaultFormat = "file")
public class DocumentSigAction extends AbstractFileAction {

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(AbstractFileAction.Param.ID);

        IDBasedFileAccess fileAccess = request.getFileAccess();

        InputStream documentStream = null;
        ThresholdFileHolder fileHolder = null;
        try {
            final RdiffService rdiff = Services.getRdiffService();
            if (rdiff == null) {
                throw ServiceExceptionCode.absentService(RdiffService.class);
            }
            documentStream = fileAccess.getDocument(request.getId(), request.getVersion());
            // Make signature
            fileHolder = new ThresholdFileHolder();
            rdiff.createSignatures(documentStream, fileHolder.asOutputStream());
            // Return FileHolder result
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
            fileHolder = null;
            return requestResult;
        } finally {
            Streams.close(fileHolder, documentStream);
        }
    }

}
