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
import com.openexchange.importexport.Format;
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
    public SizedInputStream exportData(ServerSession session, AJAXRequestData requestData, OutputStream optOut, boolean isSaveToDisk, String filename) throws OXException {
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

        if (null != optOut) {
            try {
                getExportDataSource(session, optOut);
                return null;
            } finally {
                Streams.close(optOut);
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
