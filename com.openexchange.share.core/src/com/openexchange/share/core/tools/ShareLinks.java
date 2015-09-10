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

import static org.slf4j.LoggerFactory.getLogger;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.share.PersonalizedShareTarget;
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
     * Generates an (absolute) share link for a guest user based on the passed share token. If configured, the share link will use the
     * guest hostname as supplied by an installed hostname service, or via the config cascade property
     * <code>com.openexchange.share.guestHostname</code>.
     *
     * @param hostData The host data
     * @param guestToken The guests base token
     * @param target The share target
     * @return The share link
     */
    public static String generateExternal(HostData hostData, String guestToken, ShareTarget target) {
        URIBuilder builder = prepare(hostData);
        String guestHostname = getGuestHostname(guestToken);
        if (false == Strings.isEmpty(guestHostname)) {
            builder.setHost(guestHostname);
        } else {
            getLogger(ShareLinks.class).warn("No hostname for guests is configured. Falling back to current host \"{}\" for share link " +
                "generation. Please configure \"com.openexchange.share.guestHostname\" to make this warning disappear.", builder.getHost());
        }
        String targetPath = "";
        if (target != null) {
            targetPath = "/" + target.getPath();
        }
        return builder.setPath(serverPath(hostData, "/" + guestToken + targetPath)).toString();
    }

    /**
     * Generates a share link for an internal user with a concrete target to jump to.
     *
     * @param hostData The host data
     * @param target The personalized share target according to the user for who the link is generated
     * @return The link
     */
    public static String generateInternal(HostData hostData, PersonalizedShareTarget target) {
        String module = Module.getForFolderConstant(target.getModule()).getName();
        String folder = target.getFolder();
        String item = target.getItem();
        StringBuilder fragment = new StringBuilder(64).append("!&app=io.ox/").append(module).append("&folder=").append(folder);
        if (Strings.isNotEmpty(item)) {
            fragment.append("&id=").append(item);
        }

        return prepare(hostData)
            .setPath("/appsuite/ui")
            .setFragment(fragment.toString())
            .toString();
    }

    /**
     * Generates the link for confirming a requested password reset.
     *
     * @param hostData The host data
     * @param baseShareToken The base token of the according share
     * @param confirmToken The confirm token
     * @return The link
     */
    public static String generateConfirmPasswordReset(HostData hostData, String baseShareToken, String confirmToken) {
            return prepare(hostData)
                .setPath(serverPath(hostData, "/reset/password"))
                .addParameter("share", baseShareToken)
                .addParameter("confirm", confirmToken)
                .toString();
    }

    private static URIBuilder prepare(HostData hostData) {
        return new URIBuilder()
            .setScheme(hostData.isSecure() ? "https" : "http")
            .setHost(hostData.getHost());
    }

    private static String serverPath(HostData hostData, String endpoint) {
        return hostData.getDispatcherPrefix() + ShareConstants.SHARE_SERVLET + endpoint;
    }

    /**
     * Gets the hostname to use for guest users based on a specific share token.
     *
     * @param shareToken The share token to get the guest hostname for
     * @return The guest hostname, or <code>null</code> if not defined
     */
    private static String getGuestHostname(String shareToken) {
        int contextID = -1;
        int userID = -1;
        try {
            ShareToken token = new ShareToken(shareToken);
            contextID = token.getContextID();
            userID = token.getUserID();
        } catch (OXException e) {
            getLogger(ShareLinks.class).error("Error resolving share token {}", shareToken, e);
        }
        return getGuestHostname(contextID, userID);
    }

    /**
     * Gets the hostname to use for guest users based on a specific context and user.
     *
     * @param contextID The identifier of the context to get the guest hostname for, or <code>-1</code> to use the common fallback
     * @param userID The identifier of the user to get the guest hostname for, or <code>-1</code> to use the common fallback
     * @return The guest hostname, or <code>null</code> if not defined
     */
    private static String getGuestHostname(final int contextID, final int userID) {
        /*
         * prefer a guest hostname from dedicated hostname service
         */
        String hostname = null;
        try {
            hostname = ServiceCallWrapper.tryServiceCall(ShareLinks.class, HostnameService.class, new ServiceUser<HostnameService, String>() {

                @Override
                public String call(HostnameService service) throws Exception {
                    return service.getGuestHostname(userID, contextID);
                }
            }, null);
        } catch (ServiceException e) {
            // ignore
        }
        if (null == hostname) {
            /*
             * consult the config cascade as fallback
             */
            try {
                hostname = ServiceCallWrapper.tryServiceCall(ShareLinks.class, ConfigViewFactory.class, new ServiceUser<ConfigViewFactory, String>() {

                    @Override
                    public String call(ConfigViewFactory service) throws Exception {
                        return service.getView(userID, contextID).opt("com.openexchange.share.guestHostname", String.class, null);
                    }
                }, null);
            } catch (ServiceException e) {
                // ignore
            }
        }
        return hostname;
    }

    private static final Map<Character, Character> CHAR_MAPPING = new HashMap<>();
    static {
        CHAR_MAPPING.put(new Character('0'), new Character('j'));
        CHAR_MAPPING.put(new Character('1'), new Character('w'));
        CHAR_MAPPING.put(new Character('2'), new Character('s'));
        CHAR_MAPPING.put(new Character('3'), new Character('7'));
        CHAR_MAPPING.put(new Character('4'), new Character('9'));
        CHAR_MAPPING.put(new Character('5'), new Character('S'));
        CHAR_MAPPING.put(new Character('6'), new Character('k'));
        CHAR_MAPPING.put(new Character('7'), new Character('3'));
        CHAR_MAPPING.put(new Character('8'), new Character('Q'));
        CHAR_MAPPING.put(new Character('9'), new Character('4'));
        CHAR_MAPPING.put(new Character('a'), new Character('m'));
        CHAR_MAPPING.put(new Character('A'), new Character('P'));
        CHAR_MAPPING.put(new Character('B'), new Character('o'));
        CHAR_MAPPING.put(new Character('b'), new Character('V'));
        CHAR_MAPPING.put(new Character('c'), new Character('U'));
        CHAR_MAPPING.put(new Character('C'), new Character('x'));
        CHAR_MAPPING.put(new Character('D'), new Character('l'));
        CHAR_MAPPING.put(new Character('d'), new Character('N'));
        CHAR_MAPPING.put(new Character('E'), new Character('i'));
        CHAR_MAPPING.put(new Character('e'), new Character('Z'));
        CHAR_MAPPING.put(new Character('f'), new Character('g'));
        CHAR_MAPPING.put(new Character('F'), new Character('W'));
        CHAR_MAPPING.put(new Character('g'), new Character('f'));
        CHAR_MAPPING.put(new Character('G'), new Character('I'));
        CHAR_MAPPING.put(new Character('H'), new Character('J'));
        CHAR_MAPPING.put(new Character('h'), new Character('M'));
        CHAR_MAPPING.put(new Character('i'), new Character('E'));
        CHAR_MAPPING.put(new Character('I'), new Character('G'));
        CHAR_MAPPING.put(new Character('j'), new Character('0'));
        CHAR_MAPPING.put(new Character('J'), new Character('H'));
        CHAR_MAPPING.put(new Character('k'), new Character('6'));
        CHAR_MAPPING.put(new Character('K'), new Character('X'));
        CHAR_MAPPING.put(new Character('l'), new Character('D'));
        CHAR_MAPPING.put(new Character('L'), new Character('n'));
        CHAR_MAPPING.put(new Character('m'), new Character('a'));
        CHAR_MAPPING.put(new Character('M'), new Character('h'));
        CHAR_MAPPING.put(new Character('_'), new Character('T'));
        CHAR_MAPPING.put(new Character('-'), new Character('b'));
        CHAR_MAPPING.put(new Character('N'), new Character('d'));
        CHAR_MAPPING.put(new Character('n'), new Character('L'));
        CHAR_MAPPING.put(new Character('o'), new Character('B'));
        CHAR_MAPPING.put(new Character('O'), new Character('R'));
        CHAR_MAPPING.put(new Character('P'), new Character('A'));
        CHAR_MAPPING.put(new Character('p'), new Character('-'));
        CHAR_MAPPING.put(new Character('Q'), new Character('8'));
        CHAR_MAPPING.put(new Character('q'), new Character('v'));
        CHAR_MAPPING.put(new Character('R'), new Character('O'));
        CHAR_MAPPING.put(new Character('r'), new Character('Y'));
        CHAR_MAPPING.put(new Character('s'), new Character('2'));
        CHAR_MAPPING.put(new Character('S'), new Character('5'));
        CHAR_MAPPING.put(new Character('T'), new Character('p'));
        CHAR_MAPPING.put(new Character('t'), new Character('z'));
        CHAR_MAPPING.put(new Character('U'), new Character('c'));
        CHAR_MAPPING.put(new Character('u'), new Character('y'));
        CHAR_MAPPING.put(new Character('V'), new Character('_'));
        CHAR_MAPPING.put(new Character('v'), new Character('q'));
        CHAR_MAPPING.put(new Character('w'), new Character('1'));
        CHAR_MAPPING.put(new Character('W'), new Character('F'));
        CHAR_MAPPING.put(new Character('x'), new Character('C'));
        CHAR_MAPPING.put(new Character('X'), new Character('K'));
        CHAR_MAPPING.put(new Character('Y'), new Character('r'));
        CHAR_MAPPING.put(new Character('y'), new Character('u'));
        CHAR_MAPPING.put(new Character('Z'), new Character('e'));
        CHAR_MAPPING.put(new Character('z'), new Character('t'));
    }


}
