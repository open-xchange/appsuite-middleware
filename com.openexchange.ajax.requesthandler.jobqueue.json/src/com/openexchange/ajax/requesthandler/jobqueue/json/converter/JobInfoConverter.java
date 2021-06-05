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

package com.openexchange.ajax.requesthandler.jobqueue.json.converter;

import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.jobqueue.Job;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link JobInfoConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class JobInfoConverter implements ResultConverter {

    /**
     * Initializes a new {@link JobInfoConverter}.
     */
    public JobInfoConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "job";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object resultObject = result.getResultObject();
        try {
            if (resultObject instanceof Collection) {
                @SuppressWarnings("unchecked") Collection<JobInfo> jobInfos = (Collection<JobInfo>) resultObject;
                JSONArray jCollection = new JSONArray(jobInfos.size());
                for (JobInfo jobInfo : jobInfos) {
                    jCollection.put(writeSingleJobInfo(jobInfo));
                }
                result.setResultObject(jCollection, "json");
            } else {
                result.setResultObject(writeSingleJobInfo((JobInfo) resultObject), "json");
            }
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create(x, x.getMessage());
        }
    }

    private JSONObject writeSingleJobInfo(JobInfo jobInfo) throws JSONException {
        JSONObject jInfo = new JSONObject(6);
        jInfo.put("id", UUIDs.getUnformattedString(jobInfo.getId()));
        Job job = jobInfo.getJob();
        AJAXRequestData requestData = job.getRequestData();
        jInfo.put("action", requestData.getAction());
        jInfo.put("module", requestData.getModule());
        jInfo.put("done", jobInfo.isDone());
        return jInfo;
    }

}
