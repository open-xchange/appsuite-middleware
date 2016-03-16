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

package com.openexchange.drive.impl.internal;

import com.openexchange.drive.impl.management.DriveConfig;


/**
 * {@link JumpLinkGenerator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class JumpLinkGenerator {

    private static final String[] OFFICE_TEXT_MIMETYPES = { "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/rtf",
        "application/vnd.oasis.opendocument.text", "application/vnd.openxmlformats-officedocument.wordprocessingml.template", "application/msword",
        "application/vnd.oasis.opendocument.text-master", "application/vnd.oasis.opendocument.text-template", "application/vnd.oasis.opendocument.text-web" };

    private static final String[] OFFICE_SPREADSHEET_MIMETYPES = { "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.oasis.opendocument.spreadsheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
        "application/vnd.ms-excel", "application/vnd.oasis.opendocument.spreadsheet-template" };

    private final static String APP_FILES = "app=io.ox/files";
    private final static String APP_EDITOR = "app=io.ox/editor";
    private final static String APP_PERMISSIONS = "app=io.ox/files/permissions";
    private final static String APP_HISTORY = "app=io.ox/files/history";
    private final static String APP_OFFICE_TEXT = "app=io.ox/office/text";
    private final static String APP_OFFICE_SPREADSHEET = "app=io.ox/office/spreadsheet";

    private final SyncSession session;

    public JumpLinkGenerator(SyncSession session) {
        this.session = session;
    }

    public String getJumpLink(String folderId, String method) {
        return getJumpLink(folderId, null, method, null);
    }

    public String getJumpLink(String folderId, String fileId, String method, String mimeType) {
        String redirectUrl = DriveConfig.getInstance().getJumpLink()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", trimSlashes(DriveConfig.getInstance().getUiWebPath()))
            .replaceAll("\\[folder\\]", "folder=" + folderId);

        if (null != mimeType && null != fileId) {
            switch (method) {
            case "edit":
                if (inArray(OFFICE_TEXT_MIMETYPES, mimeType) && session.hasCapability("text")) {
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_OFFICE_TEXT);
                } else if (inArray(OFFICE_SPREADSHEET_MIMETYPES, mimeType) && session.hasCapability("spreadsheet")) {
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_OFFICE_SPREADSHEET);
                } else if (mimeType.startsWith("text/")){
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_EDITOR);
                } else {
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_FILES);
                }
                break;
            case "permissions":
                redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_PERMISSIONS);
                break;
            case "version_history":
                redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_HISTORY);
                break;
            case "preview":
            default:
                redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_FILES);
                break;
            }
            redirectUrl = redirectUrl.replaceAll("\\[id\\]", "id=" + fileId);
        } else {
            if ("permissions".equals(method)) {
                redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_PERMISSIONS);
            } else {
                redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_FILES);
            }
            redirectUrl = redirectUrl.replaceAll("&\\[id\\]", "");
        }

        return redirectUrl;
    }

    private static String trimSlashes(String path) {
        if (null != path && 0 < path.length()) {
            if ('/' == path.charAt(0)) {
                path = path.substring(1);
            }
            if (0 < path.length() && '/' == path.charAt(path.length() - 1)) {
                path = path.substring(0, path.length() - 1);
            }
        }
        return path;
    }

    private static boolean inArray(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }

}
