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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.webdav;

import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.common.base.Objects;
import com.openexchange.file.storage.Quota;
import com.openexchange.java.Strings;
import com.openexchange.webdav.client.WebDAVResource;

/**
 * {@link WebDAVUtils}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.4
 */
public class WebDAVUtils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebDAVUtils.class);

    private WebDAVUtils() {
        super();
    }

    /**
     * Looks up a specific WebDAV resource by its path.
     *
     * @param resources The resources to search
     * @param path The path to search for
     * @return The matching resource, or <code>null</code> if not found
     */
    public static WebDAVResource find(Collection<WebDAVResource> resources, URI path) {
        if (null != resources) {
            for (WebDAVResource resource : resources) {
                if (matches(resource.getHref(), path)) {
                    return resource;
                }
            }
        }
        return null;
    }

    /**
     * Looks up a specific WebDAV resource by its path.
     *
     * @param resources The resources to search
     * @param path The path to search for
     * @return The matching resource, or <code>null</code> if not found
     */
    public static WebDAVResource find(Collection<WebDAVResource> resources, WebDAVPath path) {
        return find(resources, path.toURI());
    }

    /**
     * Gets a value indicating whether a relative or absolute href value matches a specific URI.
     *
     * @param href The href to check
     * @param path The path to match the href against
     * @return <code>true</code> of the relative paths of both arguments are equal, <code>false</code>, otherwise
     */
    public static boolean matches(String href, URI path) {
        if (null != path) {
            String relativePath = path.getPath();
            if (null != relativePath) {
                try {
                    String hrefPath = URI.create(href).getPath();
                    return relativePath.equals(hrefPath);
                } catch (RuntimeException e) {
                    LOG.warn("Unable to instantiate an URI from {}", href, e);
                }
            }
        }
        return false;
    }

    /**
     * Gets a value indicating whether a relative or absolute href value matches a specific WebDAV path.
     *
     * @param href The href to check
     * @param path The path to match the href against
     * @return <code>true</code> of the relative paths of both arguments are equal, <code>false</code>, otherwise
     */
    public static boolean matches(String href, WebDAVPath path) {
        return null != path ? matches(href, path.toURI()) : false;
    }

    public static long extractQuotaBytes(Element quotaBytesElement) {
        if (null != quotaBytesElement && null != quotaBytesElement.getTextContent()) {
            try {
                long value = Long.parseLong(quotaBytesElement.getTextContent());
                if (0 <= value) {
                    return value;
                }
            } catch (Exception e) {
                LOG.warn("Error parsing {}, falling back to 'unlimited'", quotaBytesElement.getTextContent(), e);
            }
        }
        return Quota.UNLIMITED;
    }

    public static Date extractLockedUntil(Element lockDiscoveryElement) {
        if (null != lockDiscoveryElement) {
            NodeList timeoutElements = lockDiscoveryElement.getElementsByTagNameNS("DAV:", "timeout");
            if (null != timeoutElements && 0 < timeoutElements.getLength()) {
                String timeout = timeoutElements.item(0).getTextContent();
                if (Strings.isNotEmpty(timeout)) {
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    if ("Infinite".equalsIgnoreCase(timeout)) {
                        calendar.add(Calendar.YEAR, 10);
                    } else if (timeout.startsWith("Second-")) {
                        calendar.add(Calendar.SECOND, Integer.parseInt(timeout.substring(7)));
                    }
                    return calendar.getTime();
                }
            }
        }
        return null;
    }

    public static String extractLockToken(Element lockDiscoveryElement) {
        if (null != lockDiscoveryElement) {
            NodeList locktokenElements = lockDiscoveryElement.getElementsByTagNameNS("DAV:", "locktoken");
            if (null != locktokenElements && 0 < locktokenElements.getLength()) {
                Node hrefNode = getFirstChild(locktokenElements.item(0), "DAV:", "href");
                if (null != hrefNode) {
                    return hrefNode.getTextContent();
                }
            }
        }
        return null;
    }

    private static Node getFirstChild(Node node, String namespaceURI, String localName) {
        if (null != node) {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = node.getChildNodes().item(i);
                if (Objects.equal(namespaceURI, item.getNamespaceURI()) && Objects.equal(localName, item.getLocalName())) {
                    return item;
                }
            }
        }
        return null;
    }

}
