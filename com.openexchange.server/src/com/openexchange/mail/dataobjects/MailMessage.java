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

package com.openexchange.mail.dataobjects;

import static com.openexchange.mail.mime.utils.MimeMessageUtility.decodeMultiEncodedHeader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.mime.HeaderName;
import com.openexchange.mail.mime.MessageHeaders;
import com.openexchange.mail.mime.PlainTextAddress;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.tools.TimeZoneUtils;

/**
 * {@link MailMessage} - Abstract super class for all {@link MailMessage} subclasses.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailMessage extends MailPart {

    private static final long serialVersionUID = 8585899349289256569L;

    private static final transient org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(MailMessage.class);

    private static final String HDR_REFERENCES = MessageHeaders.HDR_REFERENCES;
    private static final String HDR_MESSAGE_ID = MessageHeaders.HDR_MESSAGE_ID;
    private static final String HDR_IN_REPLY_TO = MessageHeaders.HDR_IN_REPLY_TO;

    /*-
     * ------------------- Flags ------------------------------
     */
    /**
     * This message has been answered. This flag is set by clients to indicate that this message has been answered to.
     *
     * @value 1
     */
    public static final int FLAG_ANSWERED = 1;

    /**
     * This message is marked deleted. Clients set this flag to mark a message as deleted. The expunge operation on a folder removes all
     * messages in that folder that are marked for deletion.
     *
     * @value 2
     */
    public static final int FLAG_DELETED = 1 << 1;

    /**
     * This message is a draft. This flag is set by clients to indicate that the message is a draft message.
     *
     * @value 4
     */
    public static final int FLAG_DRAFT = 1 << 2;

    /**
     * This message is flagged. No semantic is defined for this flag. Clients alter this flag.
     *
     * @value 8
     */
    public static final int FLAG_FLAGGED = 1 << 3;

    /**
     * This message is recent. Folder implementations set this flag to indicate that this message is new to this folder, that is, it has
     * arrived since the last time this folder was opened.
     * <p>
     * Clients cannot alter this flag.
     *
     * @value 16
     */
    public static final int FLAG_RECENT = 1 << 4;

    /**
     * This message is seen. This flag is implicitly set by the implementation when the this Message's content is returned to the client in
     * some form.
     *
     * @value 32
     */
    public static final int FLAG_SEEN = 1 << 5;

    /**
     * A special flag that indicates that this folder supports user defined flags.
     * <p>
     * Clients cannot alter this flag.
     *
     * @value 64
     */
    public static final int FLAG_USER = 1 << 6;

    /**
     * Virtual Spam flag
     *
     * @value 128
     */
    public static final int FLAG_SPAM = 1 << 7;

    /**
     * Virtual forwarded flag that marks this message as being forwarded.
     *
     * @value 256
     */
    public static final int FLAG_FORWARDED = 1 << 8;

    /**
     * Virtual read acknowledgment flag that marks this message as being notified for delivery.
     *
     * @value 512
     */
    public static final int FLAG_READ_ACK = 1 << 9;

    /*-
     * ------------------- User Flags ------------------------------
     */
    
    /**
     * The value of virtual forwarded flag.
     *
     * @value $Forwarded
     */
    public static final String USER_FORWARDED = "$Forwarded";

    /**
     * The value of virtual read acknowledgment flag.
     *
     * @value $MDNSent
     */
    public static final String USER_READ_ACK = "$MDNSent";
    
    /**
     * The value of virtual spam flag.
     *
     * @value $Junk
     */
    public static final String USER_SPAM = "$Junk";
    
    /*-
     * ------------------- Priority ------------------------------
     */
    /**
     * Highest priority
     */
    public static final int PRIORITY_HIGHEST = 1;

    /**
     * High priority
     */
    public static final int PRIORITY_HIGH = 2;

    /**
     * Normal priority
     */
    public static final int PRIORITY_NORMAL = 3;

    /**
     * Low priority
     */
    public static final int PRIORITY_LOW = 4;

    /**
     * Lowest priority
     */
    public static final int PRIORITY_LOWEST = 5;

    /*-
     * ------------------- Color Label ------------------------------
     */

    /**
     * The prefix for a mail message's color labels stored as a user flag
     */
    public static final String COLOR_LABEL_PREFIX = "$cl_";

    /**
     * The deprecated prefix for a mail message's color labels stored as a user flag
     */
    public static final String COLOR_LABEL_PREFIX_OLD = "cl_";

    /**
     * The <code>int</code> value for no color label
     */
    public static final int COLOR_LABEL_NONE = 0;

    /**
     * The <code>string</code> with all valid color flags whitespace seperated
     */
    private static final Set<String> ALL_COLOR_LABELS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("$cl_0", "$cl_1", "$cl_2", "$cl_3", "$cl_4", "$cl_5", "$cl_6", "$cl_7", "$cl_8", "$cl_9", "$cl_10", "cl_0", "cl_1", "cl_2", "cl_3", "cl_4", "cl_5", "cl_6", "cl_7", "cl_8", "cl_9", "cl_10")));

    /**
     * Determines the corresponding <code>int</code> value of a given color label's string representation.
     * <p>
     * A color label's string representation matches the pattern:<br>
     * &lt;value-of-{@link #COLOR_LABEL_PREFIX}&gt;&lt;color-label-int-value&gt;
     * <p>
     * &lt;value-of-{@link #COLOR_LABEL_PREFIX_OLD} &gt;&lt;color-label-int-value&gt; is also accepted.
     *
     * @param cl The color label's string representation
     * @return The color label's <code>int</code> value
     * @throws OXException
     */
    public static int getColorLabelIntValue(final String cl) throws OXException {
        if (!isColorLabel(cl)) {
            throw MailExceptionCode.UNKNOWN_COLOR_LABEL.create(cl);
        } else if (!isValidColorLabel(cl)) {
            return COLOR_LABEL_NONE;
        }
        try {
            return Integer.parseInt(cl.substring(cl.charAt(0) == '$' ? COLOR_LABEL_PREFIX.length() : COLOR_LABEL_PREFIX_OLD.length()));
        } catch (final NumberFormatException e) {
            throw MailExceptionCode.UNKNOWN_COLOR_LABEL.create(e, cl);
        }
    }

    /**
     * Tests if specified string matches a color label pattern.
     *
     * @param cl The string to check
     * @return <code>true</code> if specified string matches a color label pattern; otherwise <code>false</code>
     */
    public static boolean isColorLabel(final String cl) {
        return (cl != null && (cl.startsWith(MailMessage.COLOR_LABEL_PREFIX) || cl.startsWith(MailMessage.COLOR_LABEL_PREFIX_OLD)));
    }

    /**
     * Tests if specified string contains a valid color label
     *
     * @param cl The string to check
     * @return <code>true</code> if specified string is a valid color label; otherwise <code>false</code>
     */
    public static boolean isValidColorLabel(final String cl) {
        return ALL_COLOR_LABELS.contains(cl);
    }

    /**
     * Parses specified color label's string.
     * <p>
     * <b>Note</b> that this method assumes {@link #isColorLabel(String)} would return <code>true</code> for specified string.
     *
     * @param cl The color label's string
     * @param defaultValue The default value to return if parsing color label's <code>int</code> value fails
     * @return The color label's <code>int</code> value or <code>defaultValue</code> on failure.
     */
    public static int parseColorLabel(final String cl, final int defaultValue) {
        try {
            return Integer.parseInt(cl.substring('$' == cl.charAt(0) ? COLOR_LABEL_PREFIX.length() : COLOR_LABEL_PREFIX_OLD.length()));
        } catch (final NumberFormatException e) {
            LOG.debug("Invalid color label: {}", cl, e);
            return defaultValue;
        }
    }

    /**
     * Generates the color label's string representation from given <code>int</code> value.
     * <p>
     * A color label's string representation matches the pattern:<br>
     * &lt;value-of-{@link #COLOR_LABEL_PREFIX}&gt;&lt;color-label-int-value&gt;
     *
     * @param cl The color label's <code>int</code> value
     * @return The color abel's string representation
     */
    public static String getColorLabelStringValue(final int cl) {
        return new StringBuilder(COLOR_LABEL_PREFIX).append(cl).toString();
    }

    private static final InternetAddress[] EMPTY_ADDRS = new InternetAddress[0];

    /**
     * The flags.
     */
    private int flags;

    private boolean b_flags;

    /**
     * The previous \Seen state.
     */
    private boolean prevSeen;

    private boolean b_prevSeen;

    /**
     * References to other messages.
     */
    private String[] references;

    private boolean b_references;

    /**
     * From addresses.
     */
    private HashSet<InternetAddress> from;

    private boolean b_from;

    /**
     * To addresses.
     */
    private HashSet<InternetAddress> to;

    private boolean b_to;

    /**
     * Cc addresses.
     */
    private HashSet<InternetAddress> cc;

    private boolean b_cc;

    /**
     * Bcc addresses.
     */
    private HashSet<InternetAddress> bcc;

    private boolean b_bcc;

    /**
     * Reply-To addresses.
     */
    private HashSet<InternetAddress> replyTo;

    private boolean b_replyTo;

    /**
     * The level in a communication thread.
     */
    private int threadLevel;

    private boolean b_threadLevel;

    /**
     * The subject.
     */
    private String subject;

    private boolean b_subject;

    /**
     * The sent date (the <code>Date</code> header).
     */
    private Date sentDate;

    private boolean b_sentDate;

    /**
     * The (internal) received date.
     */
    private Date receivedDate;

    private boolean b_receivedDate;

    /**
     * User flags.
     */
    private HashSet<HeaderName> userFlags;

    private boolean b_userFlags;

    /**
     * The color label (set through an user flag).
     */
    private int colorLabel;

    private boolean b_colorLabel;

    /**
     * The priority (the <code>X-Priority</code> header).
     */
    private int priority;

    private boolean b_priority;

    /**
     * The <code>Disposition-Notification-To</code> header.
     */
    private InternetAddress dispositionNotification;

    private boolean b_dispositionNotification;

    /**
     * The message folder fullname/ID.
     */
    private String folder;

    private boolean b_folder;

    /**
     * The message's account ID.
     */
    private int accountId;

    private boolean b_accountId;

    /**
     * The message account's name.
     */
    private String accountName;

    private boolean b_accountName;

    /**
     * Whether an attachment is present or not.
     */
    private boolean hasAttachment;

    private boolean b_hasAttachment;

    /**
     * Whether a VCard should be appended or not.
     */
    private boolean appendVCard;

    private boolean b_appendVCard;

    /**
     * The number of recent messages in associated folder.
     */
    private int recentCount;

    private boolean b_recentCount;

    /**
     * The Message-Id header value.
     */
    private String messageId;

    private boolean b_messageId;

    /**
     * The original folder identifier
     */
    private String originalFolder;
    private boolean b_originalFolder;

    /**
     * The original identifier
     */
    private String originalId;
    private boolean b_originalId;

    /**
     * Default constructor
     */
    protected MailMessage() {
        super();
        priority = PRIORITY_NORMAL;
        colorLabel = COLOR_LABEL_NONE;
        accountId = MailAccount.DEFAULT_ID;
    }

    /**
     * Removes the personal parts from given addresses
     *
     * @param addrs The addresses to remove the personals from
     */
    protected void removePersonalsFrom(Set<InternetAddress> addrs) {
        if (null != addrs) {
            for (InternetAddress addr : addrs) {
                try {
                    addr.setPersonal(null);
                } catch (UnsupportedEncodingException e) {
                    // Cannot occur
                }
            }
        }
    }

    /**
     * Adds an email address to <i>From</i>.
     *
     * @param addr The address
     */
    public void addFrom(final InternetAddress addr) {
        if (null == addr) {
            b_from = true;
            return;
        } else if (null == from) {
            from = new LinkedHashSet<InternetAddress>();
            b_from = true;
        }
        from.add(addr);
    }

    /**
     * Adds email addresses to <i>From</i>.
     *
     * @param addrs The addresses
     */
    public void addFrom(final InternetAddress[] addrs) {
        if (null == addrs) {
            b_from = true;
            return;
        } else if (null == from) {
            from = new LinkedHashSet<InternetAddress>();
            b_from = true;
        }
        from.addAll(Arrays.asList(addrs));
    }

    /**
     * Adds email addresses to <i>From</i>.
     *
     * @param addrs The addresses
     */
    public void addFrom(final Collection<InternetAddress> addrs) {
        if (null == addrs) {
            b_from = true;
            return;
        } else if (null == from) {
            from = new LinkedHashSet<InternetAddress>();
            b_from = true;
        }
        from.addAll(addrs);
    }

    /**
     * @return <code>true</code> if <i>From</i> is set; otherwise <code>false</code>
     */
    public boolean containsFrom() {
        return b_from || containsHeader(MessageHeaders.HDR_FROM);
    }

    /**
     * Removes the <i>From</i> addresses.
     */
    public void removeFrom() {
        from = null;
        removeHeader(MessageHeaders.HDR_FROM);
        b_from = false;
    }

    /**
     * @return The <i>From</i> addresses.
     */
    public InternetAddress[] getFrom() {
        if (!b_from) {
            final String fromStr = getFirstHeader(MessageHeaders.HDR_FROM);
            if (fromStr == null) {
                return EMPTY_ADDRS;
            }
            try {
                addFrom(QuotedInternetAddress.parse(fromStr, true));
            } catch (final AddressException e) {
                LOG.debug("", e);
                addFrom(new PlainTextAddress(fromStr));
            }
        }
        return from == null ? EMPTY_ADDRS : from.toArray(new InternetAddress[from.size()]);
    }

    /**
     * Removes the personal parts from the <i>From</i> addresses.
     */
    public void removeFromPersonals() {
        removePersonalsFrom(this.from);
    }

    /**
     * Adds an email address to <i>To</i>.
     *
     * @param addr The address
     */
    public void addTo(final InternetAddress addr) {
        if (null == addr) {
            b_to = true;
            return;
        } else if (null == to) {
            to = new LinkedHashSet<InternetAddress>();
            b_to = true;
        }
        to.add(addr);
    }

    /**
     * Adds email addresses to <i>To</i>
     *
     * @param addrs The addresses
     */
    public void addTo(final InternetAddress[] addrs) {
        if (null == addrs) {
            b_to = true;
            return;
        } else if (null == to) {
            to = new LinkedHashSet<InternetAddress>();
            b_to = true;
        }
        to.addAll(Arrays.asList(addrs));
    }

    /**
     * Adds email addresses to <i>To</i>
     *
     * @param addrs The addresses
     */
    public void addTo(final Collection<InternetAddress> addrs) {
        if (null == addrs) {
            b_to = true;
            return;
        } else if (null == to) {
            to = new LinkedHashSet<InternetAddress>();
            b_to = true;
        }
        to.addAll(addrs);
    }

    /**
     * @return <code>true</code> if <i>To</i> is set; otherwise <code>false</code>
     */
    public boolean containsTo() {
        return b_to || containsHeader(MessageHeaders.HDR_TO);
    }

    /**
     * Removes the <i>To</i> addresses
     */
    public void removeTo() {
        to = null;
        removeHeader(MessageHeaders.HDR_TO);
        b_to = false;
    }

    /**
     * @return The <i>To</i> addresses
     */
    public InternetAddress[] getTo() {
        if (!b_to) {
            final String toStr = getFirstHeader(MessageHeaders.HDR_TO);
            if (toStr == null) {
                return EMPTY_ADDRS;
            }
            try {
                addTo(QuotedInternetAddress.parse(toStr, true));
            } catch (final AddressException e) {
                LOG.debug("", e);
                addTo(new PlainTextAddress(toStr));
            }
        }
        return to == null ? EMPTY_ADDRS : to.toArray(new InternetAddress[to.size()]);
    }

    /**
     * Removes the personal parts from the <i>To</i> addresses.
     */
    public void removeToPersonals() {
        removePersonalsFrom(this.to);
    }

    /**
     * Adds an email address to <i>Cc</i>
     *
     * @param addr The address
     */
    public void addCc(final InternetAddress addr) {
        if (null == addr) {
            b_cc = true;
            return;
        } else if (null == cc) {
            cc = new LinkedHashSet<InternetAddress>();
            b_cc = true;
        }
        cc.add(addr);
    }

    /**
     * Adds email addresses to <i>Cc</i>
     *
     * @param addrs The addresses
     */
    public void addCc(final InternetAddress[] addrs) {
        if (null == addrs) {
            b_cc = true;
            return;
        } else if (null == cc) {
            cc = new LinkedHashSet<InternetAddress>();
            b_cc = true;
        }
        cc.addAll(Arrays.asList(addrs));
    }

    /**
     * Adds email addresses to <i>Cc</i>
     *
     * @param addrs The addresses
     */
    public void addCc(final Collection<InternetAddress> addrs) {
        if (null == addrs) {
            b_cc = true;
            return;
        } else if (null == cc) {
            cc = new LinkedHashSet<InternetAddress>();
            b_cc = true;
        }
        cc.addAll(addrs);
    }

    /**
     * @return <code>true</code> if <i>Cc</i> is set; otherwise <code>false</code>
     */
    public boolean containsCc() {
        return b_cc || containsHeader(MessageHeaders.HDR_CC);
    }

    /**
     * Removes the <i>Cc</i> addresses
     */
    public void removeCc() {
        cc = null;
        removeHeader(MessageHeaders.HDR_CC);
        b_cc = false;
    }

    /**
     * @return The <i>Cc</i> addresses
     */
    public InternetAddress[] getCc() {
        if (!b_cc) {
            final String ccStr = getFirstHeader(MessageHeaders.HDR_CC);
            if (ccStr == null) {
                return EMPTY_ADDRS;
            }
            try {
                addCc(QuotedInternetAddress.parse(ccStr, true));
            } catch (final AddressException e) {
                LOG.debug("", e);
                addCc(new PlainTextAddress(ccStr));
            }
        }
        return cc == null ? EMPTY_ADDRS : cc.toArray(new InternetAddress[cc.size()]);
    }

    /**
     * Removes the personal parts from the <i>Cc</i> addresses.
     */
    public void removeCcPersonals() {
        removePersonalsFrom(this.cc);
    }

    /**
     * Adds an email address to <i>Bcc</i>
     *
     * @param addr The address
     */
    public void addBcc(final InternetAddress addr) {
        if (null == addr) {
            b_bcc = true;
            return;
        } else if (null == bcc) {
            bcc = new LinkedHashSet<InternetAddress>();
            b_bcc = true;
        }
        bcc.add(addr);
    }

    /**
     * Adds email addresses to <i>Bcc</i>
     *
     * @param addrs The addresses
     */
    public void addBcc(final InternetAddress[] addrs) {
        if (null == addrs) {
            b_bcc = true;
            return;
        } else if (null == bcc) {
            bcc = new LinkedHashSet<InternetAddress>();
            b_bcc = true;
        }
        bcc.addAll(Arrays.asList(addrs));
    }

    /**
     * Adds email addresses to <i>Bcc</i>
     *
     * @param addrs The addresses
     */
    public void addBcc(final Collection<InternetAddress> addrs) {
        if (null == addrs) {
            b_bcc = true;
            return;
        } else if (null == bcc) {
            bcc = new LinkedHashSet<InternetAddress>();
            b_bcc = true;
        }
        bcc.addAll(addrs);
    }

    /**
     * @return <code>true</code> if <i>Bcc</i> is set; otherwise <code>false</code>
     */
    public boolean containsBcc() {
        return b_bcc || containsHeader(MessageHeaders.HDR_BCC);
    }

    /**
     * Removes the <i>Bcc</i> addresses
     */
    public void removeBcc() {
        bcc = null;
        removeHeader(MessageHeaders.HDR_BCC);
        b_bcc = false;
    }

    /**
     * @return The <i>Bcc</i> addresses
     */
    public InternetAddress[] getBcc() {
        if (!b_bcc) {
            final String bccStr = getFirstHeader(MessageHeaders.HDR_BCC);
            if (bccStr == null) {
                return EMPTY_ADDRS;
            }
            try {
                addBcc(QuotedInternetAddress.parse(bccStr, true));
            } catch (final AddressException e) {
                LOG.debug("", e);
                addBcc(new PlainTextAddress(bccStr));
            }
        }
        return bcc == null ? EMPTY_ADDRS : bcc.toArray(new InternetAddress[bcc.size()]);
    }

    /**
     * Removes the personal parts from the <i>Bcc</i> addresses.
     */
    public void removeBccPersonals() {
        removePersonalsFrom(this.bcc);
    }

    /**
     * Gets all the recipient addresses for the message.<br>
     * Extracts the TO, CC, and BCC recipients.
     *
     * @return The recipients
     */
    public InternetAddress[] getAllRecipients() {
        Set<InternetAddress> set = new LinkedHashSet<InternetAddress>(6);
        set.addAll(Arrays.asList(getTo()));
        set.addAll(Arrays.asList(getCc()));
        set.addAll(Arrays.asList(getBcc()));
        return set.toArray(new InternetAddress[set.size()]);
    }

    /**
     * Adds an email address to <i>Reply-To</i>
     *
     * @param addr The address
     */
    public void addReplyTo(final InternetAddress addr) {
        if (null == addr) {
            b_replyTo = true;
            return;
        } else if (null == replyTo) {
            replyTo = new LinkedHashSet<InternetAddress>();
            b_replyTo = true;
        }
        replyTo.add(addr);
    }

    /**
     * Adds email addresses to <i>Reply-To</i>
     *
     * @param addrs The addresses
     */
    public void addReplyTo(final InternetAddress[] addrs) {
        if (null == addrs) {
            b_replyTo = true;
            return;
        } else if (null == replyTo) {
            replyTo = new LinkedHashSet<InternetAddress>();
            b_replyTo = true;
        }
        replyTo.addAll(Arrays.asList(addrs));
    }

    /**
     * Adds email addresses to <i>Reply-To</i>
     *
     * @param addrs The addresses
     */
    public void addReplyTo(final Collection<InternetAddress> addrs) {
        if (null == addrs) {
            b_replyTo = true;
            return;
        } else if (null == replyTo) {
            replyTo = new LinkedHashSet<InternetAddress>();
            b_replyTo = true;
        }
        replyTo.addAll(addrs);
    }

    /**
     * @return <code>true</code> if <i>Reply-To</i> is set; otherwise <code>false</code>
     */
    public boolean containsReplyTo() {
        return b_replyTo || containsHeader(MessageHeaders.HDR_REPLY_TO);
    }

    /**
     * Removes the <i>Reply-To</i> addresses
     */
    public void removeReplyTo() {
        replyTo = null;
        removeHeader(MessageHeaders.HDR_REPLY_TO);
        b_replyTo = false;
    }

    /**
     * @return The <i>Reply-To</i> addresses
     */
    public InternetAddress[] getReplyTo() {
        if (!b_replyTo) {
            final String replyToStr = getFirstHeader(MessageHeaders.HDR_REPLY_TO);
            if (replyToStr == null) {
                return EMPTY_ADDRS;
            }
            try {
                addReplyTo(QuotedInternetAddress.parse(replyToStr, true));
            } catch (final AddressException e) {
                LOG.debug("", e);
                addReplyTo(new PlainTextAddress(replyToStr));
            }
        }
        return replyTo == null ? EMPTY_ADDRS : replyTo.toArray(new InternetAddress[replyTo.size()]);
    }

    /**
     * Gets the flags
     *
     * @return the flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * @return <code>true</code> if flag \ANSWERED is set; otherwise <code>false</code>
     */
    public boolean isAnswered() {
        return ((flags & FLAG_ANSWERED) == FLAG_ANSWERED);
    }

    /**
     * @return <code>true</code> if flag \DELETED is set; otherwise <code>false</code>
     */
    public boolean isDeleted() {
        return ((flags & FLAG_DELETED) == FLAG_DELETED);
    }

    /**
     * @return <code>true</code> if flag \DRAFT is set; otherwise <code>false</code>
     */
    public boolean isDraft() {
        return ((flags & FLAG_DRAFT) == FLAG_DRAFT);
    }

    /**
     * @return <code>true</code> if flag \FLAGGED is set; otherwise <code>false</code>
     */
    public boolean isFlagged() {
        return ((flags & FLAG_FLAGGED) == FLAG_FLAGGED);
    }

    /**
     * @return <code>true</code> if flag \RECENT is set; otherwise <code>false</code>
     */
    public boolean isRecent() {
        return ((flags & FLAG_RECENT) == FLAG_RECENT);
    }

    /**
     * @return <code>true</code> if flag \SEEN is set; otherwise <code>false</code>
     */
    public boolean isSeen() {
        return ((flags & FLAG_SEEN) == FLAG_SEEN);
    }

    /**
     * @return <code>true</code> if flag \SEEN is not set; otherwise <code>false</code>
     */
    public boolean isUnseen() {
        return !isSeen();
    }

    /**
     * @return <code>true</code> if virtual spam flag is set; otherwise <code>false</code>
     */
    public boolean isSpam() {
        return ((flags & FLAG_SPAM) == FLAG_SPAM);
    }

    /**
     * @return <code>true</code> if forwarded flag is set; otherwise <code>false</code>
     */
    public boolean isForwarded() {
        return ((flags & FLAG_FORWARDED) == FLAG_FORWARDED);
    }

    /**
     * @return <code>true</code> if read acknowledgment flag is set; otherwise <code>false</code>
     */
    public boolean isReadAcknowledgment() {
        return ((flags & FLAG_READ_ACK) == FLAG_READ_ACK);
    }

    /**
     * @return <code>true</code> if flag \USER is set; otherwise <code>false</code>
     */
    public boolean isUser() {
        return ((flags & FLAG_USER) == FLAG_USER);
    }

    /**
     * @return <code>true</code> if flags is set; otherwise <code>false</code>
     */
    public boolean containsFlags() {
        return b_flags;
    }

    /**
     * Removes the flags
     */
    public void removeFlags() {
        flags = 0;
        b_flags = false;
    }

    /**
     * Sets the flags
     *
     * @param flags the flags to set
     */
    public void setFlags(final int flags) {
        this.flags = flags;
        b_flags = true;
    }

    /**
     * Sets a system flag
     *
     * @param flag The system flag to set
     * @param enable <code>true</code> to enable; otherwise <code>false</code>
     * @throws OXException If an illegal flag argument is specified
     */
    public void setFlag(final int flag, final boolean enable) throws OXException {
        if ((flag == 1) || ((flag % 2) != 0)) {
            throw MailExceptionCode.ILLEGAL_FLAG_ARGUMENT.create(Integer.valueOf(flag));
        }
        flags = enable ? (flags | flag) : (flags & ~flag);
        b_flags = true;
    }

    /**
     * Gets the previous \Seen state.
     * <p>
     * This flag is used when writing the message later on. There a check is performed whether header
     * <code>Disposition-Notification-To</code> is indicated or not.
     *
     * @return the previous \Seen state
     */
    public boolean isPrevSeen() {
        return prevSeen;
    }

    /**
     * @return <code>true</code> if previous \Seen state is set; otherwise <code>false</code>
     */
    public boolean containsPrevSeen() {
        return b_prevSeen;
    }

    /**
     * Removes the previous \Seen state
     */
    public void removePrevSeen() {
        prevSeen = false;
        b_prevSeen = false;
    }

    /**
     * Sets the previous \Seen state.
     * <p>
     * This flag is used when writing the message later on. There a check is performed whether header
     * <code>Disposition-Notification-To</code> is indicated or not.
     *
     * @param prevSeen the previous \Seen state to set
     */
    public void setPrevSeen(final boolean prevSeen) {
        this.prevSeen = prevSeen;
        b_prevSeen = true;
    }

    /**
     * Gets the threadLevel
     *
     * @return the threadLevel
     */
    public int getThreadLevel() {
        return threadLevel;
    }

    /**
     * @return <code>true</code> if threadLevel is set; otherwise <code>false</code>
     */
    public boolean containsThreadLevel() {
        return b_threadLevel;
    }

    /**
     * Removes the threadLevel
     */
    public void removeThreadLevel() {
        threadLevel = 0;
        b_threadLevel = false;
    }

    /**
     * Sets the threadLevel
     *
     * @param threadLevel the threadLevel to set
     */
    public void setThreadLevel(final int threadLevel) {
        this.threadLevel = threadLevel;
        b_threadLevel = true;
    }

    /**
     * Gets the subject
     *
     * @return the subject
     */
    public String getSubject() {
        if (!b_subject) {
            final String subjectStr = MimeMessageUtility.checkNonAscii(getFirstHeader(MessageHeaders.HDR_SUBJECT));
            if (subjectStr != null) {
                setSubject(decodeMultiEncodedHeader(subjectStr));
            }
        }
        return subject;
    }

    /**
     * @return <code>true</code> if subject is set; otherwise <code>false</code>
     */
    public boolean containsSubject() {
        return b_subject || containsHeader(MessageHeaders.HDR_SUBJECT);
    }

    /**
     * Removes the subject
     */
    public void removeSubject() {
        subject = null;
        removeHeader(MessageHeaders.HDR_SUBJECT);
        b_subject = false;
    }

    /**
     * Sets the subject
     *
     * @param subject the subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
        b_subject = true;
    }

    private static final MailDateFormat MAIL_DATE_FORMAT;

    static {
        MAIL_DATE_FORMAT = new MailDateFormat();
        MAIL_DATE_FORMAT.setTimeZone(TimeZoneUtils.getTimeZone("GMT"));
    }

    /**
     * Gets the sent date which corresponds to <i>Date</i> header
     *
     * @return the sent date
     */
    public Date getSentDate() {
        if (!b_sentDate) {
            final String sentDateStr = getFirstHeader(MessageHeaders.HDR_DATE);
            if (sentDateStr != null) {
                synchronized (MAIL_DATE_FORMAT) {
                    try {
                        final Date parsedDate = MAIL_DATE_FORMAT.parse(sentDateStr);
                        if (null != parsedDate) {
                            setSentDate(parsedDate);
                        }
                    } catch (final java.text.ParseException e) {
                        LOG.warn("Date string could not be parsed: {}", sentDateStr, e);
                    }
                }
            }
        }
        final Date sentDate = this.sentDate;
        return sentDate == null ? null : new Date(sentDate.getTime());
    }

    /**
     * @return <code>true</code> if sent date is set; otherwise <code>false</code>
     */
    public boolean containsSentDate() {
        return b_sentDate || containsHeader(MessageHeaders.HDR_DATE);
    }

    /**
     * Removes the sent date
     */
    public void removeSentDate() {
        sentDate = null;
        removeHeader(MessageHeaders.HDR_DATE);
        b_sentDate = false;
    }

    /**
     * Sets the sent date
     *
     * @param sentDate the sent date to set
     */
    public void setSentDate(final Date sentDate) {
        this.sentDate = sentDate == null ? null : new Date(sentDate.getTime());
        b_sentDate = true;
    }

    /**
     * Gets the received date which represents the internal timestamp set by mail server on arrival.
     *
     * @return The received date
     */
    public Date getReceivedDate() {
        final Date receivedDate = this.receivedDate;
        return receivedDate == null ? null : new Date(receivedDate.getTime());
    }

    /**
     * Gets the received date directly which represents the internal timestamp set by mail server on arrival.
     *
     * @return The received date
     */
    public Date getReceivedDateDirect() {
        return receivedDate;
    }

    /**
     * @return <code>true</code> if received date is set; otherwise <code>false</code>
     */
    public boolean containsReceivedDate() {
        return b_receivedDate;
    }

    /**
     * Removes the received date
     */
    public void removeReceivedDate() {
        receivedDate = null;
        b_receivedDate = false;
    }

    /**
     * Sets the received date
     *
     * @param receivedDate the received date to set
     */
    public void setReceivedDate(final Date receivedDate) {
        this.receivedDate = receivedDate == null ? null : new Date(receivedDate.getTime());
        b_receivedDate = true;
    }

    /**
     * Adds given user flag
     *
     * @param userFlag The user flag to add
     */
    public void addUserFlag(final String userFlag) {
        if (userFlag == null) {
            return;
        } else if (userFlags == null) {
            userFlags = new HashSet<HeaderName>();
            b_userFlags = true;
        }
        userFlags.add(HeaderName.valueOf(userFlag));
    }

    /**
     * Adds given user flags
     *
     * @param userFlags The user flags to add
     */
    public void addUserFlags(final String[] userFlags) {
        if (userFlags == null) {
            return;
        } else if (this.userFlags == null) {
            this.userFlags = new HashSet<HeaderName>();
            b_userFlags = true;
        }
        for (String userFlag : userFlags) {
            this.userFlags.add(HeaderName.valueOf(userFlag));
        }
    }

    /**
     * Adds given user flags
     *
     * @param userFlags The user flags to add
     */
    public void addUserFlags(final Collection<String> userFlags) {
        if (userFlags == null) {
            return;
        } else if (this.userFlags == null) {
            this.userFlags = new HashSet<HeaderName>();
            b_userFlags = true;
        }
        for (String userFlag : userFlags) {
            this.userFlags.add(HeaderName.valueOf(userFlag));
        }
    }

    /**
     * @return <code>true</code> if userFlags is set; otherwise <code>false</code>
     */
    public boolean containsUserFlags() {
        return b_userFlags;
    }

    /**
     * Removes the userFlags
     */
    public void removeUserFlags() {
        userFlags = null;
        b_userFlags = false;
    }

    private static final String[] EMPTY_UF = new String[0];

    /**
     * Gets the user flags
     *
     * @return The user flags
     */
    public String[] getUserFlags() {
        if (containsUserFlags() && (null != userFlags)) {
            if (userFlags.isEmpty()) {
                return EMPTY_UF;
            }
            final int size = userFlags.size();
            final List<String> retval = new ArrayList<String>(size);
            final Iterator<HeaderName> iter = userFlags.iterator();
            for (int i = 0; i < size; i++) {
                retval.add(iter.next().toString());
            }
            return retval.toArray(new String[size]);
        }
        return EMPTY_UF;
    }

    /**
     * Gets the color label
     *
     * @return the color label
     */
    public int getColorLabel() {
        return colorLabel;
    }

    /**
     * @return <code>true</code> if color label is set; otherwise <code>false</code>
     */
    public boolean containsColorLabel() {
        return b_colorLabel;
    }

    /**
     * Removes the color label
     */
    public void removeColorLabel() {
        colorLabel = COLOR_LABEL_NONE;
        b_colorLabel = false;
    }

    /**
     * Sets the color label
     *
     * @param colorLabel the color label to set
     */
    public void setColorLabel(final int colorLabel) {
        this.colorLabel = colorLabel;
        b_colorLabel = true;
    }

    /**
     * Gets the priority
     *
     * @return the priority
     */
    public int getPriority() {
        if (!b_priority) {
            final String imp = getFirstHeader(MessageHeaders.HDR_IMPORTANCE);
            if (imp != null) {
                setPriority(MimeMessageConverter.parseImportance(imp));
            } else {
                final String prioStr = getFirstHeader(MessageHeaders.HDR_X_PRIORITY);
                if (prioStr != null) {
                    setPriority(MimeMessageConverter.parsePriority(prioStr));
                }
            }
        }
        return priority;
    }

    /**
     * @return <code>true</code> if priority is set; otherwise <code>false</code>
     */
    public boolean containsPriority() {
        return b_priority || containsHeader(MessageHeaders.HDR_IMPORTANCE) || containsHeader(MessageHeaders.HDR_X_PRIORITY);
    }

    /**
     * Removes the priority
     */
    public void removePriority() {
        priority = PRIORITY_NORMAL;
        removeHeader(MessageHeaders.HDR_IMPORTANCE);
        removeHeader(MessageHeaders.HDR_X_PRIORITY);
        b_priority = false;
    }

    /**
     * Sets the priority
     *
     * @param priority the priority to set
     */
    public void setPriority(final int priority) {
        this.priority = priority;
        b_priority = true;
    }

    /**
     * Gets the dispositionNotification
     *
     * @return the dispositionNotification
     */
    public InternetAddress getDispositionNotification() {
        if (!b_dispositionNotification) {
            final String dispNotTo = getFirstHeader(MessageHeaders.HDR_DISP_NOT_TO);
            if (dispNotTo != null) {
                try {
                    setDispositionNotification(new QuotedInternetAddress(dispNotTo, false));
                } catch (final AddressException e) {
                    LOG.debug("", e);
                    setDispositionNotification(new PlainTextAddress(dispNotTo));
                }
            }
        }
        return dispositionNotification;
    }

    /**
     * @return <code>true</code> if dispositionNotification is set; otherwise <code>false</code>
     */
    public boolean containsDispositionNotification() {
        return b_dispositionNotification || containsHeader(MessageHeaders.HDR_DISP_NOT_TO);
    }

    /**
     * Removes the dispositionNotification
     */
    public void removeDispositionNotification() {
        dispositionNotification = null;
        removeHeader(MessageHeaders.HDR_DISP_NOT_TO);
        b_dispositionNotification = false;
    }

    /**
     * Sets the dispositionNotification
     *
     * @param dispositionNotification the dispositionNotification to set
     */
    public void setDispositionNotification(final InternetAddress dispositionNotification) {
        this.dispositionNotification = dispositionNotification;
        b_dispositionNotification = true;
    }

    /**
     * Gets the original folder
     *
     * @return the original folder
     */
    public String getOriginalFolder() {
        return originalFolder;
    }

    /**
     * @return <code>true</code> if original folder is set; otherwise <code>false</code>
     */
    public boolean containsOriginalFolder() {
        return b_originalFolder;
    }

    /**
     * Removes the original folder
     */
    public void removeOriginalFolder() {
        originalFolder = null;
        b_originalFolder = false;
    }

    /**
     * Sets the original folder
     *
     * @param originalFolder the original folder to set
     */
    public void setOriginalFolder(final String originalFolder) {
        this.originalFolder = originalFolder;
        b_originalFolder = true;
    }

    /**
     * Gets the original identifier
     *
     * @return the original identifier
     */
    public String getOriginalId() {
        return originalId;
    }

    /**
     * @return <code>true</code> if original identifier is set; otherwise <code>false</code>
     */
    public boolean containsOriginalId() {
        return b_originalId;
    }

    /**
     * Removes the original identifier
     */
    public void removeOriginalId() {
        originalId = null;
        b_originalId = false;
    }

    /**
     * Sets the original identifier
     *
     * @param originalId the original identifier to set
     */
    public void setOriginalId(final String originalId) {
        this.originalId = originalId;
        b_originalId = true;
    }

    /**
     * Gets the folder
     *
     * @return the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * @return <code>true</code> if folder is set; otherwise <code>false</code>
     */
    public boolean containsFolder() {
        return b_folder;
    }

    /**
     * Removes the folder
     */
    public void removeFolder() {
        folder = null;
        b_folder = false;
    }

    /**
     * Sets the folder
     *
     * @param folder the folder to set
     */
    public void setFolder(final String folder) {
        this.folder = folder;
        b_folder = true;
    }

    /**
     * Gets the account ID.
     *
     * @return The account ID
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * @return <code>true</code> if account ID is set; otherwise <code>false</code>
     */
    public boolean containsAccountId() {
        return b_accountId;
    }

    /**
     * Removes the account ID.
     */
    public void removeAccountId() {
        accountId = 0;
        b_accountId = false;
    }

    /**
     * Sets the account ID.
     *
     * @param accountId The account ID
     */
    public void setAccountId(final int accountId) {
        this.accountId = accountId;
        b_accountId = true;
    }

    /**
     * Gets the account Name
     *
     * @return The account name
     */
    public String getAccountName() {
        return accountName;
    }

    /**
     * @return <code>true</code> if account name is set; otherwise <code>false</code>
     */
    public boolean containsAccountName() {
        return b_accountName;
    }

    /**
     * Removes the account name.
     */
    public void removeAccountName() {
        accountName = null;
        b_accountName = false;
    }

    /**
     * Sets the account Name
     *
     * @param accountName The account name
     */
    public void setAccountName(final String accountName) {
        this.accountName = accountName;
        b_accountName = true;
    }

    /**
     * Gets the hasAttachment
     *
     * @return the hasAttachment
     */
    public boolean hasAttachment() {
        return hasAttachment;
    }

    /**
     * @return <code>true</code> if hasAttachment is set; otherwise <code>false</code>
     */
    public boolean containsHasAttachment() {
        return b_hasAttachment;
    }

    /**
     * Removes the hasAttachment
     */
    public void removeHasAttachment() {
        hasAttachment = false;
        b_hasAttachment = false;
    }

    /**
     * Sets the hasAttachment
     *
     * @param hasAttachment the hasAttachment to set
     */
    public void setHasAttachment(final boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
        b_hasAttachment = true;
    }

    @Override
    public Object clone() {
        final MailMessage clone = (MailMessage) super.clone();
        if (from != null) {
            clone.from = new LinkedHashSet<InternetAddress>(from);
        }
        if (to != null) {
            clone.to = new LinkedHashSet<InternetAddress>(to);
        }
        if (cc != null) {
            clone.cc = new LinkedHashSet<InternetAddress>(cc);
        }
        if (bcc != null) {
            clone.bcc = new LinkedHashSet<InternetAddress>(bcc);
        }
        if (receivedDate != null) {
            clone.receivedDate = new Date(receivedDate.getTime());
        }
        if (sentDate != null) {
            clone.sentDate = new Date(sentDate.getTime());
        }
        if (userFlags != null) {
            clone.userFlags = new HashSet<HeaderName>(userFlags);
        }
        return clone;
    }

    /**
     * Gets the appendVCard
     *
     * @return the appendVCard
     */
    public boolean isAppendVCard() {
        return appendVCard;
    }

    /**
     * @return <code>true</code> if appendVCard is set; otherwise <code>false</code>
     */
    public boolean containsAppendVCard() {
        return b_appendVCard;
    }

    /**
     * Removes the appendVCard
     */
    public void removeAppendVCard() {
        appendVCard = false;
        b_appendVCard = false;
    }

    /**
     * Sets the appendVCard
     *
     * @param appendVCard the appendVCard to set
     */
    public void setAppendVCard(final boolean appendVCard) {
        this.appendVCard = appendVCard;
        b_appendVCard = true;
    }

    /**
     * Gets the number of recent mails in associated folder.
     *
     * @return The recent count
     */
    public int getRecentCount() {
        return recentCount;
    }

    /**
     * @return <code>true</code> if number of recent mails is set; otherwise <code>false</code>
     */
    public boolean containsRecentCount() {
        return b_recentCount;
    }

    /**
     * Removes the recent count.
     */
    public void removeRecentCount() {
        recentCount = 0;
        b_recentCount = false;
    }

    /**
     * Sets the number of recent mails in associated folder.
     *
     * @param recentCount The recent count
     */
    public void setRecentCount(final int recentCount) {
        this.recentCount = recentCount;
        b_recentCount = true;
    }

    /**
     * Gets the mail path.
     *
     * @param accountId The account ID
     * @return The mail path
     */
    public MailPath getMailPath() {
        return new MailPath(getAccountId(), getFolder(), getMailId());
    }

    /**
     * Gets the <i>Message-Id</i> value.
     *
     * @return The <i>Message-Id</i> value or <code>null</code>
     */
    public String getMessageId() {
        if (!b_messageId) {
            final String messageId = getFirstHeader(HDR_MESSAGE_ID);
            if (messageId == null) {
                return null;
            }
            setMessageId(messageId);
        }
        return this.messageId;
    }

    /**
     * @return <code>true</code> if <i>Message-Id</i> is set; otherwise <code>false</code>
     */
    public boolean containsMessageId() {
        return b_messageId;
    }

    /**
     * Removes the <i>Message-Id</i>.
     */
    public void removeMessageId() {
        messageId = null;
        b_messageId = false;
    }

    /**
     * Sets the <i>Message-Id</i>.
     *
     * @param sReferences The <i>Message-Id</i> header value
     */
    public void setMessageId(final String messageId) {
        b_messageId = true;
        this.messageId = messageId;
    }

    /**
     * Gets the <i>In-Reply-To</i> value.
     *
     * @return The <i>In-Reply-To</i> value or <code>null</code>
     */
    public String getInReplyTo() {
        return getFirstHeader(HDR_IN_REPLY_TO);
    }

    private static final Pattern SPLIT = Pattern.compile(" +");

    /**
     * Gets the <i>References</i>.
     *
     * @return The <i>References</i> or <code>null</code>
     */
    public String[] getReferences() {
        if (!b_references) {
            final String references = getFirstHeader(HDR_REFERENCES);
            if (references == null) {
                return null;
            }
            setReferences(SPLIT.split(MimeMessageUtility.decodeMultiEncodedHeader(references)));
        }
        return this.references;
    }

    /**
     * Gets the <i>References</i> in first order, falls back to <i>In-Reply-To</i> value if absent
     *
     * @return Either the <i>References</i>/<i>In-Reply-To</i> value or <code>null</code> if none available
     */
    public String[] getReferencesOrInReplyTo() {
        String[] references = getReferences();
        if (null != references) {
            return references;
        }
        String inReplyTo = getInReplyTo();
        return null == inReplyTo ? null : new String[] { inReplyTo };
    }

    /**
     * @return <code>true</code> if <i>References</i> is set; otherwise <code>false</code>
     */
    public boolean containsReferences() {
        return b_references;
    }

    /**
     * Removes the <i>References</i>.
     */
    public void removeReferences() {
        references = null;
        b_references = false;
    }

    /**
     * Sets the <i>References</i>.
     *
     * @param sReferences The <i>References</i> header value
     */
    public void setReferences(final String sReferences) {
        if (null == sReferences) {
            this.references = null;
            b_references = true;
        } else {
            setReferences(SPLIT.split(MimeMessageUtility.decodeMultiEncodedHeader(sReferences)));
        }
    }

    /**
     * Sets the <i>References</i>.
     *
     * @param references The <i>References</i>
     */
    public void setReferences(final String[] references) {
        if (null == references) {
            this.references = null;
        } else {
            final int length = references.length;
            this.references = new String[length];
            System.arraycopy(references, 0, this.references, 0, length);
        }
        b_references = true;
    }

    /**
     * Gets the implementation-specific unique ID of this mail in its mail folder. The ID returned by this method is used in storages to
     * refer to a mail.
     *
     * @return The ID of this mail or <code>null</code> if not available.
     */
    public abstract String getMailId();

    /**
     * Sets the implementation-specific unique mail ID of this mail in its mail folder. The ID returned by this method is used in storages
     * to refer to a mail.
     *
     * @param id The mail ID or <code>null</code> to indicate its absence
     */
    public abstract void setMailId(String id);

    /**
     * Gets the number of unread messages
     *
     * @return The number of unread messages
     */
    public abstract int getUnreadMessages();

    /**
     * Sets the number of unread messages
     *
     * @param unreadMessages The number of unread messages
     */
    public abstract void setUnreadMessages(int unreadMessages);
}
