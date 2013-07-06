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

package com.openexchange.spamhandler.spamassassin.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SpamhandlerSpamassassinExceptionMessage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SpamhandlerSpamassassinExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link SpamhandlerSpamassassinExceptionMessage}.
     */
    public SpamhandlerSpamassassinExceptionMessage() {
        super();
    }

    /**
     * The given value for mode "%s" is not a possible one
     */
    public final static String MODE_TYPE_WRONG_MSG = "The value given for mode \"%s\" is invalid.";

    /**
     * The parameter "%s" is not set in the property file
     */
    public final static String PARAMETER_NOT_SET_MSG = "The parameter \"%s\" is not set in property file";

    /**
     * The parameter "%s" must be set in the property file if spamd is true
     */
    public final static String PARAMETER_NOT_SET_SPAMD_MSG = "The parameter \"%s\" must be set in the property file if spamd is true";

    /**
     * The parameter "%s" must be an integer value but is "%s"
     */
    public final static String PARAMETER_NO_INTEGER_MSG = "The parameter \"%s\" must be an integer value but is \"%s\"";

    /**
     * The parameter "userSource" must be set in the property file if spamd is true
     */
    public final static String USERSOURCE_NOT_SET_MSG = "The parameter \"userSource\" must be set in the property file if spamd is true";

    /**
     * The given value for userSource "%s" is not a possible one
     */
    public final static String USERSOURCE_WRONG_MSG = "The given value for userSource \"%s\" is not a possible one";

    /**
     * The parameter "%s" must be numeric, but is "%s"
     */
    public final static String PARAMETER_NO_LONG_MSG = "The parameter \"%s\" must be numeric, but is \"%s\"";

    /**
     * Spamd returned wrong exit code "%s"
     */
    public final static String WRONG_SPAMD_EXIT_MSG = "Spamd returned wrong exit code \"%s\"";

    /**
     * Internal error: Wrong arguments are given to the tell command: "%s"
     */
    public final static String WRONG_TELL_CMD_ARGS_MSG = "Internal error: Wrong arguments are given to the tell command: \"%s\"";

    /**
     * Error during communication with spamd: "%s"
     */
    public final static String COMMUNICATION_ERROR_MSG = "Error during communication with spamd: \"%s\"";

    /**
     * Can't handle spam because MailService isn't available
     */
    public final static String MAILSERVICE_MISSING_MSG = "Spam cannot be handled because MailService is not available";

    /**
     * Error while getting spamd provider from service: "%s"
     */
    public final static String ERROR_GETTING_SPAMD_PROVIDER_MSG = "Error while getting spamd provider from service: \"%s\"";

}
