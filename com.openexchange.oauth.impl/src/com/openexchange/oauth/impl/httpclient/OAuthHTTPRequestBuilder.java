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

package com.openexchange.oauth.impl.httpclient;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPDeleteRequestBuilder;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPGetRequestBuilder;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPPostRequestBuilder;
import com.openexchange.oauth.impl.httpclient.impl.scribe.ScribeHTTPPutRequestBuilder;

public class OAuthHTTPRequestBuilder implements HTTPRequestBuilder {

    private final OAuthHTTPClient client;

    public OAuthHTTPRequestBuilder(OAuthHTTPClient client) {
        this.client = client;
    }

    @Override
    public HTTPPutRequestBuilder put() {
        return new ScribeHTTPPutRequestBuilder(this);
    }

    @Override
    public HTTPPostRequestBuilder post() {
        return new ScribeHTTPPostRequestBuilder(this);
    }

    @Override
    public HTTPMultipartPostRequestBuilder multipartPost() {
        throw new UnsupportedOperationException();
        // return new ScribeHTTPMultipartPostRequestBuilder(this);
    }

    @Override
    public HTTPGetRequestBuilder get() {
        return new ScribeHTTPGetRequestBuilder(this);
    }

    @Override
    public HTTPDeleteRequestBuilder delete() {
        return new ScribeHTTPDeleteRequestBuilder(this);
    }

    public HTTPRequestBuilder getBuilder() {
        return client.getBuilder();
    }

    public OAuthAccount getAccount() {
        return client.getAccount();
    }

    public API getApi() {
        return client.getApi();
    }

    public String getApiKey() {
        return client.getApiKey();
    }

    public String getSecret() {
        return client.getSecret();
    }

}
