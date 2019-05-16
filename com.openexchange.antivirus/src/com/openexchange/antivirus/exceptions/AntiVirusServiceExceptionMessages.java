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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.antivirus.exceptions;

/**
 * {@link AntiVirusServiceExceptionMessages}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
final class AntiVirusServiceExceptionMessages {

    // The remote Anti-Virus service is unavailable at the moment. There is nothing we can do about it. Please try again later.
    public static final String REMOTE_SERVICE_UNAVAILABLE_MSG = "The remote Anti-Virus service is unavailable at the moment. There is nothing we can do about it. Please try again later.";
    // An internal server error occurred on the remote Anti-Virus's server side. There is nothing we can do about it.
    public static final String REMOTE_INTERNAL_SERVER_ERROR_MSG = "An internal server error occurred on the remote Anti-Virus' server side. There is nothing we can do about it.";
    // A remote server error occurred on the remote Anti-Virus's server side. There is nothing we can do about it.
    public static final String REMOTE_SERVER_ERROR_MSG = "A remote server error occurred on the remote Anti-Virus's server side. There is nothing we can do about it.";
    // It seems that the remote Anti-Virus service is experiencing some connectivity issues. There is nothing we can do about it. Please try again later.
    public static final String CANNOT_STABLISH_CONNECTION = "It seems that the remote Anti-Virus service is experiencing some connectivity issues. There is nothing we can do about it. Please try again later.";
    // The file '%1$s' you are trying to scan does not exist.
    public static final String FILE_NOT_EXISTS = "The file '%1$s' you are trying to scan does not exist.";
    // The file you are trying to scan exceeds the maximum allowed file size of %1$s.
    public static final String FILE_TOO_BIG = "The file you are trying to scan exceeds the maximum allowed file size of %1$s MB.";
    // The file '%1$s' you are trying to download seems to be infected with '%2$s.
    public static final String FILE_INFECTED = "The file '%1$s' you are trying to download seems to be infected with '%2$s'.";
    // We were unable to scan your file for viruses.
    public static final String UNABLE_TO_SCAN = "We were unable to scan your file for viruses.";
    // The Anti-Virus service is unavailable at the moment. Please try again later.
    public static final String ANTI_VIRUS_SERVICE_UNAVAILABLE = "The Anti-Virus service is unavailable at the moment. Please try again later.";
}
