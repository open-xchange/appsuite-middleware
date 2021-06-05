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

package com.openexchange.antivirus;

/**
 * {@link AntiVirusResponseHeader} - Defines the ICAP AntiVirus specific headers.
 * The headers defined here are still 'work in progress' a.k.a. a DRAFT and not all
 * Anti-Viruses implement them. As fall-back the encapsulated response should be
 * checked as well.
 * 
 * @see <a href="https://tools.ietf.org/html/draft-stecher-icap-subid-00">draft-stecher-icap-subid-00</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class AntiVirusResponseHeader {

    public static final String X_INFECTION_FOUND = "X-Infection-Found";

    public static final String X_VIOLATIONS_FOUND = "X-Violations-Found";
}
