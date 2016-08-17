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

package com.openexchange.serverconfig;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.capabilities.Capability;


/**
 * {@link ServerConfig} - A simple ServerConfig Object to ease extraction of well known/most used items of the dynamically generated
 * ServerConfig Map. You can inspect all items via {@link ServerConfig#asMap()}.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public interface ServerConfig {

    /**
     * Get the whole config object as map, might contain more entries than available via documented methods as the object is built
     * dynamically.
     *
     * @return The whole config object as map
     */
    Map<String, Object> asMap();

    /**
     * Get the set of {@link Capability}s available for the {@link User} you requested the {@link ServerConfig} for
     * @return the set of {@link Capability}s
     */
    Set<Capability> getCapabilities();

    /**
     * Check if the server is forced to assume https connections
     * @return true if the server is forced to assume https connections, else false
     */
    boolean isForceHttps();

    /**
     * Get the hosts configuration
     * @return An array of hosts
     */
    String[] getHosts();

    /**
     * Get a list of known languages as key -> value pairs e.g. "en_GB" -> "English (UK)"
     *
     * @return The list of languages
     */
    List<SimpleEntry<String, String>> getLanguages();

    /**
     * Get the server version
     *
     * @return the server version
     */
    String getServerVersion();

    /**
     * Get the server build date
     *
     * @return the server build date
     */
    String getServerBuildDate();

    /**
     * Get the ui version
     *
     * @return the ui version
     */
    String getUIVersion();

    /**
     * Get the product name
     *
     * @return the product name
     */
    String getProductName();

    /**
     * Get a filtered config object as map. Not all ServerConfig entries are needed for the client. {@link ClientServerConfigFilter}s can
     * be registered to influence the filtering process.
     *
     * @return The filtered config object as map
     */
    Map<String, Object> forClient();

    /**
     * Get the style configuration for notification mails
     *
     * @return The configuration
     */
    NotificationMailConfig getNotificationMailConfig();
}
