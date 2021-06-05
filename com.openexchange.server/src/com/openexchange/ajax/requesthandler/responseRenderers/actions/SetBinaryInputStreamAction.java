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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder.RandomAccess;
import com.openexchange.ajax.fileholder.InputStreamReadable;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;

/**
 * {@link SetBinaryInputStreamAction} set the documentData as an binary InputStream
 *
 * Influence the following IDataWrapper attributes:
 * <ul>
 * <li>documentData
 * </ul>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class SetBinaryInputStreamAction implements IFileResponseRendererAction {

    @Override
    public void call(IDataWrapper data) throws Exception {
        // Set binary input stream
        RandomAccess randomAccess = data.getFile().getRandomAccess();
        if (null == randomAccess) {
            InputStream stream = data.getFile().getStream();
            if (null != stream) {
                if ((stream instanceof ByteArrayInputStream) || (stream instanceof BufferedInputStream)) {
                    data.setDocumentData(new InputStreamReadable(stream));
                } else {
                    data.setDocumentData(new InputStreamReadable(new BufferedInputStream(stream, 65536)));
                }
            }
        } else {
            data.setDocumentData(randomAccess);
        }

        if (null == data.getDocumentData()) {
            // Quit with 404
            throw new FileResponseRenderer.FileResponseRendererActionException(HttpServletResponse.SC_NOT_FOUND, "File not found.");
        }
    }
}
