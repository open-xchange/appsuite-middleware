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

package com.openexchange.dav.caldav.methods;

import java.io.IOException;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.w3c.dom.Document;

/**
 * {@link CustomPropFindMethod}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CustomPropFindMethod extends PropFindMethod {

    private Document responseDocument;

    public CustomPropFindMethod(String uri, int propfindType, DavPropertyNameSet propNameSet, int depth) throws IOException {
        super(uri, propfindType, propNameSet, depth);
    }

    @Override
    public Document getResponseBodyAsDocument() throws IOException {
        if (null == responseDocument) {
            responseDocument = super.getResponseBodyAsDocument();
        }
        return responseDocument;
    }

}
