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

package com.openexchange.groupware.infostore.facade.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EntityInfoLoader;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.session.Session;

/**
 * {@link InfostoreEntityInfoLoader}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public abstract class InfostoreEntityInfoLoader extends MetadataLoader<EntityInfo> {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreEntityInfoLoader.class);

    protected final Session session;
    protected final EntityInfoLoader loader;
    protected final Map<Integer, DocumentMetadata> documents;

    public InfostoreEntityInfoLoader(Map<Integer, DocumentMetadata> documents, EntityInfoLoader loader, Session session) {
        super();
        this.documents = documents;
        this.session = session;
        this.loader = loader;
    }

    public Map<Integer, EntityInfo> load(Collection<Integer> ids, int field) throws OXException {
        if (null == ids || ids.size() == 0 || null == documents || documents.isEmpty() || ids.size() != documents.size()) {
            return Collections.emptyMap();
        }
        Map<Integer, EntityInfo> map = new HashMap<Integer, EntityInfo>(ids.size());
        for (Integer id : ids) {
            DocumentMetadata document = documents.get(id);
            if (null == document) {
                LOG.debug("\"ids\" size and \"documents\" size mismatch: {}, {}", I(ids.size()), I(documents.size()));
                return Collections.emptyMap();
            }
            EntityInfo info = null;
            if (Metadata.CREATED_FROM == field) {
                info = loader.load(document.getCreatedBy(), session);
            } else if (Metadata.MODIFIED_FROM == field) {
                info = loader.load(document.getModifiedBy(), session);
            } else {
                LOG.debug("Invalid column to load: {}.", Metadata.get(field));
                return Collections.emptyMap();
            }
            map.put(id, info);
        }
        return map;
    }
}
