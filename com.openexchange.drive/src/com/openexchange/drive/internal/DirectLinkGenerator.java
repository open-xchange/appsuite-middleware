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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;

/**
 * {@link DirectLinkGenerator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectLinkGenerator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DirectLinkGenerator.class);

    private final SyncSession session;
    private final ConfigurationService configService;

    private String hostName;
    private Boolean documentPreview;

    /**
     * Initializes a new {@link DirectLinkGenerator}.
     *
     * @param session The sync session
     */
    public DirectLinkGenerator(SyncSession session) {
        super();
        this.session = session;
        this.configService = DriveServiceLookup.getService(ConfigurationService.class);
    }

    /**
     * Gets the ready-to-use quota link.
     *
     * @return The quota link
     */
    public String getQuotaLink() {
        return getProperty("com.openexchange.drive.directLinkQuota", "https://[hostname]")
            .replaceAll("\\[hostname\\]", getHostName())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[contextid\\]", String.valueOf(session.getServerSession().getContextId()))
            .replaceAll("\\[userid\\]", String.valueOf(session.getServerSession().getUserId()))
            .replaceAll("\\[login\\]", String.valueOf(session.getServerSession().getLogin()))
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
        return getProperty("com.openexchange.drive.directLinkFragmentsFile", "m=infostore&f=[folder]&i=[object]")
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
        return getProperty("com.openexchange.drive.directLinkFile", "https://[hostname]/[uiwebpath]#[filefragments]")
            .replaceAll("\\[hostname\\]", getHostName())
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
        return getFileImageLink(file, 800, 800);
    }

    /**
     * Gets a ready-to-use thumbnail link for the supplied file if available.
     *
     * @param file The file
     * @return The direct link, or <code>null</code> if not available
     */
    public String getFileThumbnailLink(File file) {
        return getFileImageLink(file, 100, 100);
    }

    private String getFileImageLink(File file, int width, int height) {
        String mimeType = file.getFileMIMEType();
        if (false == Strings.isEmpty(mimeType)) {
            // patterns borrowed from web interface
            if (mimeType.matches("(?i)^(image\\/(gif|png|jpe?g|bmp|tiff))$")) {
                return getProperty("com.openexchange.drive.imageLinkAudioFile", "https://[hostname]/[dispatcherPrefix]/files?action=" +
                    "document&folder=[folder]&id=[object]&version=[version]&delivery=download&scaleType=contain&width=[width]&height=[height]")
                    .replaceAll("\\[hostname\\]", getHostName())
                    .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
                    .replaceAll("\\[folder\\]", file.getFolderId())
                    .replaceAll("\\[object\\]", file.getId())
                    .replaceAll("\\[version\\]", file.getVersion())
                    .replaceAll("\\[width\\]", String.valueOf(width))
                    .replaceAll("\\[height\\]", String.valueOf(height))
                ;
            }
            if (mimeType.matches("(?i)^audio\\/(mpeg|m4a|m4b|mp3|ogg|oga|opus|x-m4a)$")) {
                return getProperty("com.openexchange.drive.imageLinkAudioFile", "https://[hostname]/[dispatcherPrefix]/image/file/" +
                    "mp3Cover?folder=[folder]&id=[object]&version=[version]&delivery=download&scaleType=contain&width=[width]&height=[height]")
                    .replaceAll("\\[hostname\\]", getHostName())
                    .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
                    .replaceAll("\\[folder\\]", file.getFolderId())
                    .replaceAll("\\[object\\]", file.getId())
                    .replaceAll("\\[version\\]", file.getVersion())
                    .replaceAll("\\[width\\]", String.valueOf(width))
                    .replaceAll("\\[height\\]", String.valueOf(height))
                ;
            }
            if (mimeType.matches(
                "(?i)^application\\/.*(ms-word|ms-excel|ms-powerpoint|msword|msexcel|mspowerpoint|openxmlformats|opendocument|pdf|rtf).*$")
                && hasDocumentPreview()) {
                return getProperty("com.openexchange.drive.imageLinkAudioFile", "https://[hostname]/[dispatcherPrefix]/files?action=" +
                    "document&format=preview_image&folder=[folder]&id=[object]&version=[version]&delivery=download&scaleType=contain" +
                    "&width=[width]&height=[height]")
                    .replaceAll("\\[hostname\\]", getHostName())
                    .replaceAll("\\[dispatcherPrefix\\]", getDispatcherPrefix())
                    .replaceAll("\\[folder\\]", file.getFolderId())
                    .replaceAll("\\[object\\]", file.getId())
                    .replaceAll("\\[version\\]", file.getVersion())
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
        return getProperty("com.openexchange.drive.directLinkFragmentsDirectory", "m=infostore&f=[folder]")
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
        return getProperty("com.openexchange.drive.directLinkDirectory", "https://[hostname]/[uiwebpath]#[directoryfragments]")
            .replaceAll("\\[hostname\\]", getHostName())
            .replaceAll("\\[uiwebpath\\]", getWebpath())
            .replaceAll("\\[directoryfragments\\]", getDirectoryLinkFragments(folderID))
        ;
    }

    private String getProperty(String propertyName, String defaultValue) {
        return null != configService ? configService.getProperty(propertyName, defaultValue) : defaultValue;
    }

    private boolean hasDocumentPreview() {
        if (null == documentPreview) {
            documentPreview = Boolean.FALSE;
            CapabilityService capabilityService = DriveServiceLookup.getService(CapabilityService.class);
            if (null != capabilityService) {
                try {
                    Set<Capability> capabilities = capabilityService.getCapabilities(session.getServerSession());
                    if (null != capabilities && capabilities.contains(new Capability("document_preview"))) {
                        documentPreview = Boolean.TRUE;
                    }
                } catch (OXException e) {
                    LOG.warn("Error determining capabilities", e);
                }
            }
        }
        return documentPreview.booleanValue();
    }

    private String getHostName() {
        if (null == hostName) {
            /*
             * Try host data parameter first
             */
            Object parameter = session.getServerSession().getParameter(HostnameService.PARAM_HOST_DATA);
            if (null != parameter && HostData.class.isInstance(parameter)) {
                hostName = ((HostData)parameter).getHost();
            }
            /*
             * Ask hostname service if available
             */
            if (Strings.isEmpty(hostName)) {
                HostnameService hostnameService = DriveServiceLookup.getOptionalService(HostnameService.class);
                if (null != hostnameService) {
                    hostName = hostnameService.getHostname(
                        session.getServerSession().getUserId(), session.getServerSession().getContextId());
                }
            }
            /*
             * Get hostname from java
             */
            if (Strings.isEmpty(hostName)) {
                try {
                    hostName = InetAddress.getLocalHost().getCanonicalHostName();
                } catch (UnknownHostException e) {
                    LOG.debug("error getting canonical hostname", e);
                }
            }
            /*
             * Fall back to localhost as last resort
             */
            if (Strings.isEmpty(hostName)) {
                LOG.warn("unable to get hostname, falling back to 'localhost'");
                hostName = "localhost";
            }
        }
        return hostName;
    }

    private String getWebpath() {
        return getProperty("com.openexchange.UIWebPath", "/ox6/index.html");
    }

    private String getDispatcherPrefix() {
        return getProperty("com.openexchange.dispatcher.prefix", "/ajax/");
    }

}
