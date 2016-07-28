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

package com.openexchange.filestore.s3.internal;

import java.util.Map;
import com.amazonaws.Request;
import com.amazonaws.Response;
import com.amazonaws.handlers.RequestHandler2;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * {@link ETagCorrectionHandler}
 *
 * Request handler to correct misspelled ETags in responses.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class ETagCorrectionHandler extends RequestHandler2 {

    private static final ETagCorrectionHandler INSTANCE = new ETagCorrectionHandler();

    /**
     * Gets the handler instance to correct misspelled ETag headers in responses.
     *
     * @return The handler
     */
    static ETagCorrectionHandler getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link ETagCorrectionHandler}.
     */
    private ETagCorrectionHandler() {
        super();
    }

    @Override
    public void beforeRequest(Request<?> request) {
        // nothing to do
    }

    @Override
    public void afterResponse(Request<?> request, Response<?> response) {
        Object awsResponse = response.getAwsResponse();
        if (null != awsResponse && ObjectMetadata.class.isInstance(awsResponse)) {
            ObjectMetadata metadata = (ObjectMetadata) awsResponse;
            Map<String, Object> headers = metadata.getRawMetadata();
            if (null != headers && headers.containsKey("Etag")) {
                String etag = (String) headers.get("Etag");
                if (null != etag) {
                    metadata.setHeader(Headers.ETAG, etag.replace("\"", ""));
                }
            }
        }
    }

    @Override
    public void afterError(Request<?> request, Response<?> response, Exception e) {
        // nothing to do
    }

}
