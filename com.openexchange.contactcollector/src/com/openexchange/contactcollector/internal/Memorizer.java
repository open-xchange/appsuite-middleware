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

package com.openexchange.contactcollector.internal;

import static com.openexchange.mail.mime.QuotedInternetAddress.toIDN;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.contact.ContactFieldOperand;
import com.openexchange.contact.ContactService;
import com.openexchange.contactcollector.osgi.CCServiceRegistry;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ConstantOperand;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * {@link Memorizer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Memorizer implements Runnable {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ServerUserSetting.class));

    private static final boolean ALL_ALIASES = true;

    /*-
     * Member section
     */

    private final List<InternetAddress> addresses;

    private final Session session;

    private final TimeoutConcurrentMap<Integer, Future<Set<InternetAddress>>> aliasesMap;

    /**
     * Initializes a new {@link Memorizer}.
     *
     * @param addresses The addresses to insert if not already present
     * @param session The associated session
     * @param aliasesMap The aliases map holding already determined aliases per context
     */
    public Memorizer(final List<InternetAddress> addresses, final Session session, final TimeoutConcurrentMap<Integer, Future<Set<InternetAddress>>> aliasesMap) {
        this.addresses = addresses;
        this.session = session;
        this.aliasesMap = aliasesMap;
    }

    @Override
    public void run() {
        if (!isEnabled() || getFolderId() == 0) {
            return;
        }

        final Context ctx;
        final Set<InternetAddress> aliases;
        final UserConfiguration userConfig;
        try {
            final ServiceLookup serviceRegistry = CCServiceRegistry.getInstance();
            final ContextService contextService = serviceRegistry.getService(ContextService.class);
            if (null == contextService) {
                LOG.warn("Contact collector run aborted: missing context service");
                return;
            }
            ctx = contextService.getContext(session.getContextId());

            final UserService userService = serviceRegistry.getService(UserService.class);
            if (null == userService) {
                LOG.warn("Contact collector run aborted: missing user service");
                return;
            }
            if (ALL_ALIASES) {
                // All context-known users' aliases
                aliases = getContextAliases(ctx, userService);
            } else {
                // Only aliases of session user
                aliases = getAliases(userService.getUser(session.getUserId(), ctx));
            }

            final UserConfigurationService userConfigurationService = serviceRegistry.getService(UserConfigurationService.class);
            if (null == userConfigurationService) {
                LOG.warn("Contact collector run aborted: missing user configuration service");
                return;
            }
            userConfig = userConfigurationService.getUserConfiguration(session.getUserId(), ctx);
        } catch (final OXException e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        } catch (final Exception e) {
            LOG.error("Contact collector run aborted.", e);
            return;
        }
        /*
         * Iterate addresses
         */
        for (final InternetAddress address : addresses) {
            /*
             * Check if address is contained in user's aliases
             */
            if (!aliases.contains(address)) {
                try {
                    memorizeContact(address, ctx, userConfig);
                } catch (final OXException e) {
                    LOG.warn("Contact collector run aborted for address: " + address.toUnicodeString(), e);
                }
            }
        }
    }

    private static final ContactField[] FIELDS = {
    	ContactField.OBJECT_ID, ContactField.FOLDER_ID, ContactField.LAST_MODIFIED, ContactField.USE_COUNT };

	private static final ContactField[] SEARCH_FIELDS = { ContactField.EMAIL1, ContactField.EMAIL1, ContactField.EMAIL3 };

    private int memorizeContact(final InternetAddress address, final Context ctx, final UserConfiguration userConfig) throws OXException {
        /*
         * Convert email address to a contact
         */
        final Contact contact;
        try {
            contact = transformInternetAddress(address);
        } catch (final ParseException e) {
            // Decoding failed; ignore contact
            LOG.warn(e.getMessage(), e);
            return -1;
        } catch (final UnsupportedEncodingException e) {
            // Decoding failed; ignore contact
            LOG.warn(e.getMessage(), e);
            return -1;
        }
        /*
         * Check if such a contact already exists
         */
        final ContactService contactService = CCServiceRegistry.getInstance().getService(ContactService.class);
        final Contact foundContact;
        {
        	final CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
        	for (final ContactField field : SEARCH_FIELDS) {
        		final SingleSearchTerm term = new SingleSearchTerm(SingleOperation.EQUALS);
        		term.addOperand(new ContactFieldOperand(field));
        		term.addOperand(new ConstantOperand<String>(contact.getEmail1()));
        		orTerm.addSearchTerm(term);
        	}
        	final SearchIterator<Contact> iterator = contactService.searchContacts(session, orTerm, FIELDS);
            try {
                if (iterator.hasNext()) {
                    foundContact = iterator.next();
                } else {
                    foundContact = null;
                }
            } finally {
                iterator.close();
            }
        }
        /*
         * Either create contact or increment its use count
         */
        final int retval;
        if (null == foundContact) {
            final OXFolderAccess folderAccess = new OXFolderAccess(ctx);
            final int folderId = getFolderId();
            if (folderAccess.exists(folderId)) {
                final OCLPermission perm = folderAccess.getFolderPermission(folderId, session.getUserId(), userConfig);
                if (perm.canCreateObjects()) {
                    contact.setUseCount(1);
                    contactService.createContact(session, Integer.toString(contact.getParentFolderID()), contact);
                    retval = contact.getObjectID();
                } else {
                    retval = -1;
                }
            } else {
                retval = -1;
            }
        } else {
            final int currentCount = foundContact.getUseCount();
            final int newCount = currentCount + 1;
            foundContact.setUseCount(newCount);
            final OCLPermission perm =
                new OXFolderAccess(ctx).getFolderPermission(foundContact.getParentFolderID(), session.getUserId(), userConfig);
            if (perm.canWriteAllObjects()) {
                contactService.updateContact(session, Integer.toString(contact.getParentFolderID()),
                		Integer.toString(foundContact.getObjectID()), foundContact, foundContact.getLastModified());
            }
            retval = foundContact.getObjectID();
        }
        return retval;
    }

    /**
     * Gets the aliases of a specified user.
     *
     * @param user The user whose aliases shall be returned
     * @return The aliases of a specified user
     */
    static Set<InternetAddress> getAliases(final User user) {
        final String[] aliases = user.getAliases();
        if (null == aliases || aliases.length <= 0) {
            return Collections.emptySet();
        }
        final Set<InternetAddress> set = new HashSet<InternetAddress>(aliases.length);
        for (final String aliase : aliases) {
            try {
                set.add(new QuotedInternetAddress(aliase, false));
            } catch (final AddressException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(MessageFormat.format("Alias could not be parsed to an internet address: {0}", aliase), e);
                }
            }
        }
        return set;
    }

    private int getFolderId() {
        int retval = 0;
        try {
            final Integer folder = ServerUserSetting.getInstance().getContactCollectionFolder(session.getContextId(), session.getUserId());
            if (null != folder) {
                retval = folder.intValue();
            }
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        return retval;
    }

    private boolean isEnabled() {
        Boolean enabled = null;
        boolean enabledRight = false;
        try {
            enabled = ServerUserSetting.getInstance().isContactCollectionEnabled(session.getContextId(), session.getUserId());
            enabledRight = ServerSessionAdapter.valueOf(session).getUserPermissionBits().isCollectEmailAddresses();
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
        }
        return enabledRight && enabled != null && enabled.booleanValue();
    }

    private Contact transformInternetAddress(final InternetAddress address) throws ParseException, UnsupportedEncodingException {
        final Contact retval = new Contact();
        final String addr = decodeMultiEncodedValue(toIDN(address.getAddress()));
        retval.setEmail1(addr);
        final String displayName;
        if (address.getPersonal() != null && !"".equals(address.getPersonal().trim())) {
            displayName = decodeMultiEncodedValue(address.getPersonal());
        } else {
            displayName = addr;
        }
        retval.setDisplayName(displayName);
        retval.setParentFolderID(getFolderId());
        return retval;
    }

    private static final Pattern ENC_PATTERN = Pattern.compile("(=\\?\\S+?\\?\\S+?\\?)(.+?)(\\?=)");

    /**
     * Decodes a multi-mime-encoded value using the algorithm specified in RFC 2047, Section 6.1.
     * <p>
     * If the charset-conversion fails for any sequence, an {@link UnsupportedEncodingException} is thrown.
     * <p>
     * If the String is not a RFC 2047 style encoded value, it is returned as-is
     *
     * @param value The possibly encoded value
     * @return The possibly decoded value
     * @throws UnsupportedEncodingException If an unsupported charset encoding occurs
     * @throws ParseException If encoded value cannot be decoded
     */
    private static String decodeMultiEncodedValue(final String value) throws ParseException, UnsupportedEncodingException {
        if (value == null) {
            return null;
        }
        final String val = MimeUtility.unfold(value);
        final Matcher m = ENC_PATTERN.matcher(val);
        if (m.find()) {
            final com.openexchange.java.StringAllocator sa = new com.openexchange.java.StringAllocator(val.length());
            int lastMatch = 0;
            do {
                sa.append(val.substring(lastMatch, m.start()));
                sa.append(com.openexchange.java.Strings.quoteReplacement(MimeUtility.decodeWord(m.group())));
                lastMatch = m.end();
            } while (m.find());
            sa.append(val.substring(lastMatch));
            return sa.toString();
        }
        return val;
    }

    /**
     * Gets all aliases of all users of specified context.
     *
     * @param context The context
     * @param userService The user service
     * @return All aliases
     * @throws Exception If an error occurs
     */
    private Set<InternetAddress> getContextAliases(final Context context, final UserService userService) throws Exception {
        final Integer key = Integer.valueOf(context.getContextId());
        Future<Set<InternetAddress>> f = aliasesMap.get(key);
        if (null == f) {
            final FutureTask<Set<InternetAddress>> ft = new FutureTask<Set<InternetAddress>>(new Callable<Set<InternetAddress>>() {

                @Override
                public Set<InternetAddress> call() throws Exception {
                    // All context-known users' aliases
                    final int[] allUserIDs = userService.listAllUser(context);
                    final Set<InternetAddress> aliases = new HashSet<InternetAddress>(allUserIDs.length * 8);
                    for (final int allUserID : allUserIDs) {
                        aliases.addAll(getAliases(userService.getUser(allUserID, context)));
                    }
                    return aliases;
                }
            });
            // Put (if absent) with 5 minutes time-to-live.
            f = aliasesMap.putIfAbsent(key, ft, 300);
            if (f == null) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            // Cannot occur
            throw e;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new Exception(cause);
        }
    }

}
