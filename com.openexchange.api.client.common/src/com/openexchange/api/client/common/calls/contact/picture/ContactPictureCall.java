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

package com.openexchange.api.client.common.calls.contact.picture;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.annotation.NonNull;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.InputStreamAwareResponse;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.InputStreamParser;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Functions.OXFunction;
import com.openexchange.java.Strings;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link ContactPictureCall}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ContactPictureCall extends AbstractGetCall<ContactPicture> {

    static final Logger LOGGER = LoggerFactory.getLogger(ContactPictureCall.class);

    /**
     * The Last-Modified header-name
     */
    private static final String LAST_MODIFIED = "Last-Modified";

    /**
     * The ETag header-name
     */
    private static final String HEADER_ETAG = "ETag";

    final OXFunction<String, Date, ParseException> parseLastModified = (s) -> {
        return Strings.isEmpty(s) ? null : Tools.parseHeaderDate(s);
    };

    private final PictureSearchData data;

    /**
     * Initializes a new {@link ContactPictureCall}.
     * 
     * @param data The data to fill the request with
     */
    public ContactPictureCall(PictureSearchData data) {
        super();
        this.data = data;
    }

    @Override
    @NonNull
    public String getModule() {
        return "contacts/picture";
    }

    @Override
    protected String getAction() {
        return "get";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        putIfNotEmpty(parameters, "user_id", data.getUserId());
        putIfNotEmpty(parameters, "contact_id", data.getContactId());
        putIfNotEmpty(parameters, "folder_id", data.getFolderId());
        putIfNotEmpty(parameters, "account_id", data.getAccountId());
        if (data.hasEmail()) {
            putIfNotEmpty(parameters, "email", data.getEmails().stream().collect(Collectors.joining(",")));
        }
    }

    @Override
    public HttpResponseParser<ContactPicture> getParser() {
        return new HttpResponseParser<ContactPicture>() {

            @Override
            public ContactPicture parse(HttpResponse response, HttpContext httpContext) throws OXException {
                if (response.getStatusLine() == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    return ContactPicture.NOT_FOUND;
                }
                String eTag = ApiClientUtils.getHeaderValue(response, HEADER_ETAG);
                String lastModified = ApiClientUtils.getHeaderValue(response, LAST_MODIFIED);
                Date lm = parseLastModified.consumeError(lastModified, (e) -> LOGGER.debug("Unable to parse last modified", e)).orElse(null);

                ContactPictureResponse contactPictureResponse = new ContactPictureResponse(eTag, lm);
                InputStream stream = new InputStreamParser().parse(response, httpContext);
                if (stream != null) {
                    contactPictureResponse.setInputStream(stream);
                }
                return contactPictureResponse;
            }
        };
    }

    private static class ContactPictureResponse extends ContactPicture implements InputStreamAwareResponse {

        private InputStream in;

        /**
         * Initializes a new {@link ContactPictureResponse}.
         * 
         * @param eTag The ETag
         * @param lastModified The last modified
         */
        public ContactPictureResponse(String eTag, Date lastModified) {
            super(eTag, null, lastModified);
        }

        @Override
        @Nullable
        public InputStream getInputStream() {
            return in;
        }

        @Override
        public void setInputStream(@NonNull InputStream stream) {
            this.in = stream;
        }

        @Override
        public IFileHolder getFileHolder() {
            if (null == in) {
                return null;
            }
            return new FileHolder(in, -1, null, null);
        }

    }

}
