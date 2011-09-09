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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mailaccount.internal;

import java.util.Map;
import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.cache.dynamic.impl.Refresher;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link MailAccountReloader} - Manages to reload the mail account into the cache if cache invalidates it.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class MailAccountReloader extends Refresher<MailAccount> implements MailAccount {

    private static final long serialVersionUID = -3246334631928688669L;

    /**
     * Cached delegate.
     */
    private MailAccount delegate;

    /**
     * Initializes a new {@link MailAccountReloader}.
     *
     * @throws OXException If initial load of the object fails.
     */
    public MailAccountReloader(final OXObjectFactory<MailAccount> factory, final String regionName) throws OXException {
        super(factory, regionName, true);
        delegate = refresh();
    }

    /**
     * @throws RuntimeException if refreshing fails.
     */
    private void updateDelegate() throws RuntimeException {
        try {
            delegate = refresh();
        } catch (final OXException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        updateDelegate();
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        updateDelegate();
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return "MailAccountReloader: " + delegate.toString();
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public String getLogin() {
        updateDelegate();
        return delegate.getLogin();
    }

    @Override
    public String generateMailServerURL() {
        updateDelegate();
        return delegate.generateMailServerURL();
    }

    @Override
    public int getMailPort() {
        updateDelegate();
        return delegate.getMailPort();
    }

    @Override
    public String getMailProtocol() {
        updateDelegate();
        return delegate.getMailProtocol();
    }

    @Override
    public String getMailServer() {
        updateDelegate();
        return delegate.getMailServer();
    }

    @Override
    public boolean isMailSecure() {
        updateDelegate();
        return delegate.isMailSecure();
    }

    @Override
    public String getName() {
        updateDelegate();
        return delegate.getName();
    }

    @Override
    public String getPassword() {
        updateDelegate();
        return delegate.getPassword();
    }

    @Override
    public String getPrimaryAddress() {
        updateDelegate();
        return delegate.getPrimaryAddress();
    }

    @Override
    public String getPersonal() {
        updateDelegate();
        return delegate.getPersonal();
    }

    @Override
    public String getReplyTo() {
        updateDelegate();
        return delegate.getReplyTo();
    }

    @Override
    public String generateTransportServerURL() {
        updateDelegate();
        return delegate.generateTransportServerURL();
    }

    @Override
    public int getTransportPort() {
        updateDelegate();
        return delegate.getTransportPort();
    }

    @Override
    public String getTransportProtocol() {
        updateDelegate();
        return delegate.getTransportProtocol();
    }

    @Override
    public String getTransportServer() {
        updateDelegate();
        return delegate.getTransportServer();
    }

    @Override
    public boolean isTransportSecure() {
        updateDelegate();
        return delegate.isTransportSecure();
    }

    @Override
    public int getUserId() {
        return delegate.getUserId();
    }

    @Override
    public boolean isDefaultAccount() {
        return delegate.isDefaultAccount();
    }

    @Override
    public String getConfirmedHam() {
        updateDelegate();
        return delegate.getConfirmedHam();
    }

    @Override
    public String getConfirmedSpam() {
        updateDelegate();
        return delegate.getConfirmedSpam();
    }

    @Override
    public String getDrafts() {
        updateDelegate();
        return delegate.getDrafts();
    }

    @Override
    public String getSent() {
        updateDelegate();
        return delegate.getSent();
    }

    @Override
    public String getSpam() {
        updateDelegate();
        return delegate.getSpam();
    }

    @Override
    public String getTrash() {
        updateDelegate();
        return delegate.getTrash();
    }

    @Override
    public String getSpamHandler() {
        updateDelegate();
        return delegate.getSpamHandler();
    }

    @Override
    public String getTransportLogin() {
        updateDelegate();
        return delegate.getTransportLogin();
    }

    @Override
    public String getTransportPassword() {
        updateDelegate();
        return delegate.getTransportPassword();
    }

    @Override
    public boolean isUnifiedINBOXEnabled() {
        updateDelegate();
        return delegate.isUnifiedINBOXEnabled();
    }

    @Override
    public String getConfirmedHamFullname() {
        updateDelegate();
        return delegate.getConfirmedHamFullname();
    }

    @Override
    public String getConfirmedSpamFullname() {
        updateDelegate();
        return delegate.getConfirmedSpamFullname();
    }

    @Override
    public String getDraftsFullname() {
        updateDelegate();
        return delegate.getDraftsFullname();
    }

    @Override
    public String getSentFullname() {
        updateDelegate();
        return delegate.getSentFullname();
    }

    @Override
    public String getSpamFullname() {
        updateDelegate();
        return delegate.getSpamFullname();
    }

    @Override
    public String getTrashFullname() {
        updateDelegate();
        return delegate.getTrashFullname();
    }

    @Override
    public void addProperty(final String name, final String value) {
        updateDelegate();
        delegate.addProperty(name, value);
    }

    @Override
    public Map<String, String> getProperties() {
        updateDelegate();
        return delegate.getProperties();
    }
}
