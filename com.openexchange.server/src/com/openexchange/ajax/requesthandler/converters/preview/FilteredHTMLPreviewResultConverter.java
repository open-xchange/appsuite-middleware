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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.exception.OXException;
import com.openexchange.html.HTMLService;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FilteredHTMLPreviewResultConverter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FilteredHTMLPreviewResultConverter extends AbstractPreviewResultConverter {

    private static final String FORMAT = "preview_filtered";

    public FilteredHTMLPreviewResultConverter() {
        super();
    }

    @Override
    public String getOutputFormat() {
        return FORMAT;
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
    public void convert(final AJAXRequestData request, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        super.convert(request, result, session, converter);
        final Object resultObject = result.getResultObject();
        if (!(resultObject instanceof PreviewDocument)) {
            return;
        }
        /*
         * Sanitize document's HTML content
         */
        final PreviewDocument previewDocument = (PreviewDocument) resultObject;
        if (resultObject instanceof SanitizedPreviewDocument) {
            // Already sanitized
            return;
        }
        final Map<String, String> metaData = previewDocument.getMetaData();
        final String sanitizedHtml;
        {
            final HTMLService htmlService = ServerServiceRegistry.getInstance().getService(HTMLService.class);
            String content = previewDocument.getContent();
            content = htmlService.dropScriptTagsInHeader(content);
            final String charset = metaData.get("charset");
            content = htmlService.getConformHTML(content, charset == null ? "ISO-8859-1" : charset, false);
            content = htmlService.checkBaseTag(content, false);
            /*
             * Filter according to white-list
             */
            content = htmlService.filterWhitelist(content);
            final boolean[] modified = new boolean[1];
            content = htmlService.filterExternalImages(content, modified);
            sanitizedHtml = content;
        }
        result.setResultObject(new SanitizedPreviewDocument(metaData, sanitizedHtml), FORMAT);
    }

    /**
     * {@link SanitizedPreviewDocument}
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class SanitizedPreviewDocument implements PreviewDocument {

        private final Map<String, String> metaData;

        private final String sanitizedHtml;

        /**
         * Initializes a new {@link SanitizedPreviewDocument}.
         * 
         * @param metaData
         * @param sanitizedHtml
         */
        protected SanitizedPreviewDocument(final Map<String, String> metaData, final String sanitizedHtml) {
            this.metaData = metaData;
            this.sanitizedHtml = sanitizedHtml;
        }

        @Override
        public Map<String, String> getMetaData() {
            return metaData;
        }

        @Override
        public String getContent() {
            return sanitizedHtml;
        }
    } // End of class SanitizedPreviewDocument

}
