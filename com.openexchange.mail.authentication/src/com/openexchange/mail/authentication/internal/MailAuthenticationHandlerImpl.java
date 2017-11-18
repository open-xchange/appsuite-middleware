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

package com.openexchange.mail.authentication.internal;

import java.util.regex.Pattern;
import com.openexchange.mail.authentication.MailAuthenticationHandler;
import com.openexchange.mail.authentication.MailAuthenticationResult;
import com.openexchange.mail.authentication.MailAuthenticationStatus;
import com.openexchange.mail.authentication.mechanism.MailAuthenticationMechanism;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.HeaderCollection;

/**
 * {@link MailAuthenticationHandlerImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MailAuthenticationHandlerImpl implements MailAuthenticationHandler {

    /**
     * Regex for checking whether each part of the domain is not longer than 63 characters,
     * and allow internationalized domain names using the punycode notation
     */
    //FIXME: Allow wildcards or regexes in the pattern
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("\\b((?=[a-z0-9-]{1,63}\\.)(xn--)?[a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,63}\\b");

    /**
     * Initialises a new {@link MailAuthenticationHandlerImpl}.
     */
    public MailAuthenticationHandlerImpl() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.mail.authentication.MailAuthenticationHandler#handle(com.openexchange.mail.dataobjects.MailPart)
     */
    @Override
    public MailAuthenticationResult handle(MailPart mailPart) {
        //TODO: Perform preliminary configuration checks, 
        //      like whether the feature is enabled or 
        //      the core engine shall be used.
        //      Maybe move those checks to a higher layer in the framework.

        HeaderCollection headerCollection = mailPart.getHeaders();
        String[] authHeaders = headerCollection.getHeader(MailAuthenticationHandler.AUTH_RESULTS_HEADER);
        if (authHeaders == null || authHeaders.length == 0) {
            // TODO: Pass on to custom handlers; return null for now
            return null;
        }

        MailAuthenticationResult result = new MailAuthenticationResult();
        result.setStatus(MailAuthenticationStatus.NOT_ANALYZED);

        // There can only be one, if there are more only the first one is relevant
        String[] split = authHeaders[0].split(";");
        // TODO: Check for possible version after the domain name
        // checkVersion();
        if (split == null || split.length == 0) {
            // Huh? Invalid/Malformed authentication results header, set to 'neutral'
            result.setStatus(MailAuthenticationStatus.NEUTRAL);
            return result;
        }

        // The first property of the header MUST always be the domain
        String domain = split[0];
        if (!isValidDomain(domain)) {
            // Not a valid domain, thus we return with 'neutral' status
            result.setStatus(MailAuthenticationStatus.NEUTRAL);
            return result;
        }
        result.setDomain(domain);

        for (MailAuthenticationMechanism mechanism : MailAuthenticationMechanism.values()) {
            parseMechanism(mechanism, split, result);
        }

        return result;
    }

    /**
     * 
     * @param mechanism
     * @param split
     * @param result
     */
    private void parseMechanism(MailAuthenticationMechanism mechanism, String[] split, MailAuthenticationResult result) {
        for (String s : split) {
            if (!s.startsWith(mechanism.name().toLowerCase())) {
                continue;
            }
            parseMechanism(mechanism, s, result);
        }
    }

    /**
     * Parses the specified mechanism
     * 
     * @param mechanism
     * @param s
     * @param result
     */
    private void parseMechanism(MailAuthenticationMechanism mechanism, String s, MailAuthenticationResult result) {
        // The mechanism tags are separated by a space
        String[] splitTags = s.split(" ");
        if (splitTags == null || splitTags.length == 0) {
            // Ignore
            return;
        }
        // The first one is always the mechanism used
        String mech = splitTags[0];
        String[] mechUsed = mech.split("=");
        if (mechUsed == null || mechUsed.length != 2) {
            // Ignore
            return;
        }
        String mechStatus = mechUsed[1];
        // TODO: Retrieve the valid status from the mechStatus of the header and set it to the result
    }

    /**
     * Determines whether the specified string denotes a valid domain name
     * 
     * @param domain Domain candidate
     * @return <code>true</code> if the string denotes a valid domain, <code>false</code> otherwise
     */
    private boolean isValidDomain(String domain) {
        return DOMAIN_PATTERN.matcher(domain).matches();
    }
}
