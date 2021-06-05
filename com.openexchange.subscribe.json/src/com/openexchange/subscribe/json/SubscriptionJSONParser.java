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

package com.openexchange.subscribe.json;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.json.FormContentParser;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;

/**
 * {@link SubscriptionJSONParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SubscriptionJSONParser {

    private final SubscriptionSourceDiscoveryService discovery;

    /**
     * Initializes a new {@link SubscriptionJSONParser}.
     *
     * @param discovery
     */
    public SubscriptionJSONParser(SubscriptionSourceDiscoveryService discovery) {
        super();
        this.discovery = discovery;
    }

    public Subscription parse(JSONObject object) throws JSONException {
        Subscription subscription = new Subscription();
        if (object.has("id")) {
            subscription.setId(object.getInt("id"));
        }
        if (object.has("folder")) {
            subscription.setFolderId(object.getString("folder"));
        }
        if (object.has("enabled")) {
            subscription.setEnabled(object.getBoolean("enabled"));
        }
        if (object.has("source")) {
            SubscriptionSource source = discovery.getSource(object.getString("source"));
            subscription.setSource(source);
            if (source != null) {
                JSONObject config = object.optJSONObject(subscription.getSource().getId());
                if (config != null) {
                    Map<String, Object> configuration = FormContentParser.parse(config, source.getFormDescription());
                    subscription.setConfiguration(configuration);
                }
            }
        }
        return subscription;
    }

}
