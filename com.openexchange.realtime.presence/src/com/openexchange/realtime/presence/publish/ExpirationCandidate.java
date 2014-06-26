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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.realtime.presence.publish;

import java.io.Serializable;
import com.openexchange.realtime.directory.Resource;

/**
 * {@link ExpirationCandidate} - Combine Resource with birthTime to be used for expiration and sorting.
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ExpirationCandidate implements Comparable<ExpirationCandidate>, Serializable {

    private static final long serialVersionUID = -8665102416929430074L;

    private Resource resource;

    private final long birthTime;

    /**
     * Initializes a new {@link ExpirationCandidate}.
     * 
     * @param id
     */
    public ExpirationCandidate(Resource resource) {
        super();
        this.resource = resource;
        this.birthTime = System.currentTimeMillis();
    }

    /**
     * Gets the resource
     * 
     * @return The resource
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Sets the resource
     * 
     * @param resource The resource to set
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Gets the birthTime
     * 
     * @return The birthTime
     */
    public long getBirthTime() {
        return birthTime;
    }

    @Override
    public int compareTo(final ExpirationCandidate otherEntry) {
        if (this.birthTime < otherEntry.birthTime) {// this one is older
            return 1;
        } else if (birthTime > otherEntry.birthTime) {// this one is younger
            return -1;
        } else {
            return 0;
        }
    }

}
