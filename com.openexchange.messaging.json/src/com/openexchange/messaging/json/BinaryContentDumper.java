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

package com.openexchange.messaging.json;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.BinaryContent;
import com.openexchange.messaging.MessagingContent;

/**
 * {@link BinaryContentDumper}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class BinaryContentDumper implements MessagingContentDumper {

    @Override
    public void dump(final MessagingContent content, final OutputStream outputStream) throws OXException, IOException {
        final BinaryContent binContent = (BinaryContent) content;
        final InputStream inputStream = binContent.getData();

        final BufferedInputStream bin = new BufferedInputStream(inputStream, 65536);
        final BufferedOutputStream bout = new BufferedOutputStream(outputStream);
        try {
            int i = -1;
            while ((i = bin.read()) > 0) {
                bout.write(i);
            }
        } finally {
            bin.close();
            bout.flush();
        }
    }

    @Override
    public boolean handles(final MessagingContent content) {
        return BinaryContent.class.isInstance(content);
    }

}
