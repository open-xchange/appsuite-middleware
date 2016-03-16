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
                        final ManagedFile managedFile = fileManagement.createManagedFile(fileManagement.newTempFile());
                        managedFile.setContentType("image/*");
                        documentHandler.extractedFiles.put(resourceName, managedFile);
                        attrs.setValue(i, managedFile.constructURL(documentHandler.session));
                    } catch (final OXException e) {
                        throw new SAXException("Couldn't create image file.", e);
                    }
                }
            }
        }
        super.startElement(uri, localName, qName, attrs);
    }

}
