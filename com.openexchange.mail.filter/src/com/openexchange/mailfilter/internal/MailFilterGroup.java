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

package com.openexchange.mailfilter.internal;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.jsieve.commands.Rule;

/**
 * A {@link MailFilterGroup} groups and sorts a list of mail filter rules 
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public interface MailFilterGroup {

    /**
     * Removes all rules from the given list of rules which are part of this group and return them in a group specific order
     *  
     * @param rules A list of rules
     * @return A ordered list of rules
     * @throws OXException
     */
    public List<Rule> getOrderedRules(List<Rule> rules) throws OXException;

}
