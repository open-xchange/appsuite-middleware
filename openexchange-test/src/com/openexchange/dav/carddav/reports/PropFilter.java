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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.dav.carddav.reports;


/**
 * {@link PropFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class PropFilter {

    private String name;
    private String matchType;
    private String textMatch;
    
    /**
     * Initializes a new {@link PropFilter}.
     * 
     * @param name
     * @param matchType
     * @param textMatch
     */
    public PropFilter(String name, String matchType, String textMatch) {
        super();
        this.name = name;
        this.matchType = matchType;
        this.textMatch = textMatch;
    }
    
    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the matchType
     *
     * @return The matchType
     */
    public String getMatchType() {
        return matchType;
    }

    /**
     * Sets the matchType
     *
     * @param matchType The matchType to set
     */
    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    /**
     * Gets the textMatch
     *
     * @return The textMatch
     */
    public String getTextMatch() {
        return textMatch;
    }

    /**
     * Sets the textMatch
     *
     * @param textMatch The textMatch to set
     */
    public void setTextMatch(String textMatch) {
        this.textMatch = textMatch;
    }


}
