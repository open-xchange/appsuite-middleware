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

package com.openexchange.solr;

import java.io.Serializable;
import java.net.URI;



/**
 * {@link SolrCoreStore}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrCoreStore implements Serializable {

    private static final long serialVersionUID = -3765429569358418129L;

    private int id;

    private URI uri;

    private int maxCores;

    private int numCores;


    public SolrCoreStore() {
        super();
    }

    /**
     * Gets the id
     *
     * @return The id
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param id The id to set
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Gets the uri
     *
     * @return The uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the uri
     *
     * @param uri The uri to set
     */
    public void setUri(final URI uri) {
        this.uri = uri;
    }

    /**
     * Gets the maxCores
     *
     * @return The maxCores
     */
    public int getMaxCores() {
        return maxCores;
    }

    /**
     * Sets the maxCores
     *
     * @param maxCores The maxCores to set
     */
    public void setMaxCores(final int maxCores) {
        this.maxCores = maxCores;
    }


    public final int getNumCores() {
        return numCores;
    }


    public final void setNumCores(int numCores) {
        this.numCores = numCores;
    }
}
