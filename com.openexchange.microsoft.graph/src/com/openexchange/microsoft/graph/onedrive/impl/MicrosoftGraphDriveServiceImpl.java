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

package com.openexchange.microsoft.graph.onedrive.impl;

import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.microsoft.graph.api.MicrosoftGraphOneDriveAPI;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;
import com.openexchange.microsoft.graph.onedrive.exceptions.ErrorCode;
import com.openexchange.microsoft.graph.onedrive.parser.OneDriveFolderParser;
import com.openexchange.rest.client.exception.RESTExceptionCodes;

/**
 * {@link MicrosoftGraphDriveServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphDriveServiceImpl implements MicrosoftGraphDriveService {

    private final MicrosoftGraphOneDriveAPI api;
    private final OneDriveFolderParser folderEntityParser;

    /**
     * Initialises a new {@link MicrosoftGraphDriveServiceImpl}.
     */
    public MicrosoftGraphDriveServiceImpl(MicrosoftGraphOneDriveAPI api) {
        super();
        this.api = api;
        this.folderEntityParser = new OneDriveFolderParser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#existsFolder(java.lang.String, java.lang.String)
     */
    @Override
    public boolean existsFolder(String accessToken, String folderPath) throws OXException {
        try {
            JSONObject response = api.getFolder(accessToken, folderPath);
            return !containsError(response, ErrorCode.itemNotFound);
        } catch (OXException e) {
            if (RESTExceptionCodes.PAGE_NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getRootFolder(java.lang.String)
     */
    @Override
    public OneDriveFolder getRootFolder(String accessToken) throws OXException {
        return folderEntityParser.parseEntity(api.getRoot(accessToken));
    }

    //////////////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * Checks the specified response whether it contains the specified error code
     * 
     * @param response The response
     * @param errorCode The error code
     * @return <code>true</code> if the specified error code is contained</code>;
     *         <code>false</code> if the response is null or empty, or if the error code is not contained.
     */
    private boolean containsError(JSONObject response, ErrorCode errorCode) {
        if (response == null || response.isEmpty()) {
            return true;
        }
        if (!response.hasAndNotNull("error")) {
            return false;
        }
        JSONObject error = response.optJSONObject("error");
        return errorCode.name().equals(error.optString("code"));
    }
}
