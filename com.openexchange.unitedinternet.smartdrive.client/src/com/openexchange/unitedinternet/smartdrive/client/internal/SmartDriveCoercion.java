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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveCollision;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveConstants;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveDeadProperty;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveResource;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveThumbNail;

/**
 * {@link SmartDriveCoercion} - Coerces JSON values to corresponding SmartDrive objects.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmartDriveCoercion implements SmartDriveConstants {

    /**
     * Initializes a new {@link SmartDriveCoercion}.
     */
    private SmartDriveCoercion() {
        super();
    }

    /**
     * Parses specified JSON array to a list of SmartDrive resources.
     * 
     * @param jsonArray The JSON array
     * @return The parsed list of SmartDrive resources.
     * @throws SmartDriveException If parsing fails
     */
    public static List<SmartDriveResource> parseResourcesResponse(final JSONArray jsonArray) throws SmartDriveException {
        try {
            final int len = jsonArray.length();
            final List<SmartDriveResource> list = new ArrayList<SmartDriveResource>(len);
            for (int i = 0; i < len; i++) {
                list.add(parseResourceResponse(jsonArray.getJSONObject(i)));
            }
            return list;
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses specified JSON object to a SmartDrive resource.
     * 
     * @param jsonResource The JSON resource
     * @return The parsed SmartDrive resource.
     * @throws SmartDriveException If parsing fails
     */
    public static SmartDriveResource parseResourceResponse(final JSONObject jsonResource) throws SmartDriveException {
        try {
            final SmartDriveResource ret;
            if (jsonResource.hasAndNotNull(JSON_FILE_SIZE)) {
                /*
                 * Expect a file
                 */
                final SmartDriveFileImpl fileImpl = new SmartDriveFileImpl();
                parseJSONFile(fileImpl, jsonResource);
                ret = fileImpl;
            } else {
                /*
                 * Expect a directory
                 */
                final SmartDriveDirectorympl directorympl = new SmartDriveDirectorympl();
                parseJSONDirectory(directorympl, jsonResource);
                ret = directorympl;
            }
            return ret;
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static void parseJSONDirectory(final SmartDriveDirectorympl directory, final JSONObject jsonDirectory) throws JSONException {
        if (jsonDirectory.hasAndNotNull(JSON_MIME_TYPE)) {
            final String mimeType = jsonDirectory.getString(JSON_MIME_TYPE);
            /*
             * Expect directory MIME type to be "application/directory"
             */
            if (!MIME_APPLICATION_DIRECTORY.equalsIgnoreCase(mimeType)) {
                throw new IllegalStateException(MessageFormat.format(
                    "Unexpected MIME type contained in SmartDrive directory. Expected \"{0}\", but was \"{1}\"",
                    MIME_APPLICATION_DIRECTORY,
                    mimeType));
            }
            directory.setMimeType(mimeType);
        }
        if (jsonDirectory.hasAndNotNull(JSON_UPLOAD_TOKEN)) {
            directory.setUploadToken(jsonDirectory.getString(JSON_UPLOAD_TOKEN));
        }
        parseJSONResource(directory, jsonDirectory);
    }

    private static void parseJSONFile(final SmartDriveFileImpl file, final JSONObject jsonFile) throws JSONException {
        if (jsonFile.hasAndNotNull(JSON_FILE_SIZE)) {
            file.setFileSize(jsonFile.getInt(JSON_FILE_SIZE));
        }
        parseJSONResource(file, jsonFile);
    }

    private static void parseJSONResource(final AbstractSmartDriveResource resource, final JSONObject jsonResource) throws JSONException {
        if (jsonResource.hasAndNotNull(JSON_NAME)) {
            resource.setName(jsonResource.getString(JSON_NAME));
        }
        if (jsonResource.hasAndNotNull(JSON_CREATION_DATE)) {
            resource.setCreationDate(new Date(jsonResource.getLong(JSON_CREATION_DATE)));
        }
        if (jsonResource.hasAndNotNull(JSON_LAST_MODIFIED)) {
            resource.setLastModified(new Date(jsonResource.getLong(JSON_LAST_MODIFIED)));
        }
        if (jsonResource.hasAndNotNull(JSON_DOWNLOAD_TOKEN)) {
            resource.setDownloadToken(jsonResource.getString(JSON_DOWNLOAD_TOKEN));
        }
        if (jsonResource.hasAndNotNull(JSON_THUMB_NAILS)) {
            final JSONObject jsonThumNails = jsonResource.getJSONObject(JSON_THUMB_NAILS);
            final Map<String, SmartDriveThumbNail> map = new HashMap<String, SmartDriveThumbNail>(jsonThumNails.length());
            for (final Entry<String, Object> entry : jsonThumNails.entrySet()) {
                final JSONObject jsonThumbNail = (JSONObject) entry.getValue();
                final SmartDriveThumbNailImpl thumbNailImpl = new SmartDriveThumbNailImpl();
                parseJSONThumbNail(thumbNailImpl, jsonThumbNail);
                map.put(entry.getKey(), thumbNailImpl);
            }
            resource.setThumbNails(map);
        }
        if (jsonResource.hasAndNotNull(JSON_DEAD_PROPERTIES)) {
            final JSONObject jsonDeadProperties = jsonResource.getJSONObject(JSON_DEAD_PROPERTIES);
            final List<SmartDriveDeadProperty> list = new ArrayList<SmartDriveDeadProperty>(jsonDeadProperties.length());
            for (final Entry<String, Object> entry : jsonDeadProperties.entrySet()) {
                final SmartDriveDeadProperty deadPropertyImpl = new SmartDriveDeadProperty();
                deadPropertyImpl.setPropertyName(entry.getKey());
                deadPropertyImpl.setValue((String) entry.getValue());
                list.add(deadPropertyImpl);
            }
            resource.setDeadProperties(list);
        }
    }

    private static void parseJSONThumbNail(final SmartDriveThumbNailImpl thumbNailImpl, final JSONObject jsonThumbNail) throws JSONException {
        if (jsonThumbNail.hasAndNotNull(JSON_MIME_TYPE)) {
            thumbNailImpl.setMimeType(jsonThumbNail.getString(JSON_MIME_TYPE));
        }
        if (jsonThumbNail.hasAndNotNull(JSON_URL)) {
            thumbNailImpl.setUrl(jsonThumbNail.getString(JSON_URL));
        }
    }

    /**
     * Parses specified JSON array to SmartDrive collisions.
     * 
     * @param jsonCollisions The JSON array
     * @return The parsed SmartDrive collisions
     * @throws SmartDriveException If parsing fails
     */
    public static List<SmartDriveCollision> parseCollisionsResponse(final JSONArray jsonCollisions) throws SmartDriveException {
        try {
            final int len = jsonCollisions.length();
            final List<SmartDriveCollision> list = new ArrayList<SmartDriveCollision>(len);
            for (int i = 0; i < len; i++) {
                list.add(parseCollisionResponse(jsonCollisions.getJSONObject(i)));
            }
            return list;
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses specified JSON object to a SmartDrive collision.
     * 
     * @param jsonCollision The JSON collision
     * @return The parsed SmartDrive collision.
     * @throws SmartDriveException If parsing fails
     */
    public static SmartDriveCollision parseCollisionResponse(final JSONObject jsonCollision) throws SmartDriveException {
        try {
            final SmartDriveCollisionImpl collisionImpl = new SmartDriveCollisionImpl();
            if (jsonCollision.hasAndNotNull(JSON_NAME)) {
                collisionImpl.setName(jsonCollision.getString(JSON_NAME));
            }
            if (jsonCollision.hasAndNotNull(JSON_RESPONSE)) {
                collisionImpl.setResponse(jsonCollision.getInt(JSON_RESPONSE));
            }
            if (jsonCollision.hasAndNotNull(JSON_STATUS)) {
                collisionImpl.setStatus(jsonCollision.getString(JSON_STATUS));
            }
            if (jsonCollision.hasAndNotNull(JSON_DESCRIPTION)) {
                collisionImpl.setDescription(jsonCollision.getString(JSON_DESCRIPTION));
            }
            if (jsonCollision.hasAndNotNull(JSON_IS_COLLECTION)) {
                collisionImpl.setDirectory(jsonCollision.getBoolean(JSON_IS_COLLECTION));
            }
            return collisionImpl;
        } catch (final JSONException e) {
            throw SmartDriveExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw SmartDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
