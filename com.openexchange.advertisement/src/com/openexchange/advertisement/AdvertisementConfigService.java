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

package com.openexchange.advertisement;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link AdvertisementConfigService} - The service to manage advertisement configurations for users and/or resellers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public interface AdvertisementConfigService {

    public static final String CONFIG_PREFIX = "com.openexchange.advertisement.";

    /**
     * Checks if an advertisement configuration is available for the given user
     *
     * @param session The user session
     * @return <code>true</code> if such an advertisement configuration is available; otherwise <code>false</code>
     */
    public boolean isAvailable(Session session);

    /**
     * Retrieves the advertisement configuration for a given user
     *
     * @param session The user session
     * @return The JSON representation for the configuration
     * @throws OXException If configuration cannot be returned
     */
    public JSONObject getConfig(Session session) throws OXException;

    /**
     * Sets an advertisement configuration for a given user by name. This is for testing purpose only.
     * <p>
     * Setting the configuration parameter to null will delete the current configuration for the user.
     *
     * @param name The login name of the user
     * @param ctxId The context identifier
     * @param config The advertisement configuration
     * @throws OXException If advertisement configuration cannot be set
     */
    public void setConfigByName(String name, int ctxId, String config) throws OXException;

    /**
     * Sets an advertisement configuration for a given user. This is for testing purpose only.
     * <p>
     * Setting the configuration parameter to <code>null</code> will delete the current configuration for the user.
     *
     * @param userId The user identifier
     * @param ctxId The context identifier
     * @param config The advertisement configuration
     * @throws OXException If advertisement configuration cannot be set
     */
    public void setConfig(int userId, int ctxId, String config) throws OXException;

    /**
     * Sets an advertisement configuration for a given package of a given reseller.
     * <p>
     * Setting the configuration parameter to <code>null</code> will delete the current configuration.
     *
     * @param reseller The reseller name
     * @param pack The package name
     * @param config The advertisement configuration
     * @throws OXException If advertisement configuration cannot be set
     */
    public void setConfig(String reseller, String pack, String config) throws OXException;

    /**
     * Sets all advertisement configurations for a given reseller.
     * <p>
     * The configs String must contain a JSONArray of JSONObjects with the following structure:
     * <p>
     * {<br>
     * "package": "package1",<br>
     * "config": "configdata..."<br>
     * }<br>
     * <p>
     * 
     * Setting the configuration parameter to <code>null</code> will delete the current configuration for the reseller.
     *
     * @param reseller The reseller name
     * @param config An array of package informations and advertisement configurations
     * @throws OXException If advertisement configuration cannot be set
     */
    public List<ConfigResult> setConfig(String reseller, String configs) throws OXException;

    /**
     * Retrieves the package scheme identifier for this configuration service.
     *
     * @return The package scheme identifier
     */
    public String getSchemeId();

}
