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

package com.openexchange.dav.mixins;

import static com.openexchange.dav.DAVTools.getExternalPath;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link PrincipalURL}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class PrincipalURL extends SingleXMLPropertyMixin {

    /**
     * Gets the principal URL value.
     *
     * @param userID The identifier of the user to get the principal URL for
     * @param configViewFactory The configuration view
     * @return The principal URL
     */
    public static String forUser(int userID, ConfigViewFactory configViewFactory) {
        return getExternalPath(configViewFactory, "/principals/users/" + userID);
    }

    /**
     * Gets the principal URL value.
     *
     * @param groupID The identifier of the group to get the principal URL for
     * @param configViewFactory The configuration view
     * @return The principal URL
     */
    public static String forGroup(int groupID, ConfigViewFactory configViewFactory) {
        return getExternalPath(configViewFactory, "/principals/groups/" + groupID);
    }

    /**
     * Gets the principal URL value.
     *
     * @param resourceID The identifier of the resource to get the principal URL for
     * @param configViewFactory The configuration view
     * @return The principal URL
     */
    public static String forResource(int resourceID, ConfigViewFactory configViewFactory) {
        return getExternalPath(configViewFactory, "/principals/resources/" + resourceID);
    }

    /**
     * Extracts the calendar user type along with the principal identifier from the supplied string representation of a principal URL.
     *
     * @param principalURL The principal URL to parse
     * @param configViewFactory The configuration view
     * @return The parsed principal URL, or <code>null</code> if the URL couldn't be parsed
     */
    public static PrincipalURL parse(String principalURL, ConfigViewFactory configViewFactory) {
        if (null != principalURL) {
            if (false == principalURL.startsWith("/")) {
                try {
                    principalURL = new URI(principalURL).getPath();
                } catch (URISyntaxException e) {
                    // ignore
                }
                if (null == principalURL) {
                    return null;
                }
            }
            String path = getExternalPath(configViewFactory, "/principals/");
            if (principalURL.startsWith(path)) {
                principalURL = principalURL.substring(path.length());
            }
            Matcher matcher = URL_PATTERN.matcher(principalURL);
            if (matcher.find() && 2 == matcher.groupCount()) {
                try {
                    switch (matcher.group(1)) {
                        case "resources":
                            return new PrincipalURL(Integer.parseInt(matcher.group(2)), CalendarUserType.RESOURCE, configViewFactory);
                        case "groups":
                            return new PrincipalURL(Integer.parseInt(matcher.group(2)), CalendarUserType.GROUP, configViewFactory);
                        case "users":
                            return new PrincipalURL(Integer.parseInt(matcher.group(2)), CalendarUserType.INDIVIDUAL, configViewFactory);
                        default:
                            throw new IllegalArgumentException(matcher.group(1));
                    }
                } catch (IllegalArgumentException e) {
                    org.slf4j.LoggerFactory.getLogger(PrincipalURL.class).debug("Error parsing principal URL", e);
                }
            }
        }
        return null;
    }

    private static final Pattern URL_PATTERN = Pattern.compile("(resources|users|groups)/(\\d+)/?");
    private static final String PROPERTY_NAME = "principal-URL";

    private final int principalID;
    private final CalendarUserType type;

    private final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link PrincipalURL}.
     *
     * @param principalID The identifier of the principal
     * @param type The calendar user type of the principal
     * @param configViewFactory The configuration view
     */
    public PrincipalURL(int principalID, CalendarUserType type, ConfigViewFactory configViewFactory) {
        super(Protocol.DAV_NS.getURI(), PROPERTY_NAME);
        this.principalID = principalID;
        this.type = type;
        this.configViewFactory = configViewFactory;
    }

    /**
     * Gets identifier of the principal
     *
     * @return The identifier of the principal
     */
    public int getPrincipalID() {
        return principalID;
    }

    /**
     * Gets the calendar user type of the principal
     *
     * @return The calendar user type of the principal
     */
    public CalendarUserType getType() {
        return type;
    }

    @Override
    protected String getValue() {
        String url;
        if (CalendarUserType.INDIVIDUAL.equals(type)) {
            url = forUser(principalID, configViewFactory);
        } else if (CalendarUserType.GROUP.equals(type)) {
            url = forGroup(principalID, configViewFactory);
        } else if (CalendarUserType.RESOURCE.equals(type)) {
            url = forResource(principalID, configViewFactory);
        } else {
            throw new IllegalArgumentException(type.toString());
        }
        return "<D:href>" + url + "</D:href>";
    }

}
