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

package com.openexchange.group.json.anonymizer;

import com.openexchange.ajax.anonymizer.AnonymizerService;
import com.openexchange.ajax.anonymizer.Anonymizers;
import com.openexchange.ajax.anonymizer.Module;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.session.Session;


/**
 * {@link GroupAnonymizer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GroupAnonymizer implements AnonymizerService<Group> {

    /**
     * Initializes a new {@link GroupAnonymizer}.
     */
    public GroupAnonymizer() {
        super();
    }

    @Override
    public Module getModule() {
        return Module.GROUP;
    }

    @Override
    public Group anonymize(Group entity, Session session) throws OXException {
        if (null == entity) {
            return entity;
        }

        String i18n = Anonymizers.getGroupI18nFor(session);
        String name = new StringBuilder(i18n).append(' ').append(entity.getIdentifier()).toString();
        entity.setDisplayName(name);
        entity.setSimpleName(name);
        return entity;
    }

}
