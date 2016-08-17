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
