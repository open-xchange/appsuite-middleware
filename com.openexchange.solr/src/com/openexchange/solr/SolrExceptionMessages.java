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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link SolrExceptionMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SolrExceptionMessages implements LocalizableStrings {

    /**
     * Initializes a new {@link SolrExceptionMessages}.
     */
    public SolrExceptionMessages() {
        super();
    }

    // Could not find solr core entry for user %1$s and module %2$s in context %3$s.
    public static final String CORE_ENTRY_NOT_FOUND_MSG = "Did not find solr core entry for user %1$s and module %2$s in context %3$s.";

    // Could not find solr core store for given attributes. %1$s.
    public static final String CORE_STORE_ENTRY_NOT_FOUND_MSG = "Could not find solr core store for given attributes. %1$s.";

    // All core stores seem to be full.
    public static final String NO_FREE_CORE_STORE_MSG = "All core stores seem to be full.";   
    
    // This cores instance directory (%1$s) already exists and its structure is inconsistent.
    public static final String INSTANCE_DIR_EXISTS_MSG = "This cores instance directory (%1$s) already exists and its structure is inconsistent.";

    // An index fault occurred: %1$s
    public static final String INDEX_FAULT_MSG = "An index fault occurred: %1$s";

    // The file or directory %1$s does not exist.
    public static final String FILE_EXISTS_ERROR_MSG = "The file or directory %1$s does not exist.";

    // Could neither delegate solr request to a local nor to a remote server instance.
    public static final String DELEGATION_ERROR_MSG = "Could neither delegate solr request to a local nor to a remote server instance.";

    // Could not parse URI: %1$s.
    public static final String URI_PARSE_ERROR_MSG = "Could not parse URI: %1$s.";

    // Remote error: %1$s.
    public static final String REMOTE_ERROR_MSG = "Remote error: %1$s.";

    // Could not parse solr core identifier %1$s.
    public static final String IDENTIFIER_PARSE_ERROR_MSG = "Could not parse solr core identifier %1$s.";

    // Unknown module: %1$s.
    public static final String UNKNOWN_MODULE_MSG = "Unknown module: %1$s.";

    // Can not reach solr core store. URI %1$s does not lead to an existing directory.
    public static final String CORE_STORE_NOT_EXISTS_ERROR_MSG = "Can not reach solr core store. URI %1$s does not lead to an existing directory.";

    // The affected solr core %1$s is not started up yet. Please try again later.
	public static final String CORE_NOT_STARTED_MSG = "The affected solr core %1$s is not started up yet. Please try again later.";

	// The document with uuid %1$s could not be found.
    public static final String DOCUMENT_NOT_FOUND_MSG = "The document with uuid %1$s could not be found.";
    
}
