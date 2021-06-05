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

package com.openexchange.contact.vcard;

/**
 * {@link DistributionListMode}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public enum DistributionListMode {

    /**
     * Contacts marked as distribution list will be exported to vCards using the <i>Apple</i>-style properties
     * <code>X-ADDRESSBOOKSERVER-KIND</code> and <code>X-ADDRESSBOOKSERVER-MEMBER</code>.
     */
    ADDRESSBOOKSERVER,

    /**
     * Contacts marked as distribution list will be exported to vCards using the <i>version 4</i>-style properties
     * <code>KIND</code> and <code>MEMBER</code>, but still within a vCard version 3.0 container.
     */
    V4_IN_V3_EXPORT,

    ;

}
