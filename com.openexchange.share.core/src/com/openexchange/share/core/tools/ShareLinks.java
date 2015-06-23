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
     * Generates a share link for a guest user with only the base token set.
     *
     * @param context The request context
     * @param baseToken The guest users base token
     * @return The link
     */
    public static String generateExternal(RequestContext context, String baseToken) {
        return generateExternal0(context, baseToken, null);
    }

    /**
     * Generates a share link for a guest user with a concrete share target path.
     *
     * @param context The request context
     * @param baseToken The guest users base token
     * @param targetPath The share targets path
     * @return The link
     */
    public static String generateExternal(RequestContext context, String baseToken, String targetPath) {
        return generateExternal0(context, baseToken, targetPath);
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
        StringBuilder stringBuilder = prepare(context)
            .append("/appsuite/ui#!!&app=io.ox/")
            .append(module)
            .append("&folder=")
            .append(folder);

        if (Strings.isNotEmpty(item)) {
            stringBuilder.append("&item=").append(item);
        }

        return stringBuilder.toString();
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
        StringBuilder stringBuilder = prepare(context)
            .append(context.getServletPrefix())
            .append(ShareConstants.SHARE_SERVLET)
            .append("/reset/password?")
            .append("share=")
            .append(baseShareToken)
            .append("&confirm=")
            .append(confirmToken);


        return stringBuilder.toString();
    }

    private static String generateExternal0(RequestContext context, String baseToken, String targetPath) {
        StringBuilder stringBuilder = prepare(context)
            .append(context.getServletPrefix())
            .append(ShareConstants.SHARE_SERVLET).append('/')
            .append(baseToken);

        if (null != targetPath) {
            stringBuilder.append('/').append(targetPath);
        }
        return stringBuilder.toString();
    }

    private static StringBuilder prepare(RequestContext context) {
        return new StringBuilder()
            .append(context.getProtocol())
            .append("://")
            .append(context.getHostname());
    }

}
