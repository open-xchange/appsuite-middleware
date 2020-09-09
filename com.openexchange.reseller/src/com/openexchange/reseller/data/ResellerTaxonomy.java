/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.reseller.data;

/**
 * {@link ResellerTaxonomy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ResellerTaxonomy {

    private final String taxonomy;
    private final int resellerId;
    private final int hashCode;

    /**
     * Initializes a new {@link ResellerTaxonomy}.
     * @param taxonomy The taxonomy
     * @param resellerId The reseller identifier
     */
    public ResellerTaxonomy(String taxonomy, int resellerId) {
        super();
        this.taxonomy = taxonomy;
        this.resellerId = resellerId;

        final int prime = 31;
        int result = 1;
        result = prime * result + resellerId;
        result = prime * result + ((taxonomy == null) ? 0 : taxonomy.hashCode());
        hashCode = result;
    }

    /**
     * Gets the taxonomy
     *
     * @return The taxonomy
     */
    public String getTaxonomy() {
        return taxonomy;
    }

    /**
     * Gets the resellerId
     *
     * @return The resellerId
     */
    public int getResellerId() {
        return resellerId;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResellerTaxonomy other = (ResellerTaxonomy) obj;
        if (resellerId != other.resellerId) {
            return false;
        }
        if (taxonomy == null) {
            if (other.taxonomy != null) {
                return false;
            }
        } else if (!taxonomy.equals(other.taxonomy)) {
            return false;
        }
        return true;
    }
}
