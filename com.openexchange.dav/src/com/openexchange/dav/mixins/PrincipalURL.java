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
            }
            String path = getExternalPath(configViewFactory, "/principals/");
            if (principalURL.startsWith(path)) {
                // Compile pattern each time, services/configuration might not be ready when building pattern statically.
                Pattern url_pattern = Pattern.compile(path + "(resources|users|groups)/(\\d+)/?");
                Matcher matcher = url_pattern.matcher(principalURL);
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
        }
        return null;
    }

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
