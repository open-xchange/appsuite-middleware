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

package com.openexchange.gdpr.dataexport.json;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.json.action.AbstractDataExportAction;
import com.openexchange.gdpr.dataexport.json.action.CancelDataExportAction;
import com.openexchange.gdpr.dataexport.json.action.DeleteDataExportAction;
import com.openexchange.gdpr.dataexport.json.action.DownloadDataExportResultFileAction;
import com.openexchange.gdpr.dataexport.json.action.GetAvailableModulesDataExportAction;
import com.openexchange.gdpr.dataexport.json.action.GetDataExportAction;
import com.openexchange.gdpr.dataexport.json.action.ScheduleDataExportAction;
import com.openexchange.gdpr.dataexport.json.action.SubmitDataExportAction;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class DataExportActionFactory implements AJAXActionServiceFactory {

    private static final String MODULE = "dataexport";

    /**
     * Gets the <code>"dataexport"</code> module identifier for GDPR data export action factory.
     *
     * @return The module identifier
     */
    public static String getModule() {
        return MODULE;
    }

    // ------------------------------------------------------------------------------------------

    private final Map<String, AbstractDataExportAction> actions;

    /**
     * Initializes a new {@link DataExportActionFactory}.
     */
    public DataExportActionFactory(ServiceLookup services) {
        super();
        ImmutableMap.Builder<String, AbstractDataExportAction> actions = ImmutableMap.builderWithExpectedSize(16);
        actions.put("get", new GetDataExportAction(services));
        actions.put("download", new DownloadDataExportResultFileAction(services));
        actions.put("availableModules", new GetAvailableModulesDataExportAction(services));
        actions.put("submit", new SubmitDataExportAction(services));
        actions.put("cancel", new CancelDataExportAction(services));
        actions.put("delete", new DeleteDataExportAction(services));
        actions.put("schedule", new ScheduleDataExportAction(services));
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }

}
