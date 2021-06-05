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

package com.openexchange.notification.mail.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.activation.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.notification.mail.MailAttachment;


/**
 * {@link AbstractMailAttachment}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public abstract class AbstractMailAttachment implements MailAttachment {

    /** The headers mapping */
    protected final Map<String, String> headers;

    /** The content type */
    protected String contentType;

    /** The name */
    protected String name;

    /** The disposition */
    protected String disposition;

    /**
     * Initializes a new {@link AbstractMailAttachment}.
     */
    protected AbstractMailAttachment() {
        super();
        headers = new LinkedHashMap<String, String>(4);
    }

    /**
     * Creates the {@link DataSource} view for this attachment.
     *
     * @return The <code>DataSource</code> instance
     * @throws IOException If <code>DataSource</code> instance cannot be returned
     * @throws OXException If <code>DataSource</code> instance cannot be returned
     */
    public abstract DataSource asDataHandler() throws IOException, OXException;

    @Override
    public void close() throws Exception {
        // Nothing
    }

    @Override
    public long getLength() {
        return -1;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDisposition() {
        return disposition;
    }

    @Override
    public void setDisposition(String disposition) {
        this.disposition = disposition;
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public void setHeader(String name, String value) {
        if (null == name) {
            return;
        }
        if (null == value) {
            headers.remove(name);
        } else {
            headers.put(name, value);
        }
    }

    @Override
    public void addHeaders(Map<? extends String, ? extends String> headers) {
        if (null != headers) {
            this.headers.putAll(headers);
        }
    }

}
