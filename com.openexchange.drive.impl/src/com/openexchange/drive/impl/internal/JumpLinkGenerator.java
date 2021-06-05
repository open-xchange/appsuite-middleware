/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.impl.internal;

/**
 * {@link JumpLinkGenerator}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class JumpLinkGenerator {

    private static final String[] OFFICE_TEXT_MIMETYPES = { "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/rtf",
        "application/vnd.oasis.opendocument.text", "application/vnd.openxmlformats-officedocument.wordprocessingml.template", "application/msword",
        "application/vnd.oasis.opendocument.text-master", "application/vnd.oasis.opendocument.text-template", "application/vnd.oasis.opendocument.text-web"
    };

    private static final String[] OFFICE_SPREADSHEET_MIMETYPES = { "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.oasis.opendocument.spreadsheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
        "application/vnd.ms-excel", "application/vnd.oasis.opendocument.spreadsheet-template"
    };

    private static final String[] OFFICE_PRESENTATION_MIMETYPES = { "application/vnd.openxmlformats-officedocument.presentationml.template",
        "application/vnd.openxmlformats-officedocument.presentationml.slideshow", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.openxmlformats-officedocument.presentationml.slide", "application/vnd.ms-powerpoint",
        "application/vnd.ms-powerpoint.presentation.macroEnabled.12", "application/vnd.oasis.opendocument.presentation",
        "application/vnd.oasis.opendocument.presentation-template"
    };

    private final static String APP_FILES = "app=io.ox/files";
    private final static String APP_EDITOR = "app=io.ox/editor";
    private final static String APP_PERMISSIONS = "app=io.ox/files/permissions";
    private final static String APP_HISTORY = "app=io.ox/files/history";
    private final static String APP_OFFICE_TEXT = "app=io.ox/office/text";
    private final static String APP_OFFICE_SPREADSHEET = "app=io.ox/office/spreadsheet";
    private final static String APP_OFFICE_PRESENTATION = "app=io.ox/office/presentation";

    private final SyncSession session;

    public JumpLinkGenerator(SyncSession session) {
        this.session = session;
    }

    public String getJumpLink(String folderId, String method) {
        return getJumpLink(folderId, null, method, null);
    }

    public String getJumpLink(String folderId, String fileId, String method, String mimeType) {
        String redirectUrl = session.getConfig().getJumpLink()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", trimSlashes(session.getConfig().getUiWebPath()))
            .replaceAll("\\[folder\\]", "folder=" + folderId);

        if (null != mimeType && null != fileId) {
            switch (method) {
            case "edit":
                if (inArray(OFFICE_TEXT_MIMETYPES, mimeType) && session.hasCapability("text")) {
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_OFFICE_TEXT);
                } else if (inArray(OFFICE_SPREADSHEET_MIMETYPES, mimeType) && session.hasCapability("spreadsheet")) {
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_OFFICE_SPREADSHEET);
                } else if (inArray(OFFICE_PRESENTATION_MIMETYPES, mimeType) && session.hasCapability("presentation")) {
                    redirectUrl = redirectUrl.replaceAll("\\[app\\]", APP_OFFICE_PRESENTATION);
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
