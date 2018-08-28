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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.api.MicrosoftGraphOneDriveAPI;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.microsoft.graph.onedrive.OneDriveFile;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;
import com.openexchange.microsoft.graph.onedrive.exceptions.ErrorCode;
import com.openexchange.microsoft.graph.onedrive.parser.OneDriveFileParser;
import com.openexchange.microsoft.graph.onedrive.parser.OneDriveFolderParser;
import com.openexchange.rest.client.exception.RESTExceptionCodes;

/**
 * {@link MicrosoftGraphDriveServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphDriveServiceImpl implements MicrosoftGraphDriveService {

    private static final Logger LOG = LoggerFactory.getLogger(MicrosoftGraphDriveServiceImpl.class);

    private final MicrosoftGraphOneDriveAPI api;
    private final OneDriveFolderParser folderEntityParser;
    private final OneDriveFileParser fileEntityParser;

    /**
     * Initialises a new {@link MicrosoftGraphDriveServiceImpl}.
     */
    public MicrosoftGraphDriveServiceImpl(MicrosoftGraphOneDriveAPI api) {
        super();
        this.api = api;
        this.folderEntityParser = new OneDriveFolderParser();
        this.fileEntityParser = new OneDriveFileParser();
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
    public OneDriveFolder getRootFolder(int userId, String accessToken) throws OXException {
        return folderEntityParser.parseEntity(userId, api.getRoot(accessToken));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFolder(int, java.lang.String, java.lang.String)
     */
    @Override
    public OneDriveFolder getFolder(int userId, String accessToken, String folderId) throws OXException {
        return folderEntityParser.parseEntity(userId, api.getFolder(accessToken, folderId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getSubFolders(java.lang.String, java.lang.String)
     */
    @Override
    public List<OneDriveFolder> getSubFolders(int userId, String accessToken, String folderId) throws OXException {
        int offset = 100;
        String skipToken = null;
        List<OneDriveFolder> list = new LinkedList<>();
        do {
            JSONObject response = api.getChildren(accessToken, folderId, offset, skipToken);
            skipToken = extractSkipToken(response);
            list.addAll(folderEntityParser.parseEntities(userId, response.optJSONArray("value")));
        } while (Strings.isNotEmpty(skipToken));
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#createFolder(int, java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public OneDriveFolder createFolder(int userId, String accessToken, String folderName, String parentId, boolean autorename) throws OXException {
        return folderEntityParser.parseEntity(userId, api.createFolder(accessToken, folderName, parentId, autorename));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFiles(int, java.lang.String, java.lang.String)
     */
    @Override
    public List<OneDriveFile> getFiles(int userId, String accessToken, String folderId) throws OXException {
        int offset = 100;
        String skipToken = null;
        List<OneDriveFile> list = new LinkedList<>();
        do {
            JSONObject response = api.getChildren(accessToken, folderId, offset, skipToken);
            skipToken = extractSkipToken(response);
            list.addAll(fileEntityParser.parseEntities(userId, response.optJSONArray("value")));
        } while (Strings.isNotEmpty(skipToken));
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFile(int, java.lang.String, java.lang.String)
     */
    @Override
    public OneDriveFile getFile(int userId, String accessToken, String itemId) throws OXException {
        return fileEntityParser.parseEntity(userId, api.getItem(accessToken, itemId));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService#getFiles(int, java.lang.String, java.util.List)
     */
    @Override
    public List<OneDriveFile> getFiles(int userId, String accessToken, List<String> itemIds) throws OXException {
        List<OneDriveFile> files = new LinkedList<>();
        for (String itemId : itemIds) {
            try {
                files.add(fileEntityParser.parseEntity(userId, api.getItem(accessToken, itemId)));
            } catch (OXException e) {
                if (RESTExceptionCodes.PAGE_NOT_FOUND.equals(e)) {
                    LOG.debug("Item with id '{}' for user with id '{}' was not found in OneDrive", itemId, userId);
                    continue;
                }
                throw e;
            }
        }
        return files;
    }

    //////////////////////////////////////// HELPERS /////////////////////////////////////

    /**
     * Checks and extracts a 'skipToken' if available.
     * 
     * @param response The {@link JSONObject} response body
     * @return the skipToken or <code>null</code> if none available.
     */
    private String extractSkipToken(JSONObject response) {
        String nextLink = response.optString("@odata.nextLink");
        if (Strings.isEmpty(nextLink)) {
            return null;
        }
        int indexOfKey = nextLink.indexOf("skiptoken");
        if (indexOfKey < 0) {
            return null;
        }
        int indexOfValue = nextLink.indexOf("=", indexOfKey);
        if (indexOfValue < 0) {
            return null;
        }
        int indexOfLast = nextLink.indexOf("=", indexOfValue);
        return ((indexOfLast < 0)) ? nextLink.substring(indexOfValue) : nextLink.substring(indexOfValue, indexOfLast);
    }

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
