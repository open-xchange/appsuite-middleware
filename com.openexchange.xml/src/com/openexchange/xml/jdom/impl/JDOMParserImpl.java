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

package com.openexchange.xml.jdom.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.openexchange.xml.jdom.JDOMParser;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class JDOMParserImpl implements JDOMParser {

    /**
     * Empty XML entity resolver to prevent resolving external entitites.
     */
    private static final EntityResolver EMPTY_RESOLVER = new EntityResolver() {

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return new InputSource(new ByteArrayInputStream(new byte[0]));
        }
    };

    public JDOMParserImpl() {
        super();
    }

    @Override
    public Document parse(final InputStream is) throws JDOMException, IOException {
        if (null == is) {
            return null;
        }
        /*
         * create builder and disable possible unsafe features
         */
        SAXBuilder builder = new SAXBuilder();
        builder.setEntityResolver(EMPTY_RESOLVER);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        builder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        /*
         * build
         */
        return builder.build(is);
    }
}
