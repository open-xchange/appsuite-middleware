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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.textxtraction.cleanContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import net.bitform.api.secure.SecureOptions;
import net.bitform.api.secure.SecureOptions.OutputTypeOption;
import net.bitform.api.secure.SecureRequest;
import net.bitform.api.secure.SecureResponse;
import org.apache.commons.io.IOUtils;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.textxtraction.DelegateTextXtraction;
import com.openexchange.textxtraction.TextXtractExceptionCodes;

/**
 * {@link CleanContentTextXtraction} - The text extractor based on CleanContent SDK.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CleanContentTextXtraction implements DelegateTextXtraction {

    /**
     * Initializes a new {@link CleanContentTextXtraction}.
     */
    public CleanContentTextXtraction() {
        super();
    }

    @Override
    public String extractFrom(final InputStream inputStream, final String optMimeType) throws OXException {
        boolean extracted = false;
        try {
            /*
             * Note that the SecureRequest object is REUSED for all the file.
             */ 
            final SecureRequest request = new SecureRequest();
            request.setOption(SecureOptions.JustAnalyze, true);             
            if (inputStream instanceof FileInputStream) {
                request.setOption(SecureOptions.SourceDocument, inputStream);
            } else {      
                byte[] byteArray = IOUtils.toByteArray(inputStream);
                ByteBuffer buffer = ByteBuffer.wrap(byteArray);
                request.setOption(SecureOptions.SourceDocument, buffer);
            }
            
            final TextAppendingElementHandler elementHandlerImpl = new TextAppendingElementHandler();
            request.setOption(SecureOptions.ElementHandler, elementHandlerImpl);
            request.setOption(SecureOptions.OutputType, OutputTypeOption.ToHandler);
            /*
             * Execute the request
             */
            request.execute();
            /*
             * Get the response
             */
            final SecureResponse response = request.getResponse();
            if (response.getResult(SecureOptions.WasProcessed)) {
                extracted = true;
                return elementHandlerImpl.getText();
            }
            
            return null;
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (extracted) {
                Streams.close(inputStream);
            }
        }
    }

    @Override
    public String extractFrom(final String content, final String optMimeType) throws OXException {
        final SecureRequest request = new SecureRequest();
        request.setOption(SecureOptions.JustAnalyze, true);
        /*
         * Note that the SecureRequest object is REUSED for all the file.
         */
        request.setOption(SecureOptions.SourceDocument, ByteBuffer.wrap(content.getBytes(Charsets.ISO_8859_1)));
        final TextAppendingElementHandler elementHandlerImpl = new TextAppendingElementHandler();
        request.setOption(SecureOptions.ElementHandler, elementHandlerImpl);
        request.setOption(SecureOptions.OutputType, OutputTypeOption.ToHandler);
        try {
            /*
             * Execute the request
             */
            request.execute();
            /*
             * Get the response
             */
            final SecureResponse response = request.getResponse();
            if (response.getResult(SecureOptions.WasProcessed)) {
                return elementHandlerImpl.getText();
            }
            return null;
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        }
        
    }

    @Override
    public String extractFromResource(final String resource, final String optMimeType) throws OXException {
        final URL url;
        try {
            final File file = new File(resource);
            if (file.isFile()) {
                url = file.toURI().toURL();
            } else {
                url = new URL(resource);
            }
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        }
        final SecureRequest request = new SecureRequest();
        request.setOption(SecureOptions.JustAnalyze, true);
        /*
         * Note that the SecureRequest object is REUSED for all the file.
         */
        request.setOption(SecureOptions.SourceDocument, url);
        final TextAppendingElementHandler elementHandlerImpl = new TextAppendingElementHandler();
        request.setOption(SecureOptions.ElementHandler, elementHandlerImpl);
        request.setOption(SecureOptions.OutputType, OutputTypeOption.ToHandler);
        try {
            /*
             * Execute the request
             */
            request.execute();
            /*
             * Get the response
             */
            final SecureResponse response = request.getResponse();
            if (response.getResult(SecureOptions.WasProcessed)) {
                return elementHandlerImpl.getText();
            }
            return null;
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public boolean isDestructive() {
        return true;
    }
}
