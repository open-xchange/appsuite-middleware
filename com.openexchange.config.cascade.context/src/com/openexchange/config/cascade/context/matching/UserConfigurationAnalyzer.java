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

package com.openexchange.config.cascade.context.matching;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;


/**
 * {@link UserConfigurationAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UserConfigurationAnalyzer {

	/**
	 * Gets the tags for given user configuration.
	 *
	 * @param perms The user permission bits
	 * @return The tags
	 */
	public Set<String> getTags(UserPermissionBits perms) {
	    if (null == perms) {
            return Collections.emptySet();
        }

	    TagCollector collector = null;
        for (Permission p : Permission.byBits(perms.getPermissionBits())) {
            if (collector == null) {
                collector = new TagCollector(p);
            } else {
                collector.addTagFor(p);
            }
        }
        return collector == null ? Collections.emptySet() : collector.getTags();
    }

	// -------------------------------------------------------------------------------------------------------------------------------------

	private static class TagCollector {

	    private final Set<String> tags;
	    private final StringBuilder sb;

        /**
         * Initializes a new {@link TagCollector}.
         *
         * @param p The initial permission to add
         */
        TagCollector(Permission p) {
            super();
            tags = new HashSet<String>(64);
            sb = new StringBuilder("uc").append(p.getTagName());
            tags.add(sb.toString());
        }

        void addTagFor(Permission p) {
            sb.setLength(2);
            tags.add(sb.append(p.getTagName()).toString());
        }

        Set<String> getTags() {
            return tags;
        }
	}

}
