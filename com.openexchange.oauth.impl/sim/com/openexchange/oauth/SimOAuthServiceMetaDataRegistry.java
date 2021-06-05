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

package com.openexchange.oauth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;


/**
 * {@link SimOAuthServiceMetaDataRegistry}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimOAuthServiceMetaDataRegistry implements OAuthServiceMetaDataRegistry {

    private final Map<String, OAuthServiceMetaData> map = new HashMap<String, OAuthServiceMetaData>();

    @Override
    public boolean containsService(final String id, final int uid, final int cid) {
        return map.containsKey(id);
    }

    @Override
    public List<OAuthServiceMetaData> getAllServices(final int uid, final int cid) {
        return new ArrayList<OAuthServiceMetaData>(map.values());
    }

    @Override
    public OAuthServiceMetaData getService(final String id, final int uid, final int cid) throws OXException {
        if (!containsService(id, uid, cid)) {
            throw OAuthExceptionCodes.UNKNOWN_OAUTH_SERVICE_META_DATA.create(id);
        }
        return map.get(id);
    }

    public void addService(final OAuthServiceMetaData authServiceMetaData) {
        map.put(authServiceMetaData.getId(), authServiceMetaData);
    }

}
