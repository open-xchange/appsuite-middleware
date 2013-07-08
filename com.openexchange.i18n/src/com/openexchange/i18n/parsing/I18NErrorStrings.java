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

package com.openexchange.i18n.parsing;

import com.openexchange.i18n.LocalizableStrings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class I18NErrorStrings implements LocalizableStrings {

    // Messages //

    // The first argument will contain the token found, the second the filename of the file we were trying to parse, the third contains the
    // line number, the fourth a list of tokens expected at this stage.
    public static final String UNEXPECTED_TOKEN = "Unexpected token %s in .po file %s:%s. Expected one of %s";

    // The first argument will contain the string sent instead of the expected number, the second argument will contain the file name, the
    // third the line number
    public static final String EXPECTED_NUMBER = "Got %s, but expected a number in .po file %s:%s.";

    // The first argument will contain the letter found in the file, the second argument will contain the letter that was expected. The
    // third argument will contain the file name and the fourth the line number.
    public static final String MALFORMED_TOKEN = "Malformed or unsupported token. Got %s but expected %s in .po file %s:%s.";

    // The argument contains the filename of the .po file that could not be read.
    public static final String IO_EXCEPTION = "An I/O error ocurred reading .po file %s.";

    // Help //
    public static final String CHECK_FILE = "Please check that the file is correctly formatted.";

    public static final String FILE_ACCESS = "Please make sure the file is readable by the groupware.";
}
