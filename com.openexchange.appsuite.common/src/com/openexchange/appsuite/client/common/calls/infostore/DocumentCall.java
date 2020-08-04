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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.appsuite.client.common.calls.infostore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import com.openexchange.annotation.NonNull;
import com.openexchange.appsuite.client.AppsuiteClientExceptions;
import com.openexchange.appsuite.client.HttpResponseParser;
import com.openexchange.appsuite.client.common.calls.AbstractGetAppsuiteCall;
import com.openexchange.exception.OXException;

/**
 * {@link DocumentCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class DocumentCall extends AbstractGetAppsuiteCall<InputStream> {

    private final String folderId;
    private final String id;
    private final String version;

    /**
     * Initializes a new {@link DocumentCall}.
     *
     * @param folderId The ID of the folder
     * @param id The ID of the document
     */
    public DocumentCall(String folderId, String id) {
        this(folderId, id, null);
    }

    /**
     * Initializes a new {@link DocumentCall}.
     *
     * @param folderId The ID of the folder
     * @param id The ID of the document
     * @param version The version to fetch
     */
    public DocumentCall(String folderId, String id, String version) {
        super();
        this.folderId = folderId;
        this.id = id;
        this.version = version;
    }

    @Override
    @NonNull
    public String getPath() {
        return "/infostore";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        parameters.put("folder", folderId);
        if (version != null) {
            parameters.put("version", version);
        }
    }

    @Override
    protected String getAction() {
        return "document";
    }

    @Override
    public HttpResponseParser<InputStream> getParser() throws OXException {

        return new HttpResponseParser<InputStream>() {

            @Override
            public InputStream parse(HttpResponse response, HttpContext httpContext) throws OXException {
                try {
                    return response.getEntity().getContent();
                } catch (UnsupportedOperationException e) {
                    throw AppsuiteClientExceptions.UNEXPECTED_ERROR.create(e, e.getMessage());
                } catch (IOException e) {
                    throw AppsuiteClientExceptions.IO_ERROR.create(e, e.getMessage());
                }
            }
        };
    }
}
