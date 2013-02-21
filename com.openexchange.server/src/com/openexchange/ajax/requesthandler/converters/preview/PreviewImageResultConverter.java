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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PreviewImageResultConverter}
 * 
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class PreviewImageResultConverter extends AbstractPreviewResultConverter {

    @Override
    public String getOutputFormat() {
        return "preview_image";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public PreviewOutput getOutput() {
        return PreviewOutput.IMAGE;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        try {
            final Object resultObject = result.getResultObject();
            if (!(resultObject instanceof IFileHolder)) {
                throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(
                    IFileHolder.class.getSimpleName(),
                    null == resultObject ? "null" : resultObject.getClass().getSimpleName());
            }
            final IFileHolder fileHolder = (IFileHolder) resultObject;

            final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);

            final DataProperties dataProperties = new DataProperties(4);
            dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, getContentType(fileHolder));
            dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
            dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
            dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));

            final PreviewDocument previewDocument =
                previewService.getPreviewFor(new SimpleData<InputStream>(fileHolder.getStream(), dataProperties), getOutput(), session, 1);

            requestData.setFormat("file");

            final InputStream thumbnail = previewDocument.getThumbnail();
            if (null == thumbnail) {
                // No thumbnail available
                throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create();
            }

            final String fileName = previewDocument.getMetaData().get("resourcename");
            final FileHolder responseFileHolder = new FileHolder(thumbnail, -1, "image/jpeg", fileName); // TODO: file length
            result.setResultObject(responseFileHolder, "file");
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final Set<String> INVALIDS = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList(
        "application/octet-stream",
        "application/force-download",
        "application/binary",
        "application/x-download",
        "application/octet-stream",
        "application/vnd",
        "application/vnd.ms-word.document.12n",
        "application/odt",
        "application/x-pdf")));

    private String getContentType(final IFileHolder fileHolder) {
        String contentType = getLowerCaseBaseType(fileHolder.getContentType());
        if (isEmpty(contentType) || INVALIDS.contains(contentType)) {
            contentType = MimeType2ExtMap.getContentType(fileHolder.getName());
        }
        return contentType == null ? "application/octet-stream" : contentType;
    }

    private String getLowerCaseBaseType(final String contentType) {
        if (null == contentType) {
            return null;
        }
        final int pos = contentType.indexOf(';');
        return toLowerCase(pos > 0 ? contentType.substring(0, pos) : contentType).trim();
    }

    /** ASCII-wise to lower-case */
    private String toLowerCase(final CharSequence chars) {
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /** Checks for an empty string */
    private boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
