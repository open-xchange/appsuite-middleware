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

package com.openexchange.importexport.exporters.ical;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.osgi.Tools.requireService;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.contact.helpers.ContactDisplayNameHelper;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.DelayInitServletOutputStream;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractICalExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalExporter implements ICalExport {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractICalExporter.class);

    public AbstractICalExporter() {
        super();
    }

    public AbstractICalExporter(String folderId, Map<String, List<String>> batchIds) {
        super();
        this.folderId = folderId;
        this.batchIds = batchIds;
    }

    private String folderId;

    private Map<String, List<String>> batchIds;

    /**
     * Exports the requested batch of data
     *
     * @param session The user session
     * @param out The output stream
     * @return ThresholdFileHolder The file holder
     * @throws OXException if export fails
     */
    abstract protected ThresholdFileHolder exportBatchData(ServerSession session, OutputStream out) throws OXException;

    /**
     * Exports the requested folder
     *
     * @param session The user session
     * @param out The output stream
     * @return ThresholdFileHolder The file holder
     * @throws OXException if export fails
     */
    abstract protected ThresholdFileHolder exportFolderData(ServerSession session, OutputStream out) throws OXException;

    @Override
    public SizedInputStream exportData(ServerSession session, AJAXRequestData requestData, boolean isSaveToDisk, String filename) throws OXException {
        if (null != requestData) {
            // Try to stream
            HttpServletResponse response = requestData.optHttpServletResponse();
            if (null != response) {
                OutputStream out = null;
                try {
                    response.setHeader("Content-Type", isSaveToDisk ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    response.setHeader("Content-Disposition", "attachment" + filename);
                    Tools.removeCachingHeader(response);
                    out = new DelayInitServletOutputStream(response);
                    getExportDataSource(session, out);
                    return null;
                } finally {
                    Streams.close(out);
                }
            }
        }

        ThresholdFileHolder sink = null;
        boolean error = true;
        try {
            sink = getExportDataSource(session, null);
            error = false;
            return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    @Override
    public ThresholdFileHolder getExportDataSource(ServerSession session, OutputStream out) throws OXException {
        try {
            return isBatchExport() ? exportBatchData(session, out) : exportFolderData(session, out);
        } catch (Exception e) {
            LOGGER.debug("Unable to export data.", e);
            throw e;
        }
    }

    private boolean isBatchExport() {
        return !getBatchIds().isEmpty();
    }

    public String getFolderId() {
        return folderId;
    }

    public Map<String, List<String>> getBatchIds() {
        return batchIds;
    }

    /**
     * Extracts the display name for an exported folder, i.e. the (localized) folder name and owner information.
     *
     * @param folder The folder to extract the name for
     * @return The display name, or <code>null</code> if none could be derived
     */
    protected String extractName(UserizedFolder folder) {
        String name = folder.getLocalizedName(folder.getLocale(), true);
        if (null == name) {
            name = folder.getName();
        }
        if (null != name && SharedType.getInstance().equals(folder.getType())) {
            try {
                Contact owner = ImportExportServices.getContactService().getUser(folder.getSession(), folder.getCreatedBy());
                String ownerName = ContactDisplayNameHelper.formatDisplayName((I18nServiceRegistry) null, owner, folder.getLocale());
                if (Strings.isNotEmpty(ownerName)) {
                    name += " (" + ownerName + ')';
                }
            } catch (OXException e) {
                LOGGER.debug("Error getting display name for folder owner", e);
            }
        }
        return name;
    }

    /**
     * Extracts the display name for an exported folder, i.e. the (localized) folder name and owner information.
     *
     * @param session The session
     * @param folderId The identifier of the folder to extract the name for
     * @return The display name, or <code>null</code> if none could be derived
     */
    protected String extractName(Session session, String folderId) {
        try {
            return extractName(ImportExportServices.getFolderService().getFolder(FolderStorage.REAL_TREE_ID, folderId, session, null));
        } catch (OXException e) {
            LOGGER.debug("Error extracting name from folder", e);
        }
        return null;
    }

    /**
     * Gets the configured maximum number of included components when exporting to the iCalendar format.
     *
     * @param session The session
     * @return The export limit, or <code>-1</code> if not limited
     */
    protected static int getExportLimit(Session session) {
        Integer defaultValue = I(10000);
        try {
            ConfigView view = requireService(ConfigViewFactory.class, ImportExportServices.LOOKUP.get()).getView(session.getUserId(), session.getContextId());
            return i(view.opt("com.openexchange.export.ical.limit", Integer.class, defaultValue));
        } catch (OXException e) {
            LOGGER.debug("Error getting configured export limit, falling back to {}", defaultValue, e);
        }
        return i(defaultValue);
    }

}
