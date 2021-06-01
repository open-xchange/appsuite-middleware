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

package com.openexchange.preview.internal;

import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.server.ServiceLookup;

/**
 * A content handler that re-writes image src attributes, and passes everything else on to the real one.
 */
public final class TikaImageRewritingContentHandler extends ContentHandlerDecorator {

    private static final String EMBEDDED_PREFIX = "embedded:";

    private final TikaDocumentHandler documentHandler;

    private final ManagedFileManagement fileManagement;

    /**
     * Initializes a new {@link TikaImageRewritingContentHandler}.
     *
     * @param handler The delegate handler
     * @param documentHandler The document handler
     */
    public TikaImageRewritingContentHandler(final ContentHandler handler, final TikaDocumentHandler documentHandler) {
        super(handler);
        this.documentHandler = documentHandler;
        final ServiceLookup serviceLookup = documentHandler.serviceLookup;
        fileManagement = serviceLookup.getService(ManagedFileManagement.class);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes origAttrs) throws SAXException {
        /*
         * If we have an image tag, re-write the src attribute if required
         */
        if (!"img".equals(localName)) {
            super.startElement(uri, localName, qName, origAttrs);
            return;
        }
        /*
         * Handle image tag
         */
        final AttributesImpl attrs = origAttrs instanceof AttributesImpl ? (AttributesImpl) origAttrs : new AttributesImpl(origAttrs);
        final int length = attrs.getLength();
        for (int i = 0; i < length; i++) {
            if ("src".equals(attrs.getLocalName(i))) {
                final String src = attrs.getValue(i);
                if (src.startsWith(EMBEDDED_PREFIX)) {
                    final String resourceName = src.substring(EMBEDDED_PREFIX.length());
                    try {
                        final ManagedFile managedFile = fileManagement.createManagedFile(fileManagement.newTempFile(), -1, true);
                        managedFile.setContentType("image/*");
                        documentHandler.extractedFiles.put(resourceName, managedFile);
                        attrs.setValue(i, managedFile.constructURL(documentHandler.session, false));
                    } catch (OXException e) {
                        throw new SAXException("Couldn't create image file.", e);
                    }
                }
            }
        }
        super.startElement(uri, localName, qName, attrs);
    }

}
