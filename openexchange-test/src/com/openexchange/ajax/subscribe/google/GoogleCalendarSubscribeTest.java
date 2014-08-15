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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.subscribe.google;

import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.xml.sax.SAXException;
import com.meterware.httpunit.ClientProperties;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.subscribe.test.AbstractSubscriptionTest;


/**
 * {@link GoogleCalendarSubscribeTest}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.6.1
 */
public class GoogleCalendarSubscribeTest extends AbstractSubscriptionTest {

    /**
     * Initializes a new {@link GoogleCalendarSubscribeTest}.
     * @param name
     */
    public GoogleCalendarSubscribeTest(String name) {
        super(name);
    }

    public void testGoogleCalendar() throws IOException, SAXException, InterruptedException {
        HttpUnitOptions.setScriptingEnabled(true);
        WebConversation wc = new WebConversation();
        ClientProperties cp = wc.getClientProperties();
        HttpUnitOptions.setJavaScriptOptimizationLevel(-2);
        cp.setUserAgent("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0");
        WebRequest     req = new GetMethodWebRequest("");
        WebResponse   resp = wc.getResponse( req );
        WebForm form1 = resp.getForms()[0];
        form1.setParameter("Email", "");
        form1.setParameter("Passwd", "");
        WebResponse resp2 = form1.submit();
        WebForm form2 = resp2.getForms()[0];
        form2.getButtonWithID("submit_approve_access").removeAttribute("disabled");;
        form2.getButtonWithID("submit_approve_access").click();
        WebResponse resp3 = form2.submit();
    }

    private String authUrl() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }

}
