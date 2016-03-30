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


package com.openexchange.admin.console.context.extensioninterfaces;

import java.util.HashMap;
import com.openexchange.admin.console.exception.OXConsolePluginException;
import com.openexchange.admin.console.user.UserAbstraction.CSVConstants;
import com.openexchange.admin.rmi.dataobjects.Context;


/**
 * This interface must be implemented by a class in the console package of a plugin so that it
 * can extend the basic command line options. To offer your own implementation of this interface
 * to the core. The ServiceLoader mechanism of JDK 6 is used which requests a directory
 * META-INF/services under which a text file whose name is the full-qualified binary name
 * of this interface (com.openexchange.admin.console.context.ContextConsoleCreateInterface). And the
 * content of this file must be the full-qualified binary name of your implementation.
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface ContextConsoleCreateInterface extends ContextConsoleCommonInterface {

    /**
     * This method processes a {@link HashMap} of CSVConstants with their name. This method can be used to
     * modify the map, so you can add or remove parameter which can be used in the CSV file. Or you can
     * change if a parameter is required or not
     *
     * @param constantsMap - the {@link HashMap}
     */
    public void processCSVConstants(HashMap<String, CSVConstants> constantsMap);

    /**
     * This method processes a single line from a CSV file and adds the results to the corresponding context
     * object
     *
     * @param nextLine
     * @param idarray
     * @param context
     * @throws OXConsolePluginException
     */
    public void applyExtensionValuesFromCSV(final String[] nextLine, final int[] idarray, final Context context) throws OXConsolePluginException;

}
