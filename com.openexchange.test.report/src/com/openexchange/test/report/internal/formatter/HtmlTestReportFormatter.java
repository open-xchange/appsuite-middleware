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

package com.openexchange.test.report.internal.formatter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.openexchange.test.report.TestReportFormatter;
import com.openexchange.test.report.TestResult;

/**
 * {@link HtmlTestReportFormatter}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class HtmlTestReportFormatter implements TestReportFormatter {

    private static final String type = "html";

    private final Document doc;

    /**
     * Initializes a new {@link HtmlTestReportFormatter}.
     */
    public HtmlTestReportFormatter() {
        super();
        doc = Document.createShell("");

        // Add css
        Element head = doc.select("head").first();
        head.appendElement("link").attr("rel", "stylesheet").attr("type", "text/css").attr("href", "style.css");

        // Add root div
        Element body = doc.select("body").first();
        body.appendElement("div").attr("class", "container");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.in8.test.report.TestReportFormatter#format(com.openexchange.in8.test.report.Result)
     */
    @Override
    public void format(TestResult result) {
        final Element container = doc.getElementsByClass("container").first();

        // Add the test class if not present
        if (container.getElementsByClass("testClass").first() == null) {
            container.appendElement("div").attr("class", "testClass").appendText(result.getTestClass());
        }

        // Check if the result is part of the same test method
        final Element testResults;
        {
            Elements elements = container.select("#" + result.getTestMethod());
            if (elements.size() == 0) {
                Element testCase = container.appendElement("div").attr("class", "testCase").attr("id", result.getTestMethod());
                testCase.appendElement("div").attr("class", "testMethod").appendText(result.getTestMethod());
                testCase.appendElement("div").attr("class", "testDescription").appendText(result.getDescription());
                testResults = testCase.appendElement("div").attr("class", "testResults");
            } else {
                testResults = elements.select("div.testResults").first();
            }
        }

        String assertationStatus = result.isAssertationStatus() ? "passed" : "failed";
        Element request = testResults.appendElement("div").attr("class", "request");
        request.appendElement("div").attr("class", "verb").text(result.getAction());
        request.appendElement("div").attr("class", "nbsp");
        request.appendElement("div").attr("class", "sideHeader").text("Request Parameters:");
        
        Element tableRoot = request.appendElement("div").attr("class", "content").appendElement("div").attr("class", "table");
        for (String key : result.getParameters().keySet()) {
            Element row = tableRoot.appendElement("div").attr("class", "row");
            row.appendElement("div").attr("class", "headerCell").text(key + ":");
            row.appendElement("div").attr("class", "cell").text(result.getParameters().get(key));
        }
        
        request.appendElement("div").attr("class", "nbsp");
        if (result.getBody() != null) {
            request.appendElement("div").attr("class", "sideHeader").text("Request Body:");
            request.appendElement("div").attr("class", "content").text(result.getBody().toString());
            request.appendElement("div").attr("class", "nbsp");
        }
        request.appendElement("div").attr("class", "sideHeader").text("Request Duration:");
        request.appendElement("div").attr("class", "content").text(Long.toString(result.getDuration()) + " ms.");
        request.appendElement("div").attr("class", "nbsp");
        request.appendElement("div").attr("class", "sideHeader").text("Assertation Status:");
        request.appendElement("div").attr("class", assertationStatus).text(assertationStatus);
        request.appendElement("div").attr("class", "nbsp");

        if (!result.isAssertationStatus()) {
            request.appendElement("div").attr("class", "sideHeader").text("Failure Reason:");
            request.appendElement("div").attr("class", "content").text(result.getFailureReason());
            request.appendElement("div").attr("class", "nbsp");
            request.appendElement("div").attr("class", "sideHeader").text("Stacktrace:");
            request.appendElement("div").attr("class", "stacktrace").appendElement("pre").text(result.getStacktrace());
            request.appendElement("div").attr("class", "nbsp");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.in8.test.report.TestReportFormatter#getType()
     */
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getFormattedReport() {
        return doc.toString();
    }
}
