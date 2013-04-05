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

package com.openexchange.mail.mime.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import com.openexchange.conversion.DataHandler;

/**
 * {@link DataContentHandlerDataSource} - A {@link DataSource} backed by a {@link DataContentHandler}.
 * <p>
 * This bypasses the need for {@link DataHandler} to look-up an appropriate {@link DataContentHandler}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DataContentHandlerDataSource implements DataSource {

    private final DataContentHandler dch;
    private final Object object;
    private final String objectMimeType;

    /**
     * Initializes a new {@link DataContentHandlerDataSource}.
     */
    public DataContentHandlerDataSource(final Object object, final String objectMimeType, final DataContentHandler dch) {
        super();
        this.object = object;
        this.objectMimeType = objectMimeType;
        this.dch = dch;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pin = new PipedInputStream(pos);
        
        final DataContentHandler dch = this.dch;
        final Object object = this.object;
        final String objectMimeType = this.objectMimeType;
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    dch.writeTo(object, objectMimeType, pos);
                } catch (final IOException e) {
                    // Ignore
                } finally {
                    try {
                        pos.close();
                    } catch (final IOException ie) {
                        // Ignore
                    }
                }
            }
        }, "DataContentHandlerDataSource.getInputStream").start();
        return pin;

    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public String getContentType() {
        return objectMimeType;
    }

    @Override
    public String getName() {
        return null;
    }

}
