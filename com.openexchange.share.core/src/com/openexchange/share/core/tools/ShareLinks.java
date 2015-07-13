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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.share.core.tools;

import org.apache.http.client.utils.URIBuilder;
import com.openexchange.groupware.modules.Module;
import com.openexchange.java.Strings;
import com.openexchange.share.RequestContext;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.core.ShareConstants;


/**
 * Utility class for generating share links.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ShareLinks {

    /**
     * Generates a share link for a guest user based on the passed share token.
     * The token can be a base token or an absolute one.
     *
     * @param context The request context
     * @param shareToken The share token
     * @return The link
     */
    public static String generateExternal(RequestContext context, String shareToken) {
        return prepare(context)
            .setPath(serverPath(context, "/" + shareToken))
            .toString();
    }

    /**
     * Generates a share link for an internal user with a concrete target to jump to.
     *
     * @param context The request context
     * @param target The share target
     * @return The link
     */
    public static String generateInternal(RequestContext context, ShareTarget target) {
        String module = Module.getForFolderConstant(target.getModule()).getName();
        String folder = target.getFolder();
        String item = target.getItem();
        StringBuilder fragment = new StringBuilder(64).append("!&app=io.ox/").append(module).append("&folder=").append(folder);
        if (Strings.isNotEmpty(item)) {
            fragment.append("&item=").append(item);
        }

        return prepare(context)
            .setPath("/appsuite/ui")
            .setFragment(fragment.toString())
            .toString();
    }

    /**
     * Generates the link for confirming a requested password reset.
     *
     * @param context The request context
     * @param baseShareToken The base token of the according share
     * @param confirmToken The confirm token
     * @return The link
     */
    public static String generateConfirmPasswordReset(RequestContext context, String baseShareToken, String confirmToken) {
            return prepare(context)
                .setPath(serverPath(context, "/reset/password"))
                .addParameter("share", baseShareToken)
                .addParameter("confirm", confirmToken)
                .toString();
    }

    private static URIBuilder prepare(RequestContext context) {
        return new URIBuilder()
            .setScheme(context.getProtocol())
            .setHost(context.getHostname());
    }

    private static String serverPath(RequestContext context, String endpoint) {
        return context.getServletPrefix() + ShareConstants.SHARE_SERVLET + endpoint;
    }

}
