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

package twitter4j;

import twitter4j.auth.Authorization;
import twitter4j.conf.Configuration;
import twitter4j.internal.http.HttpResponse;
import twitter4j.internal.json.OXStatusJSONImpl;

/**
 * {@link OXTwitterImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class OXTwitterImpl extends twitter4j.TwitterImpl implements OXTwitter {

    private static final long serialVersionUID = -5828299325374714007L;

    /**
     * Initializes a new {@link OXTwitterImpl}.
     *
     * @param conf
     * @param auth
     */
    public OXTwitterImpl(final Configuration conf, final Authorization auth) {
        super(conf, auth);
    }

    @Override
    public Status showStatusAuthenticated(final long id) throws TwitterException {
        return new OXStatusJSONImpl(get(conf.getRestBaseURL() + "statuses/show/" + id + ".json?include_entities=" + conf.isIncludeEntitiesEnabled()), conf);
        //return new Status(get(conf.getRestBaseURL() + "statuses/show/" + id + ".xml", null, true), this);
    }

    private HttpResponse get(final String url) throws TwitterException {
        if (!conf.isMBeanEnabled()) {
            return http.get(url, auth);
        }
        // intercept HTTP call for monitoring purposes
        HttpResponse response = null;
        final long start = System.currentTimeMillis();
        try {
            response = http.get(url, auth);
        } finally {
            final long elapsedTime = System.currentTimeMillis() - start;
            TwitterAPIMonitor.getInstance().methodCalled(url, elapsedTime, isOk(response));
        }
        return response;
    }

    private boolean isOk(final HttpResponse response) {
        return response != null && response.getStatusCode() < 300;
    }

}
