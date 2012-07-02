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

package com.openexchange.ajax.login;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Charsets;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.encoding.Base64;

/**
 * {@link HashCalculator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class HashCalculator {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(HashCalculator.class));

    private static final Pattern PATTERN_SPLIT = Pattern.compile("\\s*,\\s*");

    private static final Pattern PATTERN_NON_WORD_CHAR = Pattern.compile("\\W");

    public static String getHash(final HttpServletRequest req, final String client) {
        return getHash(req, getUserAgent(req), client);
    }

    public static String getHash(final HttpServletRequest req, final String userAgent, final String client) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update((null == userAgent ? parseUserAgent(req, "") : userAgent).getBytes(Charsets.UTF_8));
            if (null != client) {
                md.update(client.getBytes(Charsets.UTF_8));
            }
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            final String fieldList = null == service ? "" : service.getProperty("com.openexchange.cookie.hash.fields", "");
            final String[] fields = PATTERN_SPLIT.split(fieldList, 0);
            for (final String field : fields) {
                final String header = req.getHeader(field);
                if (header != null) {
                    md.update(header.getBytes(Charsets.UTF_8));
                }
            }
            return PATTERN_NON_WORD_CHAR.matcher(Base64.encode(md.digest())).replaceAll("");
        } catch (final NoSuchAlgorithmException e) {
            LOG.fatal(e.getMessage(), e);
        }
        return "";
    }

    public static String getHash(final HttpServletRequest req) {
        return getHash(req, getClient(req));
    }

    public static String getClient(final HttpServletRequest req) {
        final String parameter = req.getParameter(LoginFields.CLIENT_PARAM);
        if (parameter == null) {
            return "default";
        }
        return parameter;
    }

    private static String parseUserAgent(final HttpServletRequest req, final String defaultValue) {
        final String parameter = req.getParameter(LoginFields.USER_AGENT);
        return null == parameter ? defaultValue : parameter;
    }

    private static String getUserAgent(final HttpServletRequest req) {
        final String header = req.getHeader(Header.USER_AGENT);
        if (header == null) {
            return "";
        }
        return header;
    }
}
