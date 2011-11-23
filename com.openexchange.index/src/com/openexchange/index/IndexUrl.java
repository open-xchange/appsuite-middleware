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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.index;

/**
 * {@link IndexUrl} - The URL to an index host.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface IndexUrl {

    /**
     * Gets the string representation of the URL
     *
     * @return The URL's string representation
     */
    String getUrl();

    /**
     * Gets the setting for SO_TIMEOUT. 0 implies that the option is disabled (i.e., timeout of infinity).
     * <p>
     * Default is <code>1000</code>.
     *
     * @return The setting for SO_TIMEOUT
     */
    int getSoTimeout();

    /**
     * Gets the connection timeout. 0 implies that the option is disabled (i.e., timeout of infinity).
     * <p>
     * Default is <code>100</code>.
     *
     * @return The connection timeout
     */
    int getConnectionTimeout();

    /**
     * Gets the max. number of connections allowed being established per host. 0 implies that there is no restriction.
     * <p>
     * Default is <code>100</code>.
     *
     * @return The max. number of connections per host
     */
    int getMaxConnectionsPerHost();

    /**
     * Gets the max. number of indices that can be created on this server.
     * <p>
     * Default is <code>100</code>.
     *
     * @return The max. number of indices
     */
    int getMaxIndices();

    /**
     * Gets a hash code value for this index URL. This method is supported for the benefit of hashtables.
     *
     * @return A hash code value for this object.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    int hashCode();

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj The reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
     * @see #hashCode()
     */
    @Override
    boolean equals(Object obj);

}
