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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.exception.OXException;
import com.openexchange.html.HtmlService;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.text.HtmlProcessing;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
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
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        super.convert(requestData, result, session, converter);
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
        final boolean asDiv = parseBool(requestData.getParameter("previewForceDiv"));
        final Map<String, String> metaData = previewDocument.getMetaData();
        final List<String> sanitizedHtml = new ArrayList<String>();
        {
            final UserSettingMail usm = session.getUserSettingMail().clone();
            usm.setNoSave(true);
            final boolean edit = parseBool(requestData.getParameter(PARAMETER_EDIT));
            /*
             * Overwrite settings with request's parameters
             */
            final DisplayMode mode = detectDisplayMode(edit, requestData.getParameter(PARAMETER_VIEW), usm);
            final HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
            for (String content : previewDocument.getContent()) {
                if (!DisplayMode.RAW.equals(mode)) {
                    content = htmlService.dropScriptTagsInHeader(content);
                    if (DisplayMode.MODIFYABLE.isIncluded(mode) && usm.isDisplayHtmlInlineContent()) {
                        final boolean[] modified = new boolean[1];
                        final String cssPrefix = "ox-" + HtmlProcessing.getHash(Long.toString(System.currentTimeMillis()), 10);
                        final boolean externalImagesAllowed = usm.isAllowHTMLImages();
                        content = htmlService.checkBaseTag(content, externalImagesAllowed);
                        if (HtmlProcessing.useSanitize()) {
                            // No need to generate well-formed HTML
                            if (externalImagesAllowed) {
                                /*
                                 * TODO: Does not work reliably by now
                                 */
                                // retval = htmlService.checkExternalImages(retval);
                                content = htmlService.sanitize(content, null, false, null, cssPrefix);
                            } else {
                                content = htmlService.sanitize(content, null, true, modified, cssPrefix);
                            }
                        } else {
                            final String charset = metaData.get("charset");
                            content = htmlService.getConformHTML(content, charset == null ? "ISO-8859-1" : charset, false);
                            /*
                             * Filter according to white-list
                             */
                            content = htmlService.filterWhitelist(content);
                            if (externalImagesAllowed) {
                                /*
                                 * TODO: Does not work reliably by now
                                 */
                                // retval = htmlService.checkExternalImages(retval);
                            } else {
                                content = htmlService.filterExternalImages(content, modified);
                            }
                        }
                        /*
                         * Filter inlined images
                         */
                        {
                            final MailMessage mail = (MailMessage) result.getParameter("__mail");
                            if (mail != null) {
                                final MailPath mailPath = new MailPath(mail.getAccountId(), mail.getFolder(), mail.getMailId());
                                content = HtmlProcessing.filterInlineImages(content, session, mailPath);
                            }
                        }
                        /*
                         * Replace CSS classes
                         */
                        content = HtmlProcessing.saneCss(content, htmlService, cssPrefix);
                        if (asDiv) {
                            content = toDiv(content);
                        }
                    }
                }
                sanitizedHtml.add(content);
            }
        }
        // Return
        result.setResultObject(new SanitizedPreviewDocument(metaData, sanitizedHtml, previewDocument.getThumbnail(), previewDocument.isMoreAvailable()), FORMAT);
    }

    private String toDiv(final String content) {
        // Let's try and turn this into an appendable DIV
        String ret = content;
        ret = ret.replaceAll("<body[^>]*>", "<div>");
        ret = ret.substring(ret.indexOf("<div>"));
        ret = ret.replaceAll("</body[^>]*>", "</div>");
        ret = ret.substring(0, ret.lastIndexOf("</div>") + 6);
        return ret;
    }

	/**
     * {@link SanitizedPreviewDocument}
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class SanitizedPreviewDocument implements PreviewDocument {

        private final Map<String, String> metaData;

        private final List<String> sanitizedHtml;

        private final InputStream thumbnail;

        private final Boolean moreAvailable;

        /**
         * Initializes a new {@link SanitizedPreviewDocument}.
         *
         * @param metaData
         * @param sanitizedHtml
         */
        protected SanitizedPreviewDocument(final Map<String, String> metaData, final List<String> sanitizedHtml, final InputStream thumbnail, final Boolean moreAvailable) {
            this.metaData = metaData;
            this.sanitizedHtml = sanitizedHtml;
            this.thumbnail = thumbnail;
            this.moreAvailable = moreAvailable;
        }

        @Override
        public Map<String, String> getMetaData() {
            return metaData;
        }

        @Override
        public boolean hasContent() {
            return null != sanitizedHtml;
        }

        @Override
        public List<String> getContent() {
            return sanitizedHtml;
        }

        @Override
        public InputStream getThumbnail() {
            return thumbnail;
        }

        @Override
        public Boolean isMoreAvailable() {
            return moreAvailable;
        }
    } // End of class SanitizedPreviewDocument

}
