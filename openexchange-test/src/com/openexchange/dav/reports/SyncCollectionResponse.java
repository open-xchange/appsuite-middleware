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

package com.openexchange.dav.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.junit.Assert;
import com.openexchange.dav.PropertyNames;
import com.openexchange.dav.StatusCodes;

/**
 * {@link SyncCollectionResponse} - Custom response to an "sync-collection" report
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncCollectionResponse {

    private final MultiStatusResponse[] responses;
    private final String syncToken;

    public SyncCollectionResponse(MultiStatus multiStatus, String syncToken) {
        super();
        this.responses = multiStatus.getResponses();
        this.syncToken = syncToken;
    }

    /**
     * @return the syncToken
     */
    public String getSyncToken() {
        return syncToken;
    }

    /**
     * @return the responses
     */
    public MultiStatusResponse[] getResponses() {
        return responses;
    }

    public Map<String, String> getETagsStatusOK() {
        Map<String, String> eTags = new HashMap<String, String>();
        for (MultiStatusResponse response : responses) {
            if (response.getProperties(StatusCodes.SC_OK).contains(PropertyNames.GETETAG)) {
                String href = response.getHref();
                Assert.assertNotNull("got no href from response", href);
                Object value = response.getProperties(StatusCodes.SC_OK).get(PropertyNames.GETETAG).getValue();
                Assert.assertNotNull("got no ETag from response", value);
                String eTag = (String) value;
                eTags.put(href, eTag);
            }
        }
        return eTags;
    }

    public List<String> getHrefsStatusNotFound() {
        List<String> hrefs = new ArrayList<String>();
        for (MultiStatusResponse response : responses) {
            if (null != response.getStatus() && 0 < response.getStatus().length && null != response.getStatus()[0] && StatusCodes.SC_NOT_FOUND == response.getStatus()[0].getStatusCode()) {
                hrefs.add(response.getHref());
            }
        }
        return hrefs;
    }

}
