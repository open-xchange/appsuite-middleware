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

package com.openexchange.http.client.xml;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.xml.util.XMLUtils;

public class DOMProcessor implements HTTPResponseProcessor {

    @Override
    public Class<?>[] getTypes() {
        return new Class[] { InputStream.class, Document.class };
    }

    @Override
    public Object process(Object response) throws OXException {
        try {
            DocumentBuilderFactory dbf = XMLUtils.safeDbf(DocumentBuilderFactory.newInstance());
            return dbf.newDocumentBuilder().parse((InputStream) response, "UTF-8");
        } catch (Exception e) {
            throw OxHttpClientExceptionCodes.CATCH_ALL.create(e, e.getMessage());
        }
    }
}
