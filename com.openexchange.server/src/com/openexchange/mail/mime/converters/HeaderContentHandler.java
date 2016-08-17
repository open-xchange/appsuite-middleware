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

package com.openexchange.mail.mime.converters;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.unfold;
import java.io.IOException;
import java.io.InputStream;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.parser.ContentHandler;
import org.apache.james.mime4j.stream.BodyDescriptor;
import org.apache.james.mime4j.stream.Field;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link HeaderContentHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
class HeaderContentHandler implements ContentHandler {

    static final class EndHeaderException extends MimeException {

        private static final long serialVersionUID = 7701096104485623980L;

        EndHeaderException() {
            super("endHeader");
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

    }

    /**
     * Signals end of header section.
     */
    static final EndHeaderException END_HEADER_EXCEPTION = new EndHeaderException();

    private HeaderCollection headers;

    private boolean active;

    /**
     * Initializes a new {@link HeaderContentHandler}.
     */
    public HeaderContentHandler() {
        super();
    }

    /**
     * Initializes a new {@link HeaderContentHandler}.
     *
     * @param headers The header collection to fill
     */
    protected HeaderContentHandler(final HeaderCollection headers) {
        super();
        this.headers = headers;
    }

    @Override
    public void startMessage() throws MimeException {
        // Ignore
    }

    @Override
    public void endMessage() throws MimeException {
        // Ignore
    }

    @Override
    public void startBodyPart() throws MimeException {
        // Ignore
    }

    @Override
    public void endBodyPart() throws MimeException {
        // Ignore
    }

    @Override
    public void startHeader() throws MimeException {
        if (null == headers) {
            headers = new HeaderCollection(128);
        }
        active = true;
    }

    @Override
    public void field(final Field rawField) throws MimeException {
        if (active) {
            final String name = rawField.getName();
            if ("x-original-headers".equals(name)) {
                return;
            }
            final String value = rawField.getBody();
            if (com.openexchange.java.Strings.isEmpty(value)) {
                headers.addHeader(name, "");
            } else {
                headers.addHeader(name, unfold(value));
            }
        }
    }

    @Override
    public void endHeader() throws MimeException {
        active = false;
        throw END_HEADER_EXCEPTION;
    }

    @Override
    public void preamble(final InputStream is) throws MimeException, IOException {
        // Ignore
    }

    @Override
    public void epilogue(final InputStream is) throws MimeException, IOException {
        // Ignore
    }

    @Override
    public void startMultipart(final BodyDescriptor bd) throws MimeException {
        // Ignore
    }

    @Override
    public void endMultipart() throws MimeException {
        // Ignore
    }

    @Override
    public void body(final BodyDescriptor bd, final InputStream is) throws MimeException, IOException {
        // Ignore
    }

    @Override
    public void raw(final InputStream is) throws MimeException, IOException {
        // Ignore
    }

}
