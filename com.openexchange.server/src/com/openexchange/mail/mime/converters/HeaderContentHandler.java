/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
    protected HeaderContentHandler(HeaderCollection headers) {
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
    public void field(Field rawField) throws MimeException {
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
    public void preamble(InputStream is) throws MimeException, IOException {
        // Ignore
    }

    @Override
    public void epilogue(InputStream is) throws MimeException, IOException {
        // Ignore
    }

    @Override
    public void startMultipart(BodyDescriptor bd) throws MimeException {
        // Ignore
    }

    @Override
    public void endMultipart() throws MimeException {
        // Ignore
    }

    @Override
    public void body(BodyDescriptor bd, InputStream is) throws MimeException, IOException {
        // Ignore
    }

    @Override
    public void raw(InputStream is) throws MimeException, IOException {
        // Ignore
    }

}
