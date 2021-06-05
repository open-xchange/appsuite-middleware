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

import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.file.storage.File;
import com.openexchange.java.Strings;

/**
 * {@link DirectLinkGenerator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectLinkGenerator {

    private final SyncSession session;

    /**
     * Initializes a new {@link DirectLinkGenerator}.
     *
     * @param session The sync session
     */
    public DirectLinkGenerator(SyncSession session) {
        super();
        this.session = session;
    }

    /**
     * Gets the ready-to-use quota link.
     *
     * @return The quota link
     */
    public String getQuotaLink() {
        return session.getConfig().getDirectLinkQuota()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
            .replaceAll("\\[contextid\\]", String.valueOf(session.getServerSession().getContextId()))
            .replaceAll("\\[userid\\]", String.valueOf(session.getServerSession().getUserId()))
            .replaceAll("\\[login\\]", String.valueOf(session.getServerSession().getLogin()))
        ;
    }

    /**
     * Gets the ready-to-use help link.
     *
     * @return The help link
     */
    public String getHelpLink() {
        return session.getConfig().getDirectLinkHelp()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
            .replaceAll("\\[contextid\\]", String.valueOf(session.getServerSession().getContextId()))
            .replaceAll("\\[userid\\]", String.valueOf(session.getServerSession().getUserId()))
            .replaceAll("\\[login\\]", String.valueOf(session.getServerSession().getLogin()))
            .replaceAll("\\[locale\\]", String.valueOf(session.getDriveSession().getLocale()))
        ;
    }

    /**
     * Gets the direct link fragments for the supplied file.
     *
     * @param file The file
     * @return The direct link fragments
     */
    public String getFileLinkFragments(File file) {
        return getFileLinkFragments(file.getFolderId(), file.getId());
    }

    /**
     * Gets the direct link fragments for the file referenced by the supplied identifiers.
     *
     * @param folderID The file's parent folder ID
     * @param objectID The file's object ID
     * @return The direct link fragments
     */
    public String getFileLinkFragments(String folderID, String objectID) {
        return session.getConfig().getDirectLinkFragmentsFile()
            .replaceAll("\\[folder\\]", folderID)
            .replaceAll("\\[object\\]", objectID)
        ;
    }

    /**
     * Gets a ready-to-use direct link for the supplied file.
     *
     * @param file The file
     * @return The direct link
     */
    public String getFileLink(File file) {
        return getFileLink(file.getFolderId(), file.getId());
    }

    /**
     * Gets a ready-to-use direct link for the file referenced by the supplied identifiers.
     *
     * @param folderID The file's parent folder ID
     * @param objectID The file's object ID
     * @return The direct link
     */
    public String getFileLink(String folderID, String objectID) {
        return session.getConfig().getDirectLinkFile()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[filefragments\\]", getFileLinkFragments(folderID, objectID))
        ;
    }

    /**
     * Gets a ready-to-use preview link for the supplied file if available.
     *
     * @param file The file
     * @return The direct link, or <code>null</code> if not available
     */
    public String getFilePreviewLink(File file) {
        int[] dimensions = session.getConfig().getPreviewImageSize();
        return getFileImageLink(file, dimensions[0], dimensions[1]);
    }

    /**
     * Gets a ready-to-use thumbnail link for the supplied file if available.
     *
     * @param file The file
     * @return The direct link, or <code>null</code> if not available
     */
    public String getFileThumbnailLink(File file) {
        int[] dimensions = session.getConfig().getThumbnailImageSize();
        return getFileImageLink(file, dimensions[0], dimensions[1]);
    }

    private String getFileImageLink(File file, int width, int height) {
        String mimeType = DriveUtils.determineMimeType(file);
        if (Strings.isNotEmpty(mimeType) && 0 < file.getFileSize()) {
            // patterns borrowed from web interface
            if (mimeType.matches("(?i)^(image\\/(gif|png|jpe?g|bmp|tiff|heif|heic))$")) {
                return session.getConfig().getImageLinkImageFile()
                    .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
                    .replaceAll("\\[hostname\\]", session.getHostData().getHost())
                    .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
                    .replaceAll("\\[folder\\]", file.getFolderId())
                    .replaceAll("\\[object\\]", file.getId())
                    .replaceAll("\\[version\\]", null == file.getVersion() ? "0" : file.getVersion())
                    .replaceAll("\\[contextid\\]", String.valueOf(session.getServerSession().getContextId()))
                    .replaceAll("\\[userid\\]", String.valueOf(session.getServerSession().getUserId()))
                    .replaceAll("\\[width\\]", String.valueOf(width))
                    .replaceAll("\\[height\\]", String.valueOf(height))
                ;
            }
            if (mimeType.matches("(?i)^audio\\/(mpeg|m4a|m4b|mp3|ogg|oga|opus|x-m4a)$")) {
                return session.getConfig().getImageLinkAudioFile()
                    .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
                    .replaceAll("\\[hostname\\]", session.getHostData().getHost())
                    .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
                    .replaceAll("\\[folder\\]", file.getFolderId())
                    .replaceAll("\\[object\\]", file.getId())
                    .replaceAll("\\[version\\]", null == file.getVersion() ? "0" : file.getVersion())
                    .replaceAll("\\[contextid\\]", String.valueOf(session.getServerSession().getContextId()))
                    .replaceAll("\\[userid\\]", String.valueOf(session.getServerSession().getUserId()))
                    .replaceAll("\\[width\\]", String.valueOf(width))
                    .replaceAll("\\[height\\]", String.valueOf(height))
                ;
            }
            if (session.hasCapability("document_preview") &&
                (mimeType.matches(
                "(?i)^application\\/.*(ms-word|ms-excel|ms-powerpoint|msword|msexcel|mspowerpoint|openxmlformats|opendocument|pdf|rtf).*$")
                || mimeType.matches("(?i)^text\\/.*(rtf|plain).*$"))) {
                return session.getConfig().getImageLinkDocumentFile()
                    .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
                    .replaceAll("\\[hostname\\]", session.getHostData().getHost())
                    .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
                    .replaceAll("\\[folder\\]", file.getFolderId())
                    .replaceAll("\\[object\\]", file.getId())
                    .replaceAll("\\[version\\]", null == file.getVersion() ? "0" : file.getVersion())
                    .replaceAll("\\[contextid\\]", String.valueOf(session.getServerSession().getContextId()))
                    .replaceAll("\\[userid\\]", String.valueOf(session.getServerSession().getUserId()))
                    .replaceAll("\\[width\\]", String.valueOf(width))
                    .replaceAll("\\[height\\]", String.valueOf(height))
                ;
            }
        }
        return null;
    }

    /**
     * Gets the direct link fragments for the directory referenced by the supplied identifier.
     *
     * @param folderID The folder ID
     * @return The direct link fragments
     */
    public String getDirectoryLinkFragments(String folderID) {
        return session.getConfig().getDirectLinkFragmentsDirectory()
            .replaceAll("\\[folder\\]", folderID)
        ;
    }

    /**
     * Gets a ready-to-use direct link for the directory referenced by the supplied identifier.
     *
     * @param folderID The folder ID
     * @return The direct link
     */
    public String getDirectoryLink(String folderID) {
        return session.getConfig().getDirectLinkDirectory()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[directoryfragments\\]", getDirectoryLinkFragments(folderID))
        ;
    }

    private String getWebpath() {
        return trimSlashes(session.getConfig().getUiWebPath());
    }

    private String getDispatcherPrefix() {
        return trimSlashes(session.getConfig().getDispatcherPrefix());
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

}
