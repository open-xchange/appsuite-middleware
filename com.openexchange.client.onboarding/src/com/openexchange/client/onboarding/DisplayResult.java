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

package com.openexchange.client.onboarding;

import java.util.Map;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link DisplayResult} - A result when an on-boarding configuration has been successfully executed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DisplayResult implements Result {

    private final Map<String, Object> configuration;
    private final ResultReply reply;

    /**
     * Initializes a new {@link DisplayResult}.
     *
     * @param configuration The configuration
     * @param reply The result reply
     */
    public DisplayResult(Map<String, Object> configuration, ResultReply reply) {
        super();
        this.configuration = configuration;
        this.reply = null == reply ? ResultReply.ACCEPT : reply;
    }

    /**
     * Gets the form configuration.
     *
     * @return The form configuration or <code>null</code>
     */
    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    @Override
    public ResultReply getReply() {
        return reply;
    }

    @Override
    public ResultObject getResultObject(OnboardingRequest request, Session session) throws OXException {
        OnboardingAction action = request.getAction();
        if (OnboardingAction.DISPLAY != action) {
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(null == action ? "null" : action.getId());
        }

        return new SimpleResultObject(new JSONObject(configuration), "json");
    }
}
