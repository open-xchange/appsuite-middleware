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

package com.openexchange.find.spi;

import java.io.Serializable;
import com.openexchange.find.common.CommonFacetType;


/**
 * Encapsulates some configuration settings for clients. A {@link SearchConfiguration}
 * is specific per {@link ModuleSearchDriver} implementation and may additionally
 * depend on the requesting user resp. its session.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class SearchConfiguration implements Serializable {

    private static final long serialVersionUID = 1009264293278859341L;

    private boolean requiresFolder = false;

    private boolean requiresAccount = false;

    public SearchConfiguration() {
        super();
    }

    /**
     * Sets that the user-specific search driver requires a set
     * {@link CommonFacetType#FOLDER} facet on search requests.
     */
    public SearchConfiguration setRequiresFolder() {
        requiresFolder  = true;
        return this;
    }

    /**
     * Returns if the user-specific search driver requires a set
     * {@link CommonFacetType#FOLDER} facet on search requests.
     *
     * @return <code>true</code>, if the folder facet is mandatory.
     */
    public boolean requiresFolder() {
        return requiresFolder;
    }

    public SearchConfiguration setRequiresAccount() {
        requiresAccount  = true;
        return this;
    }

    public boolean requiresAccount() {
        return requiresAccount;
    }

}
