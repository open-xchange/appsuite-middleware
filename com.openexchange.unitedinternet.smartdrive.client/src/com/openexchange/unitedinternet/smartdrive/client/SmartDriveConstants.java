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

package com.openexchange.unitedinternet.smartdrive.client;

/**
 * {@link SmartDriveConstants}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SmartDriveConstants {

    public static final String HTTP_CLIENT_PARAM_SESSION_ID = "com.openexchange.unitedinternet.smartdrive.client.sessionid";

    public static final String HTTP_CLIENT_PARAM_DOWNLOAD_TOKEN = "com.openexchange.unitedinternet.smartdrive.client.downloadToken";

    /*-
     * ---------------------------------- Configuration constants ----------------------------------
     */

    /**
     * The HttpClient configuration parameter name for timeout.
     */
    public static final String CONFIG_TIMEOUT = "timeout";

    /**
     * The HttpClient configuration parameter name for login.
     */
    public static final String CONFIG_LOGIN = "login";

    /**
     * The HttpClient configuration parameter name for password.
     */
    public static final String CONFIG_PASSWORD = "password";

    /*-
     * ---------------------------------- User agent ----------------------------------
     */

    /**
     * The client name (user as <code>User-Agent</code> header for stateful SmartDrive methods).
     */
    public static final String CLIENT_NAME = "Open-Xchange SmartDrive Client";

    /*-
     * ---------------------------------- MIME types ----------------------------------
     */

    /**
     * The MIME type for a SmartDrive directory.
     */
    public static final String MIME_APPLICATION_DIRECTORY = "application/directory";

    /**
     * The MIME type for JSON data.<br>
     * TODO: What content type for JSON data? application/json or text/javascript?
     */
    public static final String MIME_JSON = "text/javascript";

    /*-
     * ---------------------------------- Cookie names ----------------------------------
     */

    /**
     * The name for JSESSIONID cookie.
     */
    public static final String COOKIE_JSESSIONID = "JSESSIONID";

    /**
     * The name for Authentication cookie.
     */
    public static final String COOKIE_AUTHENTICATION = "Authentication";

    /*-
     * ---------------------------------- Response codes ----------------------------------
     */

    /**
     * The status code for <code>200 (OK)</code>.
     */
    public static final int SC_OK = 200;

    /**
     * The status code for <code>201 (created)</code>.
     */
    public static final int SC_CREATED = 201;

    /**
     * The status code for <code>204 (no content)</code>.
     */
    public static final int SC_NO_CONTENT = 204;

    /**
     * The status code for <code>207 (multi status)</code>.
     */
    public static final int SC_MULTI_STATUS = 207;

    /**
     * The status code for <code>400 (general error)</code>.
     */
    public static final int SC_GENERAL_ERROR = 400;

    /**
     * The status code for <code>401 (Unauthorized)</code>.
     */
    public static final int SC_UNAUTHORIZED = 401;

    /**
     * The status code for <code>403 (forbidden)</code>.
     */
    public static final int SC_FORBIDDEN = 403;

    /**
     * The status code for <code>404 (not found)</code>.
     */
    public static final int SC_NOT_FOUND = 404;

    /**
     * The status code for <code>405 (method not allowed)</code>.
     */
    public static final int SC_METHOD_NOT_ALLOWED = 405;

    /**
     * The status code for <code>409 (conflict)</code>.
     */
    public static final int SC_CONFLICT = 409;

    /**
     * The status code for <code>412 (precondition failed)</code>.
     */
    public static final int SC_PRECONDITION_FAILED = 412;

    /**
     * The status code for <code>507 (insufficient storage)</code>.
     */
    public static final int SC_INSUFFICIENT_STORAGE = 507;

    /*-
     * ---------------------------------- JSON fields ----------------------------------
     */

    public static final String JSON_FILE_SIZE = "fileSize";

    public static final String JSON_NAME = "name";

    public static final String JSON_CREATION_DATE = "creationDate";

    public static final String JSON_LAST_MODIFIED = "lastModified";

    public static final String JSON_DOWNLOAD_TOKEN = "downloadToken";

    public static final String JSON_THUMB_NAILS = "thumbNails";

    public static final String JSON_MIME_TYPE = "mimeType";

    public static final String JSON_URL = "url";

    public static final String JSON_UPLOAD_TOKEN = "uploadToken";

    public static final String JSON_DEAD_PROPERTIES = "deadProperties";

    public static final String JSON_NEW_NAME = "newName";

    public static final String JSON_SRC_PATH = "srcPath";

    public static final String JSON_NAMES = "names";

    public static final String JSON_OVER_WRITE = "overWrite";

    public static final String JSON_RESPONSE = "response";

    public static final String JSON_STATUS = "status";

    public static final String JSON_DESCRIPTION = "description";

    public static final String JSON_IS_COLLECTION = "isCollection";

    // Query fields

    public static final String JSON_QUERY_TYPE = "queryType";

    public static final String JSON_QUERY_TEXT = "queryText";

    // User fields

    public static final String JSON_MAX_FILE_NAME_LENGTH = "MaxFileNameLength";

    public static final String JSON_STORAGE_FREEMAIL = "StorageFreemail";

    public static final String JSON_TRAFFIC_OWNER_USED = "TrafficOwnerUsed";

    public static final String JSON_TRAFFIC_UPLOAD = "TrafficUpload";

    public static final String JSON_TRAFFIC_UPLOAD_QUOTA = "TrafficUploadQuota";

    public static final String JSON_TRAFFIC_GUEST_QUOTA = "TrafficGuestQuoate";

    public static final String JSON_TRAFFIC_GUEST_USED = "TrafficGuestUsed";

    public static final String JSON_MAX_FILE_SIZE = "MaxFileSize";

    public static final String JSON_STORAGE_SMART_DRIVE = "StorageSmartDrive";

    public static final String JSON_MAX_FILE_COUNT = "MaxFileCount";

    public static final String JSON_STORAGE_FILE_COUNT = "StorageFileCount";

    public static final String JSON_STORAGE_QUOTA = "StorageQuota";

    public static final String JSON_STORAGE_FOTOALBUM = "StorageFotoalbum";

    public static final String JSON_TRAFFIC_OWNER_QUOTA = "TrafficOwnerQuota";

    public static final String JSON_MAX_FILES_PER_DIRECTORY = "MacFilesPerdirectory";

    public static final String JSON_ROOT = "ROOT";

    public static final String JSON_PICTURE = "PICTURE";

    public static final String JSON_MOUNT = "MOUNT";

    public static final String JSON_DOC = "DOC";

    public static final String JSON_VIDEO = "VIDEO";

    public static final String JSON_MUSIC = "MUSIC";

    public static final String JSON_TRASH = "TRASH";

    public static final String JSON_ATTACHMENT = "ATTACHMENT";

    // public static final String JSON_ = "";

}
