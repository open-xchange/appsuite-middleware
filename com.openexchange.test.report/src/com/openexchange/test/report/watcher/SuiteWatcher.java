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

package com.openexchange.test.report.watcher;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.java.Strings;
import com.openexchange.test.report.AssertationResults;
import com.openexchange.test.report.util.FileUtil;

/**
 * {@link SuiteWatcher}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SuiteWatcher extends TestWatcher {

    private final String name;
    private final String formatterId;
    private final String writerId;
    private final String path;

    /**
     * Initializes a new {@link SuiteWatcher}.
     *
     * @param name Suite's name
     */
    public SuiteWatcher(String name, String formatterId, String writerId, String path) {
        this.name = name;
        this.formatterId = formatterId;
        this.writerId = writerId;
        this.path = path;
    }

    @Override
    protected void finished(Description desc) {
        if (formatterId.equals("html") && writerId.equals("file")) {
            System.out.println("Creating index...");
            FileUtil.writeTextToFile(path + "/tests" + name + ".html", createIndex(desc));
            FileUtil.writeTextToFile(path + "/index" + name + ".html", createFrames());
            System.out.println("Suite completed!");
        }
    }

    private String createIndex(Description desc) {
        Document doc = Document.createShell("");

        // Add css
        Element head = doc.select("head").first();
        head.appendElement("link").attr("rel", "stylesheet").attr("type", "text/css").attr("href", "style.css");

        Element index = doc.select("body").first().attr("class", "nav").appendElement("div").attr("class", "index");

        SuiteClasses classes = desc.getAnnotation(SuiteClasses.class);
        for (Class<?> clazz : classes.value()) {
            String simpleName = clazz.getSimpleName();
            String assertStatus = AssertationResults.getInstance().get(clazz.getName());
            if (assertStatus == null) {
                assertStatus = "failed";
            }
            index.appendElement("div").attr("class", assertStatus).attr("id", "link").appendElement("a").attr("id", assertStatus).attr("href", simpleName + ".html").attr("target", "display").text(simpleName);
        }

        return doc.toString();
    }

    private String createFrames() {
        Document doc = Document.createShell("");
        doc.select("body").first().remove();
        Element frameset = doc.select("html").first().appendElement("frameset").attr("cols", "250,*");
        frameset.appendElement("frame").attr("src", "tests" + name + ".html").attr("frameborder", "0");
        frameset.appendElement("frame").attr("src", "").attr("name", "display").attr("frameborder", "0");
        return doc.toString();
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the formatterId
     *
     * @return The formatterId
     */
    public String getFormatterId() {
        return formatterId;
    }

    /**
     * Gets the writerId
     *
     * @return The writerId
     */
    public String getWriterId() {
        return writerId;
    }

    /**
     * Gets the path
     *
     * @return The path
     */
    public String getPath() {
        return path;
    }
}
