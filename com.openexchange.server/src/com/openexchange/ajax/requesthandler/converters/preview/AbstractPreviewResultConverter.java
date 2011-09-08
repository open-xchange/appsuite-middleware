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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.java.Streams;
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
public abstract class AbstractPreviewResultConverter implements ResultConverter {

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
    public void convert(final AJAXRequestData request, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        final FileHolder fileHolder;
        {
            final Object resultObject = result.getResultObject();
            if (!(resultObject instanceof FileHolder)) {
                throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(FileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
            }
            fileHolder = (FileHolder) resultObject;
        }
        /*
         * Obtain preview service
         */
        final PreviewDocument previewDocument;
        {
            final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);
            if (autoDetect()) {
                previewDocument = previewService.getPreviewFor(fileHolder.getStream(), getOutput(), session);
            } else {
                final DataProperties dataProperties = new DataProperties(4);
                dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, fileHolder.getContentType());
                dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
                dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
                dataProperties.put(DataProperties.PROPERTY_SIZE, String.valueOf(fileHolder.getLength()));
                previewDocument =
                    previewService.getPreviewFor(new SimpleData<InputStream>(fileHolder.getStream(), dataProperties), getOutput(), session);
            }
        }
        /*
         * Provide URL to document
         */
        final ManagedFile managedFile;
        try {
            final ManagedFileManagement fileManagement = ServerServiceRegistry.getInstance().getService(ManagedFileManagement.class);
            final File tempFile = fileManagement.newTempFile();
            final FileOutputStream fos = new FileOutputStream(tempFile);
            try {
                fos.write(previewDocument.getContent().getBytes("UTF-8"));
                fos.flush();
            } finally {
                Streams.close(fos);
            }
            managedFile = fileManagement.createManagedFile(tempFile);
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
        /*
         * Set meta data
         */
        final Map<String, String> metaData = previewDocument.getMetaData();
        managedFile.setContentType(metaData.get("content-type"));
        managedFile.setFileName(metaData.get("resourcename"));
        /*
         * Set result object
         */
        result.setResultObject(managedFile.constructURL(session), getOutputFormat());
    }

    /**
     * Indicates whether to auto-detect passed content.
     *
     * @return <code>true</code> to auto-detect; otherwise <code>false</code>
     */
    protected abstract boolean autoDetect();

    /**
     * Gets the desired output format.
     *
     * @return The output format
     */
    protected abstract PreviewOutput getOutput();

}
