/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.core.tools;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.share.core.ShareConstants.SHARE_SERVLET;
import static org.slf4j.LoggerFactory.getLogger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
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
import com.openexchange.share.ShareExceptionCodes;
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

    /** Start of "app"-fragment, see also {@link com.openexchange.share.Links.FRAGMENT_APP} */
    private static final String IO_OX = "io.ox/";

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
        if (Strings.isNotEmpty(guestHostname)) {
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
        if (null == module) {
            throw ShareCoreExceptionCodes.UNKOWN_MODULE.create(I(target.getModule()));
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
        URIBuilder builder = prepare(hostData);
        String guestHostname = getGuestHostname(baseShareToken);
        if (Strings.isNotEmpty(guestHostname)) {
            builder.setHost(guestHostname);
        } else {
            getLogger(ShareLinks.class).warn("No hostname for guests is configured. Falling back to current host \"{}\" for share link " +
                "generation. Please configure \"com.openexchange.share.guestHostname\" to make this warning disappear.", builder.getHost());
        }
        return builder
            .setPath(serverPath(hostData, "/reset/password"))
            .addParameter("share", baseShareToken)
            .addParameter("confirm", confirmToken)
        .toString();
    }

    /**
     * Extracts the share base token from a share URL.
     *
     * @param shareUrl The path to extract the token from
     * @return The token or <code>null</code> if no token is embedded in the path
     */
    public static String extractBaseToken(String shareUrl) {
        if (Strings.isEmpty(shareUrl)) {
            return null;
        }
        URI uri;
        try {
            uri = new URI(shareUrl);
        } catch (URISyntaxException e) {
            return null;
        }
        String path = uri.getPath();
        if (Strings.isEmpty(path)) {
            return null;
        }
        String prefix = SHARE_SERVLET + '/';
        int beginIndex = path.lastIndexOf(prefix);
        if (-1 == beginIndex) {
            return null;
        }
        beginIndex += prefix.length();
        int endIndex = path.indexOf('/', beginIndex);
        return -1 == endIndex ? path.substring(beginIndex) : path.substring(beginIndex, endIndex);
    }

    /**
     * Extracts the hostname part of a share link.
     * 
     * @param shareLink The share link to get the hostname for
     * @return The hostname, fallink back to the passed link as-is if the hostname cannot be extracted
     */
    public static String extractHostName(String shareLink) {
        String hostname = null;
        try {
            hostname = new URI(shareLink).getHost();
        } catch (URISyntaxException e) {
            getLogger(ShareLinks.class).warn("Error extracting host name from share link {}", shareLink, e);
        }
        return Strings.isNotEmpty(hostname) ? hostname : shareLink;
    }

    /**
     * Extracts the hostdata-relevant parts of a share link and makes them available as {@link HostData}.
     * 
     * @param shareLink The share link to extract the hostdata from
     * @return The host data
     */
    public static HostData extractHostData(String shareLink) throws OXException {
        URI uri;
        try {
            uri = new URI(shareLink);
        } catch (URISyntaxException e) {
            throw ShareExceptionCodes.INVALID_LINK.create(e, shareLink);
        }
        return new HostData() {

            @Override
            public boolean isSecure() {
                return "https".equals(uri.getScheme());
            }

            @Override
            public String getRoute() {
                return null;
            }

            @Override
            public int getPort() {
                return uri.getPort();
            }

            @Override
            public String getHost() {
                return uri.getHost();
            }

            @Override
            public String getHTTPSession() {
                return null;
            }

            @Override
            public String getDispatcherPrefix() {
                String path = uri.getPath();
                if (null != path) {
                    int idx = path.indexOf(SHARE_SERVLET + '/');
                    if (-1 != idx) {
                        return path.substring(0, idx);
                    }
                }
                return "/appsuite/api/";
            }
        };
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

    /**
     * Parsed an internal share link and extracts the internal share target the link points to.
     * <p>
     * See also {@link com.openexchange.share.Links#generateInternalLink(String, String, String, HostData)}
     * 
     * @param shareLink The share link to parse
     * @return The share target or <code>null</code> if the link is not an internal share link
     */
    public static ShareTarget parseInternal(String shareLink) {
        /*
         * Parse fragment
         */
        List<NameValuePair> fragments;
        try {
            String fragment = new URIBuilder(shareLink).getFragment();
            if (Strings.isEmpty(fragment)) {
                return null;
            }
            fragments = URLEncodedUtils.parse(fragment, Charset.forName("UTF-8"));
        } catch (URISyntaxException e) {
            getLogger(ShareLinks.class).debug("Unable to parse link {}", shareLink, e);
            return null;
        }
        /*
         * Translate to share target
         */
        Module module = null;
        String folder = null;
        String item = null;
        for (NameValuePair pair : fragments) {
            if (Strings.isEmpty(pair.getName()) || Strings.isEmpty(pair.getValue())) {
                continue;
            }
            switch (pair.getName()) {
                case "folder":
                    folder = pair.getValue();
                    break;
                case "id":
                    item = pair.getValue();
                    break;
                case "app":
                    String app = pair.getValue().startsWith(IO_OX) ? pair.getValue().substring(IO_OX.length()) : pair.getValue();
                    module = Module.getForName(app);
                    break;
                default:
                    break;
            }
        }
        if (null == module || Strings.isEmpty(folder)) {
            return null;
        }
        return new ShareTarget(module.getFolderConstant(), folder, item);
    }

}
