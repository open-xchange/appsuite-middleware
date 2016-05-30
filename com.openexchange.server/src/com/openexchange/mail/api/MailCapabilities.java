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

package com.openexchange.mail.api;

/**
 * {@link MailCapabilities} - Holds capabilities of the underlying mail system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailCapabilities {

    /**
     * A constant to signal empty capabilities
     */
    public static final MailCapabilities EMPTY_CAPS = new MailCapabilities() {

        @Override
        public boolean hasPermissions() {
            return false;
        }

        @Override
        public boolean hasQuota() {
            return false;
        }

        @Override
        public boolean hasSort() {
            return false;
        }

        @Override
        public boolean hasSubscription() {
            return false;
        }

        @Override
        public boolean hasThreadReferences() {
            return false;
        }

        @Override
        public int getCapabilities() {
            return 0;
        }

        @Override
        public String toString() {
            return "Empty mail capabilities";
        }

        @Override
        public boolean hasFileNameSearch() {
            return false;
        };

    };

    /*-
     * Bit Constants
     */

    /**
     * The bit for permission support: <code>1</code>
     */
    public static final int BIT_PERMISSIONS;

    /**
     * The bit for thread reference support: <code>2</code>
     */
    public static final int BIT_THREAD_REFERENCES;

    /**
     * The bit for quota support: <code>4</code>
     */
    public static final int BIT_QUOTA;

    /**
     * The bit for sorting support: <code>8</code>
     */
    public static final int BIT_SORT;

    /**
     * The bit for subscription support: <code>16</code>
     */
    public static final int BIT_SUBSCRIPTION;

    /**
     * The next available shift operand which can be used in sub-classes to declare own bit constants; e.g.:
     *
     * <pre>
     *
     * private static final int BIT_CUSTOM1 = 1 &lt;&lt; NEXT_SHIFT_OPERAND;
     *
     * private static final int BIT_CUSTOM2 = 1 &lt;&lt; (NEXT_SHIFT_OPERAND + 1);
     * </pre>
     */
    protected static final int NEXT_SHIFT_OPERAND;

    static {
        int shiftOperand = 0;
        BIT_PERMISSIONS = 1 << shiftOperand++;
        BIT_THREAD_REFERENCES = 1 << shiftOperand++;
        BIT_QUOTA = 1 << shiftOperand++;
        BIT_SORT = 1 << shiftOperand++;
        BIT_SUBSCRIPTION = 1 << shiftOperand++;
        NEXT_SHIFT_OPERAND = shiftOperand;
    }

    /*-
     * Member section
     */

    /**
     * Initializes a new {@link MailCapabilities}
     */
    protected MailCapabilities() {
        super();
    }

    /**
     * Indicates if mail system supports any kind of folder permissions to define access rights for certain users to a mail folder.
     * <p>
     * Therefore this capability indicates if mail system supports shared/public folders.
     *
     * @return <code>true</code> if mail system supports any kind of mail permissions; otherwise <code>false</code>
     */
    public abstract boolean hasPermissions();

    /**
     * Indicates if mail system supports sorting messages in a certain mail folder by their communication thread reference.
     *
     * @return <code>true</code> if mail system supports sorting by communication thread reference; otherwise <code>false</code>
     */
    public abstract boolean hasThreadReferences();

    /**
     * Indicates if mail system supports user-specific quota restrictions on resources like storage space.
     *
     * @return <code>true</code> if mail system supports user-specific quota restrictions; otherwise <code>false</code>
     */
    public abstract boolean hasQuota();

    /**
     * Indicates if mail system supports sorting messages in a certain mail folder.
     *
     * @return <code>true</code> if mail system supports sorting; otherwise <code>false</code>
     */
    public abstract boolean hasSort();

    /**
     * Indicates if mail system supports subscription of mail folders.
     * <p>
     * Note: This capability is also takes the configuration setting {@link MailConfig#isSupportSubscription()} into consideration.
     *
     * @return <code>true</code> if mail system supports subscription; otherwise <code>false</code>
     */
    public abstract boolean hasSubscription();

    /**
     * Indicates if mail system supports search of attachment file names.
     * <p>
     * Defaults to <code>false</code>
     * 
     * @return <code>true</code> if mail system supports search of attachment file names; otherwise <code>false</code>
     */
    public boolean hasFileNameSearch() {
        return false;
    }

    /**
     * Returns the capabilities as a bit mask.
     * <p>
     * Override to support additional capabilities:
     *
     * <pre>
     * &#064;Override
     * public int getCapabilities() {
     * 	int retval = super.getCapabilities()
     * 	// your capabilities added here
     * }
     * </pre>
     *
     * @return The capabilities as a bit mask
     */
    public int getCapabilities() {
        int retval = 0;
        retval |= hasPermissions() ? BIT_PERMISSIONS : 0;
        retval |= hasQuota() ? BIT_QUOTA : 0;
        retval |= hasSort() ? BIT_SORT : 0;
        retval |= hasThreadReferences() ? BIT_THREAD_REFERENCES : 0;
        retval |= hasSubscription() ? BIT_SUBSCRIPTION : 0;
        return retval;
    }

    @Override
    public String toString() {
        return new StringBuilder(64).append(MailCapabilities.class.getSimpleName()).append(": hasPermissions=").append(hasPermissions()).append(
            ", hasQuota=").append(hasQuota()).append(", hasSort=").append(hasSort()).append(", hasSubscription=").append(hasSubscription()).append(
            ", hasThreadReferences=").append(hasThreadReferences()).toString();
    }

}
