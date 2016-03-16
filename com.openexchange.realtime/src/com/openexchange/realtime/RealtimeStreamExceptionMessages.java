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

package com.openexchange.realtime;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link RealtimeStreamExceptionMessages} - Translatable error messages.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class RealtimeStreamExceptionMessages implements LocalizableStrings {

    /** No appropriate channel found for recipient %1$s with payload namespace %2$s */
    public static final String NO_APPROPRIATE_CHANNEL = "No appropriate channel found for recipient %1$s with payload namespace %2$s";

    /** The following needed service is missing: \"%1$s\" */
    public static final String NEEDED_SERVICE_MISSING_MSG = "The following needed service is missing: \"%1$s\"";

    // Unexpected error: %1$s
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected error: %1$s";

    /** Invalid ID. Resource identifier is missing. */
    public static final String INVALID_ID = "Invalid ID. Resource identifier is missing.";
    
    /*
    <xs:group name='streamErrorGroup'>
    <xs:choice>
      <xs:element ref='bad_format'/>
      <xs:element ref='bad_namespace_prefix'/>
      <xs:element ref='conflict'/>
      <xs:element ref='connection_timeout'/>
      <xs:element ref='host_gone'/>
      <xs:element ref='host_unknown'/>
      <xs:element ref='improper_addressing'/>
      <xs:element ref='internal_server_error'/>
      <xs:element ref='invalid_from'/>
      <xs:element ref='invalid_id'/>
      <xs:element ref='invalid_namespace'/>
      <xs:element ref='invalid_xml'/>
      <xs:element ref='not_authorized'/>
      <xs:element ref='policy_violation'/>
      <xs:element ref='remote_connection_failed'/>
      <xs:element ref='resource_constraint'/>
      <xs:element ref='restricted_xml'/>
      <xs:element ref='see_other_host'/>
      <xs:element ref='system_shutdown'/>
      <xs:element ref='undefined_condition'/>
      <xs:element ref='unsupported_encoding'/>
      <xs:element ref='unsupported_stanza_type'/>
      <xs:element ref='unsupported_version'/>
      <xs:element ref='xml_not_well_formed'/>
    </xs:choice>
    </xs:group>
    */
}
