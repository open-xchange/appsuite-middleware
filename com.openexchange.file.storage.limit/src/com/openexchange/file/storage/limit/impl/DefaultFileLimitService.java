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

package com.openexchange.file.storage.limit.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.limit.LimitFile;
import com.openexchange.file.storage.limit.FileLimitService;
import com.openexchange.file.storage.limit.type.TypeLimitChecker;
import com.openexchange.file.storage.limit.type.impl.TypeLimitCheckerRegistry;
import com.openexchange.session.Session;

/**
 *
 * {@link DefaultFileLimitService}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.1
 */
public class DefaultFileLimitService implements FileLimitService {

    private final TypeLimitCheckerRegistry typeLimitCheckerRegistry;

    /**
     * Initializes a new {@link DefaultFileLimitService}.
     *
     * @param registry The registry to use
     */
    public DefaultFileLimitService(TypeLimitCheckerRegistry registry) {
        this.typeLimitCheckerRegistry = registry;
    }

    @Override
    public List<OXException> checkLimits(Session session, String folderId, List<LimitFile> files, String type) throws OXException {
        List<OXException> exceptions = null;

        List<TypeLimitChecker> typeLimitCheckers = this.typeLimitCheckerRegistry.get(type);
        for (TypeLimitChecker typeLimitChecker : typeLimitCheckers) {
            List<OXException> checked = typeLimitChecker.check(session, folderId, files);
            if (null != checked && !checked.isEmpty()) {
                if (null == exceptions) {
                    exceptions = new ArrayList<>(checked);
                } else {
                    exceptions.addAll(checked);
                }
            }
        }
        return null == exceptions ? Collections.emptyList() : exceptions;
    }
}
