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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
package com.openexchange.webdav.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.config.ConfigurationService;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class WebdavWhiteList {
    private static WebdavWhiteList INSTANCE = new WebdavWhiteList();
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(WebdavWhiteList.class));


    private List<Pattern> patterns = Collections.emptyList();

    public static WebdavWhiteList getInstance() {
        return INSTANCE;
    }

    public void init(final ConfigurationService config) {
        final String[] clientNames = config.getProperty("com.openexchange.webdav.whitelist.clients", "").split("\\s*,\\s*");

        patterns = new ArrayList<Pattern>(clientNames.length);

        for(final String clientName : clientNames) {
            final String key = "com.openexchange.webdav.whitelist." + clientName;
            final String patternString = config.getProperty(key);
            LOG.debug(key+" : "+patternString);

            try {
                if(patternString != null) {
                    final Pattern pattern = Pattern.compile(patternString);
                    patterns.add(pattern);
                }
            } catch (final PatternSyntaxException x) {
                LOG.error("Invalid pattern in "+key+": "+patternString);
            }

        }

    }

    public boolean acceptClient(final HttpServletRequest req) {
        final String client = req.getHeader("User-Agent");
        if(client == null) {
            LOG.debug("No user agent set, so assuming the client is not on the whitelist.");
            return false;
        }

        for(final Pattern pattern : patterns) {
            final boolean result = pattern.matcher(client).find();
            if(LOG.isDebugEnabled()) {
                LOG.debug("Does the pattern "+pattern+" apply to user agent "+client+"? "+result);
            }
            if(result) {
                LOG.debug("Client "+client+" is accepted by the whitelist.");
                return true;
            }
        }
        LOG.debug("User-Agent "+client+" didn't match any entry in our whitelist.");
        return false;
    }
}
