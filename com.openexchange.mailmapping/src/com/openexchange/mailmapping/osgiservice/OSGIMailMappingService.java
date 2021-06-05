/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mailmapping.osgiservice;

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
 * The {@link OSGIMailMappingService} is a utility class for consulting mail mapping services
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added constructor
 * @deprecated Use {@link MailResolverService}!
 */
@Deprecated
public class OSGIMailMappingService implements MultipleMailResolver, SimpleRegistryListener<MailResolver> {

    private final ServiceSet<MailResolver> chain;

    /**
     * Initializes a new {@link OSGIMailMappingService}.
     */
    public OSGIMailMappingService() {
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
