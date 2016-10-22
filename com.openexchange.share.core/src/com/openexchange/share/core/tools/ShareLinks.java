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

package com.openexchange.share.core.tools;

import static org.slf4j.LoggerFactory.getLogger;
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
import com.openexchange.share.Links;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.core.ShareConstants;
import com.openexchange.share.core.exception.ShareCoreExceptionCodes;


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
    public static String generateExternal(HostData hostData, String guestToken, ShareTargetPath targetPath) {
        URIBuilder builder = prepare(hostData);
        String guestHostname = getGuestHostname(guestToken);
        if (false == Strings.isEmpty(guestHostname)) {
            builder.setHost(guestHostname);
        } else {
            getLogger(ShareLinks.class).warn("No hostname for guests is configured. Falling back to current host \"{}\" for share link " +
                "generation. Please configure \"com.openexchange.share.guestHostname\" to make this warning disappear.", builder.getHost());
        }
        String targetPathStr = "";
        if (targetPath != null) {
            targetPathStr = targetPath.get();
        }
        return builder.setPath(serverPath(hostData, "/" + guestToken + targetPathStr)).toString();
    }

    /**
     * Generates a share link for an internal user with a concrete target to jump to.
     *
     * @param hostData The host data
     * @param target The personalized share target according to the user for who the link is generated
     * @return The link
     * @throws OXException in case the link couldn't be generated
     */
    public static String generateInternal(HostData hostData, ShareTarget target) throws OXException {
        Module module = Module.getForFolderConstant(target.getModule());
        if(null==module){
            throw ShareCoreExceptionCodes.UNKOWN_MODULE.create(target.getModule());
        }
        String moduleStr = module.getName();
        String folder = target.getFolder();
        String item = target.getItem();
        return Links.generateInternalLink(moduleStr, folder, item, hostData);
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

}
