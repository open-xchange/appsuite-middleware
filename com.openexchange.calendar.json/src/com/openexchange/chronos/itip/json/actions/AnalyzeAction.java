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

package com.openexchange.chronos.itip.json.actions;

import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.json.ITipAnalysisWriter;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.RankingAwareNearRegistryServiceTracker;
import com.openexchange.server.ServiceLookup;

/**
 * 
 * {@link AnalyzeAction}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class AnalyzeAction extends AbstractITipAction {

    public AnalyzeAction(ServiceLookup services, RankingAwareNearRegistryServiceTracker<ITipAnalyzerService> analyzerListing) {
        super(services, analyzerListing);
    }

    @Override
    protected Object process(List<ITipAnalysis> analysis, AJAXRequestData request, CalendarSession session, TimeZone tz) throws OXException {
        JSONArray array = new JSONArray(analysis.size());

        ITipAnalysisWriter writer = new ITipAnalysisWriter(tz, session, services);
        for (ITipAnalysis anAnalysis : analysis) {
            JSONObject object = new JSONObject();
            try {
                writer.write(anAnalysis, object);
            } catch (JSONException e) {
                LOG.error("", e); // Shouldn't happen
            }
            array.put(object);
        }
        return array;
    }

}
