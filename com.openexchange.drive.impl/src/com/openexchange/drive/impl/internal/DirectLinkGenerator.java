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

import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.management.DriveConfig;
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
        return DriveConfig.getInstance().getDirectLinkQuota()
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
        return DriveConfig.getInstance().getDirectLinkHelp()
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
        return DriveConfig.getInstance().getDirectLinkFragmentsFile()
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
        return DriveConfig.getInstance().getDirectLinkFile()
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
        int[] dimensions = DriveConfig.getInstance().getPreviewImageSize();
        return getFileImageLink(file, dimensions[0], dimensions[1]);
    }

    /**
     * Gets a ready-to-use thumbnail link for the supplied file if available.
     *
     * @param file The file
     * @return The direct link, or <code>null</code> if not available
     */
    public String getFileThumbnailLink(File file) {
        int[] dimensions = DriveConfig.getInstance().getThumbnailImageSize();
        return getFileImageLink(file, dimensions[0], dimensions[1]);
    }

    private String getFileImageLink(File file, int width, int height) {
        String mimeType = DriveUtils.determineMimeType(file);
        if (Strings.isNotEmpty(mimeType) && 0 < file.getFileSize()) {
            // patterns borrowed from web interface
            if (mimeType.matches("(?i)^(image\\/(gif|png|jpe?g|bmp|tiff))$")) {
                return DriveConfig.getInstance().getImageLinkImageFile()
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
                return DriveConfig.getInstance().getImageLinkAudioFile()
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
            if ((mimeType.matches(
                "(?i)^application\\/.*(ms-word|ms-excel|ms-powerpoint|msword|msexcel|mspowerpoint|openxmlformats|opendocument|pdf|rtf).*$")
                || mimeType.matches("(?i)^text\\/.*(rtf|plain).*$")) && session.hasCapability("document_preview")) {
                return DriveConfig.getInstance().getImageLinkDocumentFile()
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
        return DriveConfig.getInstance().getDirectLinkFragmentsDirectory()
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
        return DriveConfig.getInstance().getDirectLinkDirectory()
            .replaceAll("\\[protocol\\]", session.getHostData().isSecure() ? "https" : "http")
            .replaceAll("\\[hostname\\]", session.getHostData().getHost())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[directoryfragments\\]", getDirectoryLinkFragments(folderID))
        ;
    }

    private String getWebpath() {
        return trimSlashes(DriveConfig.getInstance().getUiWebPath());
    }

    private String getDispatcherPrefix() {
        return trimSlashes(DriveConfig.getInstance().getDispatcherPrefix());
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
