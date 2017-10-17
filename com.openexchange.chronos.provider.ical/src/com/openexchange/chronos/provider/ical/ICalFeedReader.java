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

package com.openexchange.chronos.provider.ical;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.provider.ical.exception.ICalProviderExceptionCodes;
import com.openexchange.chronos.provider.ical.internal.Services;
import com.openexchange.chronos.provider.ical.result.GetResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;

/**
 *
 * {@link ICalFeedReader}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ICalFeedReader extends ICalFeedConnector {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalFeedReader.class);

    public ICalFeedReader(Session session, ICalFeedConfig iCalFeedConfig) {
        super(session, iCalFeedConfig);
    }

    private HttpGet prepareGet(String uri) throws OXException {
        HttpGet getMethod = new HttpGet(uri);
        getMethod.addHeader(HttpHeaders.ACCEPT, "text/calendar");
        handleAuth(getMethod);
        return getMethod;
    }

    private ImportedCalendar importCalendar(HttpEntity httpEntity) throws OXException {
        if (null == httpEntity) {
            return null;
        }
        ICalService iCalService = Services.getService(ICalService.class);
        ICalParameters parameters = iCalService.initParameters();
        parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);
        InputStream inputStream = null;
        try {
            inputStream = Streams.bufferedInputStreamFor(httpEntity.getContent());
            return iCalService.importICal(inputStream, parameters);
        } catch (UnsupportedOperationException | IOException e) {
            LOG.error("Error while processing the retrieved information:{}.", e.getMessage(), e);
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
        } finally {
            Streams.close(inputStream);
        }
    }

    protected GetResult get(String uri) throws OXException {
        HttpGet getMethod = null;
        CloseableHttpResponse response = null;
        try {
            getMethod = prepareGet(uri);
            response = httpClient.execute(getMethod);
            GetResult result = new GetResult(response.getStatusLine(), response.getAllHeaders());

            if (result.getStatusCode() >= 200 && result.getStatusCode() < 300) {
                HttpEntity httpEntity = response.getEntity();
                if (null == httpEntity) {
                    return null;
                }
                result.setCalendar(importCalendar(httpEntity));
                return result;
            }
            throw ICalProviderExceptionCodes.UNEXPECTED_FEED_ERROR.create(uri);
        } catch (IOException e) {
            //            throw OAuthExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
            throw OXException.general("", e);
        } finally {
            close(getMethod, response);
            Streams.close(response);
        }
    }
}
