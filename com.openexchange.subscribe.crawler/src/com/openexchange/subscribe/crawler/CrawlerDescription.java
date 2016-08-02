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

package com.openexchange.subscribe.crawler;

import java.util.List;
import org.yaml.snakeyaml.Yaml;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.crawler.internal.Step;

/**
 * This Class holds all information that defines a crawler and is the starting point to create one.
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class CrawlerDescription {

    private String displayName, id, workflowString;

    private int priority = 0;

    // set the default API Version to 614 as all crawlers that do not need new functionality should work with 614
    private int crawlerApiVersion = 614;

    // set the default module to CONTACT to be compatible with API-Version 614 where CONTACT is the only option
    private int module = FolderObject.CONTACT;

    private boolean javascriptEnabled = false;

    private boolean mobileUserAgentEnabled = false;

    private boolean quirkyCookieQuotes;

    public CrawlerDescription() {
        super();
    }

    public void finishUp (List<Step<?, ?>> steps){
        Workflow workflow = new Workflow(steps);
        if (mobileUserAgentEnabled) {
            workflow.setMobileUserAgent(true);
        }
        if (javascriptEnabled) {
            workflow.setEnableJavascript(true);
        }
        if (quirkyCookieQuotes) {
            workflow.setQuirkyCookieQuotes(true);
        }
        Yaml yaml = new Yaml();
        this.setWorkflowString(yaml.dump(workflow));
    }


    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getWorkflowString() {
        return workflowString;
    }

    public void setWorkflowString(final String workflowString) {
        this.workflowString = workflowString;
    }


    public int getPriority() {
        return priority;
    }


    public void setPriority(final int priority) {
        this.priority = priority;
    }


    public int getCrawlerApiVersion() {
        return crawlerApiVersion;
    }


    public void setCrawlerApiVersion(int crawlerApiVersion) {
        this.crawlerApiVersion = crawlerApiVersion;
    }


    public int getModule() {
        return module;
    }


    public void setModule(int module) {
        this.module = module;
    }


    public boolean isJavascriptEnabled() {
        return javascriptEnabled;
    }


    public void setJavascriptEnabled(boolean javascriptEnabled) {
        this.javascriptEnabled = javascriptEnabled;
    }


    public boolean isMobileUserAgentEnabled() {
        return mobileUserAgentEnabled;
    }


    public void setMobileUserAgentEnabled(boolean mobileUserAgentEnabled) {
        this.mobileUserAgentEnabled = mobileUserAgentEnabled;
    }


    public boolean isQuirkyCookieQuotes() {
        return quirkyCookieQuotes;
    }


    public void setQuirkyCookieQuotes(boolean quirkyCookieQuotes) {
        this.quirkyCookieQuotes = quirkyCookieQuotes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(256);
        builder.append("CrawlerDescription [");
        if (displayName != null) {
            builder.append("displayName=").append(displayName).append(", ");
        }
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (workflowString != null) {
            builder.append("workflowString=").append(workflowString).append(", ");
        }
        builder.append("priority=").append(priority).append(", crawlerApiVersion=").append(crawlerApiVersion).append(", module=").append(module).append(", javascriptEnabled=").append(javascriptEnabled).append(", mobileUserAgentEnabled=").append(mobileUserAgentEnabled).append(", quirkyCookieQuotes=").append(quirkyCookieQuotes).append("]");
        return builder.toString();
    }

}
