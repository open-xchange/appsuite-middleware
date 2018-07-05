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

package com.openexchange.mailmapping.impl;

import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.ServiceReference;
import com.openexchange.exception.OXException;
import com.openexchange.mailmapping.MailResolver;
import com.openexchange.mailmapping.MailResolverService;
import com.openexchange.mailmapping.MultipleMailResolver;
import com.openexchange.mailmapping.ResolveReply;
import com.openexchange.mailmapping.ResolvedMail;
import com.openexchange.osgi.ServiceSet;
import com.openexchange.osgi.SimpleRegistryListener;


/**
 * The {@link MailResolverServiceImpl} is a utility class for consulting mail mapping services
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added constructor
 */
public class MailResolverServiceImpl implements MailResolverService, SimpleRegistryListener<MailResolver> {

    private final ServiceSet<MailResolver> chain;

    /**
     * Initializes a new {@link MailResolverServiceImpl}.
     */
    public MailResolverServiceImpl() {
        super();
        chain = new ServiceSet<MailResolver>();
    }

    @Override
    public ResolvedMail resolve(String mail) throws OXException {
        for (MailResolver resolver : chain) {
            ResolvedMail resolved = resolver.resolve(mail);
            if (resolved != null) {
                ResolveReply reply = resolved.getResolveReply();
                if (ResolveReply.ACCEPT.equals(reply)) {
                    // Return resolved instance
                    return resolved;
                }
                if (ResolveReply.DENY.equals(reply)) {
                    // No further processing allowed
                    return null;
                }
                // Otherwise NEUTRAL reply; next in chain
            }
        }
        return null;
    }

    @Override
    public ResolvedMail[] resolveMultiple(String... mails) throws OXException {
        if (null == mails || mails.length == 0) {
            return new ResolvedMail[0];
        }

        Map<Integer, ResolvedMail> index2ResolvedMail = new HashMap<>(mails.length);
        for (MailResolver resolver : chain) {
            if (resolver instanceof MultipleMailResolver) {
                // Pass complete mail array to current multiple-capable resolver
                ResolvedMail[] currentResolvedMails = ((MultipleMailResolver) resolver).resolveMultiple(mails);
                for (int i = currentResolvedMails.length; i-- > 0;) {
                    ResolvedMail resolved = currentResolvedMails[i];
                    if (resolved != null) {
                        Integer index = Integer.valueOf(i);
                        ResolveReply reply = resolved.getResolveReply();
                        if (ResolveReply.ACCEPT.equals(reply)) {
                            // Put resolved instance if index is not yet occupied
                            if (false == index2ResolvedMail.containsKey(index)) {
                                index2ResolvedMail.put(index, resolved);
                            }
                        }
                        if (ResolveReply.DENY.equals(reply)) {
                            // No further processing allowed
                            if (false == index2ResolvedMail.containsKey(index)) {
                                index2ResolvedMail.put(index, null);
                            }
                        }
                        // Otherwise NEUTRAL reply; next in chain
                    }
                }
            } else {
                // Need to iterate mails to pass them one-by-one to current resolver
                for (int i = mails.length; i-- > 0;) {
                    ResolvedMail resolved = resolver.resolve(mails[i]);
                    if (resolved != null) {
                        Integer index = Integer.valueOf(i);
                        ResolveReply reply = resolved.getResolveReply();
                        if (ResolveReply.ACCEPT.equals(reply)) {
                            // Put resolved instance if index is not yet occupied
                            if (false == index2ResolvedMail.containsKey(index)) {
                                index2ResolvedMail.put(index, resolved);
                            }
                        }
                        if (ResolveReply.DENY.equals(reply)) {
                            // No further processing allowed
                            if (false == index2ResolvedMail.containsKey(index)) {
                                index2ResolvedMail.put(index, null);
                            }
                        }
                        // Otherwise NEUTRAL reply; next in chain
                    }
                }
            }
        }

        ResolvedMail[] resolvedMails = new ResolvedMail[mails.length];
        for (int i = resolvedMails.length; i-- > 0;) {
            resolvedMails[i] = index2ResolvedMail.get(Integer.valueOf(i));
        }
        return resolvedMails;
    }

    @Override
    public void added(ServiceReference<MailResolver> ref, MailResolver service) {
        chain.added(ref, service);
    }

    @Override
    public void removed(ServiceReference<MailResolver> ref, MailResolver service) {
        chain.removed(ref, service);
    }

}
