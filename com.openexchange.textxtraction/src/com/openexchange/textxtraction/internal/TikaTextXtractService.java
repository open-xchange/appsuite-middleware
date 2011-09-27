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

package com.openexchange.textxtraction.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tika.io.TikaInputStream;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.textxtraction.TextXtractService;

/**
 * {@link TikaTextXtractService} - The text extraction service based on Apache Tika.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TikaTextXtractService implements TextXtractService {

    private static final String UTF_8 = Charsets.UTF_8_NAME;

    /**
     * Initializes a new {@link TikaTextXtractService}.
     */
    public TikaTextXtractService() {
        super();
    }

    private TikaDocumentHandler newDefaultHandler() throws OXException {
        return new TikaDocumentHandler(UTF_8);
    }

    private TikaDocumentHandler newHandler(final String mimeType) throws OXException {
        return new TikaDocumentHandler(mimeType, UTF_8);
    }

    @Override
    public String extractFrom(final String arg, final String optMimeType) throws OXException {
        try {
            final URL url;
            {
                final File file = new File(arg);
                if (file.isFile()) {
                    url = file.toURI().toURL();
                } else {
                    url = new URL(arg);
                }
            }
            final TikaDocumentHandler documentHandler = isEmpty(optMimeType) ? newDefaultHandler() : newHandler(optMimeType);
            final InputStream input = TikaInputStream.get(url, documentHandler.getMetadata());
            return documentHandler.getDocumentContent(input);
        } catch (final MalformedURLException e) {
            throw TextXtractExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw TextXtractExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String extractFrom(final InputStream inputStream, final String optMimeType) throws OXException {
        return (isEmpty(optMimeType) ? newDefaultHandler() : newHandler(optMimeType)).getDocumentContent(inputStream);
    }

    private static boolean isEmpty(final String string) {
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
