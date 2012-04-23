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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractPreviewResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractPreviewResultConverter implements ResultConverter {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractPreviewResultConverter.class));

    /**
     * The <code>"view"</code> parameter.
     */
    protected static final String PARAMETER_VIEW = "view";

    /**
     * The <code>"edit"</code> parameter.
     */
    protected static final String PARAMETER_EDIT = "edit";

    /**
     * Initializes a new {@link AbstractPreviewResultConverter}.
     */
    protected AbstractPreviewResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "file";
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final IFileHolder fileHolder;
        {
            final Object resultObject = result.getResultObject();
            if (!(resultObject instanceof IFileHolder)) {
                throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(IFileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
            }
            fileHolder = (IFileHolder) resultObject;
        }
        try {
            /*
             * Obtain preview document
             */
            final PreviewDocument previewDocument;
            {
                final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);

                final DataProperties dataProperties = new DataProperties(4);
                dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, fileHolder.getContentType());
                dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
                dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
                dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));
                
                int pages = -1;
                if (requestData.containsParameter("pages")) {
                    pages = requestData.getIntParameter("pages");
                }
                previewDocument = previewService.getPreviewFor(new SimpleData<InputStream>(fileHolder.getStream(), dataProperties), getOutput(), session, pages);

            }
            if (requestData.getIntParameter("save") == 1) {
                // TODO:
//                /*-
//                 * Preview document should be saved.
//                 * We set the request format to file and return a FileHolder
//                 * containing the preview document.
//                 */
//                requestData.setFormat("file");
//                final byte[] documentBytes = previewDocument.getContent().getBytes();
//                final InputStream is = new ByteArrayInputStream(documentBytes);
//                final String contentType = previewDocument.getMetaData().get("content-type");
//                final String fileName = previewDocument.getMetaData().get("resourcename");
//                final FileHolder responseFileHolder = new FileHolder(is, documentBytes.length, contentType, fileName);
//                result.setResultObject(responseFileHolder, "file");
            } else {
                result.setResultObject(previewDocument, getOutputFormat());
            }
        } finally {
            Streams.close(fileHolder);
        }
    }

    private static final Set<String> BOOLS = new HashSet<String>(Arrays.asList("true", "yes", "y", "on", "1"));

    /**
     * Parses specified value to a <code>boolean</code>:<br>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or "1".
     * 
     * @param value The value to parse
     * @return The parsed <code>boolean</code> value
     */
    protected static boolean parseBool(final String value) {
        if (null == value) {
            return false;
        }
        return BOOLS.contains(value.trim().toLowerCase(Locale.US));
    }

    private static final String VIEW_RAW = "raw";

    private static final String VIEW_TEXT = "text";

    private static final String VIEW_TEXT_NO_HTML_ATTACHMENT = "textNoHtmlAttach";

    private static final String VIEW_HTML = "html";

    private static final String VIEW_HTML_BLOCKED_IMAGES = "noimg";

    /**
     * Detects display mode dependent on passed arguments.
     * 
     * @param modifyable Whether content is intended being modified by client
     * @param view The view parameter
     * @param usm The user settings
     * @return The display mode
     */
    protected static DisplayMode detectDisplayMode(final boolean modifyable, final String view, final UserSettingMail usm) {
        if (null == view) {
            return modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        }
        final DisplayMode displayMode;
        if (VIEW_RAW.equals(view)) {
            displayMode = DisplayMode.RAW;
        } else if (VIEW_TEXT_NO_HTML_ATTACHMENT.equals(view)) {
            usm.setDisplayHtmlInlineContent(false);
            usm.setSuppressHTMLAlternativePart(true);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else if (VIEW_TEXT.equals(view)) {
            usm.setDisplayHtmlInlineContent(false);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else if (VIEW_HTML.equals(view)) {
            usm.setDisplayHtmlInlineContent(true);
            usm.setAllowHTMLImages(true);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
            usm.setDisplayHtmlInlineContent(true);
            usm.setAllowHTMLImages(false);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else {
            LOG.warn(new StringBuilder(64).append("Unknown value in parameter ").append(PARAMETER_VIEW).append(": ").append(view).append(
                ". Using user's mail settings as fallback."));
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        }
        return displayMode;
    }

    /**
     * Gets the desired output format.
     *
     * @return The output format
     */
    public abstract PreviewOutput getOutput();

}
