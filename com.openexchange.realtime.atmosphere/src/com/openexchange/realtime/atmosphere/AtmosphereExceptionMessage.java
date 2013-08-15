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

package com.openexchange.realtime.atmosphere;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link AtmosphereExceptionMessage}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AtmosphereExceptionMessage implements LocalizableStrings {
    /** The mandatory session information is missing. */
    public static final String SESSIONINFO_DIDNT_MATCH_SERVERSESSION_MSG = "The session information didn't match any ServerSession";
    /** "Obligatory key \"%1$s\" is missing from the Stanza" */
    public static final String MISSING_KEY_MSG = "Obligatory key \"%1$s\" is missing from the Stanza";
    /** Could not find a builder for the specified element: . \"%1$s\" */
    public static final String MISSING_BUILDER_FOR_ELEMENT_MSG = "Could not find a builder for the given element: . \"%1$s\"";
    /** Error while building Stanza: \"%1$s\" */
    public static final String ERROR_WHILE_BUILDING_MSG = "Error while building Stanza: \"%1$s\"";
    /** Could not find a transformer for the PayloadElement: \"%1$s\" */
    public static final String MISSING_TRANSFORMER_FOR_PAYLOADELEMENT_MSG ="Could not find a transformer for the PayloadElement: \"%1$s\"";
    /** Could not find an initializer for the specified stanza */
    public static final String MISSING_INITIALIZER_FOR_STANZA_MSG ="Could not find an initializer for the given stanza: . \"%1$s\"";
    /** Error while transforming a PayloadElement: \"%1$s. %2$s\" */
    public static final String ERROR_WHILE_TRANSFORMING_MSG = "Error while transforming a PayloadElement: \"%1$s, %2$s\"";
    /** Error while converting a PayloadElement: \"%1$s\" */
    public static final String ERROR_WHILE_CONVERTING_MSG = "Error while converting PayloadElement data: \"%1$s\"";
    /** The following obligatory element is missing: \"%1$s\" */
    public static final String OBLIGATORY_ELEMENT_MISSING_MSG = "The following obligatory element is missing: \"%1$s\"";
    /** Malformed Presence Data */
    public static final String PRESENCE_DATA_MALFORMED_MSG = "Malformed Presence Data";
    /** Malformed Message Data */
    public static final String MESSAGE_DATA_MALFORMED_MSG = "Malformed Message Data";
    /** Malformed IQ Data */
    public static final String IQ_DATA_MALFORMED_MSG = "Malformed IQ Data";
    /** Malformed Presence Element: \"%1$s\" */
    public static final String PRESENCE_DATA_ELEMENT_MALFORMED_MSG = "Malformed Presence Element: \"%1$s\"";
    /** Malformed Message Element: \"%1$s\" */
    public static final String MESSAGE_DATA_ELEMENT_MALFORMED_MSG = "Malformed Message Element: \"%1$s\"";
    /** Malformed IQ Element: \"%1$s\" */
    public static final String IQ_DATA_ELEMENT_MALFORMED_MSG = "Malformed IQ Element: \"%1$s\"";
    /** Illegal value \"%1$s\" for key \"%1$s\" in: \"%3$s\" */
    public static final String ILLEGAL_VALUE_MSG = "Illegal value \"%1$s\" for key \"%1$s\" in: \"%3$s\"";
    /** Malformed POST Data" */
    public static final String POST_DATA_MALFORMED_MSG = "Malformed POST Data";
}
