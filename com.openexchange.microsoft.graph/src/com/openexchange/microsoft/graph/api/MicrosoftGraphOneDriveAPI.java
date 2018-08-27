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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.microsoft.graph.api;

import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTClient;
import com.openexchange.microsoft.graph.api.client.MicrosoftGraphRESTEndPoint;

/**
 * {@link MicrosoftGraphOneDriveAPI}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphOneDriveAPI extends AbstractMicrosoftGraphAPI {

    /**
     * Initialises a new {@link MicrosoftGraphOneDriveAPI}.
     * 
     * @param client The rest client
     */
    public MicrosoftGraphOneDriveAPI(MicrosoftGraphRESTClient client) {
        super(client);
    }

    /**
     * Gets the user's OneDrive
     * 
     * @param accessToken OAuth The access token
     * @return A {@link JSONObject} with the user's one drive metadata
     * @throws OXException if an error is occurred
     */
    public JSONObject getDrive(String accessToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath());
    }

    /**
     * Returns the metadata of the root folder for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @return A {@link JSONObject} with the metadata of the user's root folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     */
    public JSONObject getRoot(String accessToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/root");
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's absolute path, e.g. <code>/path/to/folder</code>
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     */
    public JSONObject getRootChildren(String accessToken) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/root/children");
    }

    /**
     * Returns the metadata of the specified folder for a user's default Drive
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's absolute path, e.g. <code>/path/to/folder</code>
     * @return A {@link JSONObject} with the metadata of the user's specified folder
     *         of the default Drive
     * @throws OXException if an error is occurred
     */
    public JSONObject getFolder(String accessToken, String folderPath) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/root:/" + folderPath);
    }

    /**
     * Returns the metadata of all children (files and folders) of the specified folder.
     * 
     * @param accessToken The oauth access token
     * @param folderPath The folder's absolute path, e.g. <code>/path/to/folder</code>
     * @return A {@link JSONObject} with the metadata of all children (files and folders) of the specified folder.
     * @throws OXException if an error is occurred
     */
    public JSONObject getChildren(String accessToken, String folderPath) throws OXException {
        return getResource(accessToken, "/me" + MicrosoftGraphRESTEndPoint.drive.getAbsolutePath() + "/root:/" + folderPath + ":/children");
    }
}
