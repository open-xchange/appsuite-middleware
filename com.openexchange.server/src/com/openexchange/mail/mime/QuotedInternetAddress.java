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

package com.openexchange.mail.mime;

import static com.openexchange.java.Strings.toUpperCase;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.idn.IDNA;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.java.Strings;
import com.openexchange.java.util.MsisdnCheck;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.config.MailReloadable;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link QuotedInternetAddress} - A quoted version of {@link InternetAddress} originally written by <b>Bill Shannon</b> and <b>John
 * Mani</b>. Moreover this class supports <a href="http://en.wikipedia.org/wiki/Punycode">punycode</a>.
 * <p>
 * Quotes are added to encoded personal names to maintain them when converting to mail-safe version. Parental {@link InternetAddress} class
 * ignores quotes when when converting to mail-safe version:
 * <p>
 * <code>``"M&uuml;ller,&nbsp;Jan"&nbsp;&lt;mj@foo.de&gt;''</code><br>
 * is converted to<br>
 * <code>``=?UTF-8?Q?M=C3=BCller=2C_Jan?=&nbsp;&lt;mj@foo.de&gt;''</code>
 * <p>
 * This class maintains the quotes in mail-safe version:
 * <p>
 * <code>``"M&uuml;ller,&nbsp;Jan"&nbsp;&lt;mj@foo.de&gt;''</code><br>
 * is converted to<br>
 * <code>``=?UTF-8?Q?=22M=C3=BCller=2C_Jan=22?=&nbsp;&lt;mj@foo.de&gt;''</code>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuotedInternetAddress extends InternetAddress {

    private static final long serialVersionUID = -2523736473507495692L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QuotedInternetAddress.class);

    private static final boolean IGNORE_BOGUS_GROUP_NAME = getBooleanSystemProperty("mail.mime.address.ignorebogusgroupname", true);

    private static boolean getBooleanSystemProperty(final String name, final boolean def) {
        return Boolean.parseBoolean(System.getProperty(name, def ? "true" : "false"));
    }

    private static volatile Boolean preferSimpleAddressParsing;
    private static boolean preferSimpleAddressParsing() {
        Boolean tmp = preferSimpleAddressParsing;
        if (null == tmp) {
            synchronized (QuotedInternetAddress.class) {
                tmp = preferSimpleAddressParsing;
                if (null == tmp) {
                    boolean defaultValue = true;
                    ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    if (null == service) {
                        return defaultValue;
                    }

                    tmp = Boolean.valueOf(service.getBoolProperty("com.openexchange.mail.preferSimpleAddressParsing", defaultValue));
                    preferSimpleAddressParsing = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    static {
        MailReloadable.getInstance().addReloadable(new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                preferSimpleAddressParsing = null;
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.preferSimpleAddressParsing");
            }
        });
    }

    /**
     * Converts given array of {@link InternetAddress} to quoted addresses
     *
     * @param addrs The addresses to convert
     * @return The quoted addresses
     * @throws AddressException If conversion fails
     */
    public static InternetAddress[] toQuotedAddresses(final InternetAddress[] addrs) throws AddressException {
        if (null == addrs) {
            return null;
        }
        final InternetAddress[] ret = new InternetAddress[addrs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new QuotedInternetAddress(addrs[i]);
        }
        return ret;
    }

    /**
     * Parse the given comma separated sequence of addresses into {@link InternetAddress} objects. Addresses must follow RFC822 syntax.
     *
     * @param addresslist A comma separated address strings
     * @return An array of {@link InternetAddress} objects
     * @exception AddressException If the parse failed
     */
    public static InternetAddress[] parse(final String addresslist) throws AddressException {
        return parse(addresslist, true);
    }

    /**
     * Parse the given sequence of addresses into {@link InternetAddress} objects. If <code>strict</code> is false, simple email addresses
     * separated by spaces are also allowed. If <code>strict</code> is true, many (but not all) of the RFC822 syntax rules are enforced. In
     * particular, even if <code>strict</code> is true, addresses composed of simple names (with no "@domain" part) are allowed. Such
     * "illegal" addresses are not uncommon in real messages.
     * <p>
     * Non-strict parsing is typically used when parsing a list of mail addresses entered by a human. Strict parsing is typically used when
     * parsing address headers in mail messages.
     *
     * @param addresslist A comma separated address strings
     * @param strict <code>true</code> to enforce RFC822 syntax; otherwise <code>false</code>
     * @return An array of {@link InternetAddress} objects
     * @exception AddressException If the parse failed
     */
    public static InternetAddress[] parse(final String addresslist, final boolean strict) throws AddressException {
        if (preferSimpleAddressParsing()) {
            return parseSimple(addresslist, strict);
        }

        return parse0(addresslist, strict);
    }

    private static InternetAddress[] parse0(String addresslist, boolean strict) throws AddressException {
        try {
            return parse(addresslist, strict, false, true);
        } catch (AddressException e) {
            return parse(addresslist, strict, false, false);
        }
    }

    /**
     * Parse the given sequence of addresses into {@link InternetAddress} objects. If <code>strict</code> is false, the full syntax rules
     * for individual addresses are not enforced. If <code>strict</code> is true, many (but not all) of the RFC822 syntax rules are
     * enforced.
     * <p>
     * To better support the range of "invalid" addresses seen in real messages, this method enforces fewer syntax rules than the
     * <code>parse</code> method when the strict flag is false and enforces more rules when the strict flag is true. If the strict flag is
     * false and the parse is successful in separating out an email address or addresses, the syntax of the addresses themselves is not
     * checked.
     *
     * @param addresslist A comma separated address strings
     * @param strict <code>true</code> to enforce RFC822 syntax; otherwise <code>false</code>
     * @return An array of {@link InternetAddress} objects
     * @exception AddressException If the parse failed
     */
    public static InternetAddress[] parseHeader(final String addresslist, final boolean strict) throws AddressException {
        if (preferSimpleAddressParsing()) {
            return parseSimple(addresslist, strict);
        }

        try {
            return parse(addresslist, strict, true, true);
        } catch (AddressException e) {
            return parse(addresslist, strict, true, false);
        }
    }

    private static InternetAddress[] parseSimple(String str, boolean strict) throws AddressException {
        if (str.length() <= 0) {
            return new InternetAddress[0];
        }
        String[] addrs = Strings.splitByCommaNotInQuotes(str);
        List<InternetAddress> l = new ArrayList<InternetAddress>(addrs.length);
        for (String addr : addrs) {
            if (addr.lastIndexOf('<') < 0 && addr.indexOf("=?") >= 0) {
                addr = MimeMessageUtility.decodeMultiEncodedHeader(addr);
            } else if (addr.indexOf("'?= <") > 0) {
                // Expect something like: =?utf-8?Q?...'?= <jane@doe.org>
                String tmp = MimeMessageUtility.decodeMultiEncodedHeader(addr);

                // Check if personal part is surrounded by single-quotes
                if (tmp.startsWith("'")) {
                    int pos = tmp.indexOf("' <");
                    if (pos > 0) {
                        // Replace with double-quotes
                        addr = new StringBuilder(tmp.length()).append('"').append(tmp.substring(1, pos)).append("\" <").append(tmp.substring(pos + 3)).toString();
                    }
                }
            }


            l.add(new QuotedInternetAddress(addr, strict));
        }
        return l.toArray(new InternetAddress[l.size()]);
    }

    /*
     * RFC822 Address parser. XXX - This is complex enough that it ought to be a real parser, not this ad-hoc mess, and because of that,
     * this is not perfect. XXX - Deal with encoded Headers too.
     */
    private static InternetAddress[] parse(String str, boolean strict, boolean parseHdr, boolean decodeFirst) throws AddressException {
        int start, end, index, nesting;
        int start_personal = -1, end_personal = -1;
        String s = decodeFirst ? MimeMessageUtility.decodeMultiEncodedHeader(str) : str;
        int length = s.length();
        boolean ignoreErrors = parseHdr && !strict;
        List<InternetAddress> list = new LinkedList<InternetAddress>();

        boolean in_group = false; // we're processing a group term
        boolean route_addr = false; // address came from route-addr term
        boolean rfc822 = false; // looks like an RFC822 address
        QuotedInternetAddress qia;

        for (start = end = -1, index = 0; index < length; index++) {
            char c = s.charAt(index);
            switch (c) {
            case '(': // We are parsing a Comment. Ignore everything inside.
                // XXX - comment fields should be parsed as whitespace,
                // more than one allowed per address
                rfc822 = true;
                if (start >= 0 && end == -1) {
                    end = index;
                }
                final int pindex = index;
                for (index++, nesting = 1; index < length && nesting > 0; index++) {
                    c = s.charAt(index);
                    switch (c) {
                    case '\\':
                        index++; // skip both '\' and the escaped char
                        break;
                    case '(':
                        nesting++;
                        break;
                    case ')':
                        nesting--;
                        break;
                    default:
                        break;
                    }
                }
                if (nesting > 0) {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing ')'", s, index);
                    }
                    // pretend the first paren was a regular character and
                    // continue parsing after it
                    index = pindex + 1;
                    break;
                }
                index--; // point to closing paren
                if (start_personal == -1) {
                    start_personal = pindex + 1;
                }
                if (end_personal == -1) {
                    end_personal = index;
                }
                break;

            case ')':
                if (!ignoreErrors) {
                    throw new AddressException("Missing '('", s, index);
                }
                // pretend the left paren was a regular character and
                // continue parsing
                if (start == -1) {
                    start = index;
                }
                break;

            case '<':
                rfc822 = true;
                if (route_addr) {
                    if (!ignoreErrors) {
                        throw new AddressException("Extra route-addr", s, index);
                    }

                    // assume missing comma between addresses
                    if (start == -1) {
                        route_addr = false;
                        rfc822 = false;
                        start = end = -1;
                        break; // nope, nothing there
                    }
                    if (!in_group) {
                        // got a token, add this to our InternetAddress vector
                        if (end == -1) {
                            end = index;
                        }
                        final String addr = s.substring(start, end).trim();

                        qia = new QuotedInternetAddress();
                        qia.setAddress(toACE(addr));
                        if (start_personal >= 0) {
                            qia.encodedPersonal = unquote(s.substring(start_personal, end_personal).trim());
                        }
                        list.add(qia);

                        route_addr = false;
                        rfc822 = false;
                        start = end = -1;
                        start_personal = end_personal = -1;
                        // continue processing this new address...
                    }
                }

                final int rindex = index;
                boolean inquote = false;
                outf: for (index++; index < length; index++) {
                    c = s.charAt(index);
                    switch (c) {
                    case '\\': // XXX - is this needed?
                        index++; // skip both '\' and the escaped char
                        break;
                    case '"':
                        inquote ^= true;
                        break;
                    case '>':
                        if (inquote) {
                            continue;
                        }
                        break outf; // out of for loop
                    default:
                        break;
                    }
                }

                // did we find a matching quote?
                if (inquote) {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing '\"'", s, index);
                    }
                    // didn't find matching quote, try again ignoring quotes
                    // (e.g., ``<"@foo.com>'')
                    for (index = rindex + 1; index < length; index++) {
                        c = s.charAt(index);
                        if (c == '\\') {
                            index++; // skip both '\' and the escaped char
                        } else if (c == '>') {
                            break;
                        }
                    }
                }

                // did we find a terminating '>'?
                if (index >= length) {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing '>'", s, index);
                    }
                    // pretend the "<" was a regular character and
                    // continue parsing after it (e.g., ``<@foo.com'')
                    index = rindex + 1;
                    if (start == -1) {
                        start = rindex; // back up to include "<"
                    }
                    break;
                }

                if (!in_group) {
                    start_personal = start;
                    if (start_personal >= 0) {
                        end_personal = rindex;
                    }
                    start = rindex + 1;
                }
                route_addr = true;
                end = index;
                break;

            case '>':
                if (!ignoreErrors) {
                    throw new AddressException("Missing '<'", s, index);
                }
                // pretend the ">" was a regular character and
                // continue parsing (e.g., ``>@foo.com'')
                if (start == -1) {
                    start = index;
                }
                break;

            case '"': // parse quoted string
                final int qindex = index;
                rfc822 = true;
                if (start == -1) {
                    start = index;
                }
                outq: for (index++; index < length; index++) {
                    c = s.charAt(index);
                    switch (c) {
                    case '\\':
                        index++; // skip both '\' and the escaped char
                        break;
                    case '"':
                        break outq; // out of for loop
                    default:
                        break;
                    }
                }
                if (index >= length) {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing '\"'", s, index);
                    }
                    // pretend the quote was a regular character and
                    // continue parsing after it (e.g., ``"@foo.com'')
                    index = qindex + 1;
                }
                break;

            case '[': // a domain-literal, probably
                rfc822 = true;
                final int lindex = index;
                outb: for (index++; index < length; index++) {
                    c = s.charAt(index);
                    switch (c) {
                    case '\\':
                        index++; // skip both '\' and the escaped char
                        break;
                    case ']':
                        break outb; // out of for loop
                    default:
                        break;
                    }
                }
                if (index >= length) {
                    if (!ignoreErrors) {
                        throw new AddressException("Missing ']'", s, index);
                    }
                    // pretend the "[" was a regular character and
                    // continue parsing after it (e.g., ``[@foo.com'')
                    index = lindex + 1;
                }
                break;

            case ';':
                if (start == -1) {
                    route_addr = false;
                    rfc822 = false;
                    start = end = -1;
                    break; // nope, nothing there
                }
                if (in_group) {
                    in_group = false;
                    /*
                     * If parsing headers, but not strictly, peek ahead. If next char is "@", treat the group name like the local part of
                     * the address, e.g., "Undisclosed-Recipient:;@java.sun.com".
                     */
                    if (parseHdr && !strict && index + 1 < length && s.charAt(index + 1) == '@') {
                        break;
                    }
                    qia = new QuotedInternetAddress();
                    end = index + 1;
                    qia.setAddress(toACE(s.substring(start, end).trim()));
                    list.add(qia);

                    route_addr = false;
                    rfc822 = false;
                    start = end = -1;
                    start_personal = end_personal = -1;
                    break;
                }
                if (!ignoreErrors) {
                    throw new AddressException("Illegal semicolon, not in group", s, index);
                }

                // otherwise, parsing a header; treat semicolon like comma
                // fall through to comma case...
                //$FALL-THROUGH$

            case ',': // end of an address, probably
                if (start == -1) {
                    route_addr = false;
                    rfc822 = false;
                    start = end = -1;
                    break; // nope, nothing there
                }
                if (in_group) {
                    route_addr = false;
                    break;
                }
                // got a token, add this to our InternetAddress vector
                if (end == -1) {
                    end = index;
                }

                String addr = s.substring(start, end).trim();
                String pers = null;
                if (rfc822 && start_personal >= 0) {
                    pers = unquote(s.substring(start_personal, end_personal).trim());
                    if (isEmpty(pers)) {
                        pers = null;
                    }
                }

                /*
                 * If the personal name field has an "@" and the address field does not, assume they were reversed, e.g., ``"joe doe"
                 * (john.doe@example.com)''.
                 */
                if (parseHdr && !strict && pers != null && pers.indexOf('@') >= 0 && addr.indexOf('@') < 0 && addr.indexOf('!') < 0) {
                    final String tmp = addr;
                    addr = pers;
                    pers = tmp;
                }
                if (rfc822 || strict || parseHdr) {
                    final String ace = toACE(addr);
                    if (!ignoreErrors) {
                        checkAddress(ace, route_addr, false);
                    }
                    qia = new QuotedInternetAddress();
                    qia.setAddress(ace);
                    if (pers != null) {
                        qia.encodedPersonal = pers;
                    }
                    list.add(qia);
                } else {
                    // maybe we passed over more than one space-separated addr
                    final StringTokenizer st = new StringTokenizer(addr);
                    while (st.hasMoreTokens()) {
                        final String a = st.nextToken();
                        final String ace = toACE(a);
                        checkAddress(ace, false, false);
                        qia = new QuotedInternetAddress();
                        qia.setAddress(ace);
                        list.add(qia);
                    }
                }

                route_addr = false;
                rfc822 = false;
                start = end = -1;
                start_personal = end_personal = -1;
                break;

            case ':':
                rfc822 = true;
                if (in_group) {
                    if (!ignoreErrors) {
                        throw new AddressException("Nested group", s, index);
                    }
                }
                if (start == -1) {
                    start = index;
                }
                if (parseHdr && !strict) {
                    /*
                     * If next char is a special character that can't occur at the start of a valid address, treat the group name as the
                     * entire address, e.g., "Date:, Tue", "Re:@foo".
                     */
                    if (index + 1 < length) {
                        final String addressSpecials = ")>[]:@\\,.";
                        char nc = s.charAt(index + 1);
                        if (addressSpecials.indexOf(nc) >= 0) {
                            if (nc != '@') {
                                break; // don't change in_group
                            }
                            /*
                             * Handle a common error: ``Undisclosed-Recipient:@example.com;'' Scan ahead. If we find a semicolon before one
                             * of these other special characters, consider it to be a group after all.
                             */
                            for (int i = index + 2; i < length; i++) {
                                nc = s.charAt(i);
                                if (nc == ';') {
                                    break;
                                }
                                if (addressSpecials.indexOf(nc) >= 0) {
                                    break;
                                }
                            }
                            if (nc == ';') {
                                break; // don't change in_group
                            }
                        }
                    }

                    // ignore bogus "mailto:" prefix in front of an address,
                    // or bogus mail header name included in the address field
                    final String gname = s.substring(start, index);
                    if (IGNORE_BOGUS_GROUP_NAME && (gname.equalsIgnoreCase("mailto") || gname.equalsIgnoreCase("From") || gname.equalsIgnoreCase("To") || gname.equalsIgnoreCase("Cc") || gname.equalsIgnoreCase("Subject") || gname.equalsIgnoreCase("Re"))) {
                        start = -1; // we're not really in a group
                    } else {
                        in_group = true;
                    }
                } else {
                    in_group = true;
                }
                break;

            // Ignore whitespace
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break;

            default:
                if (start == -1) {
                    start = index;
                }
                break;
            }
        }

        if (start >= 0) {
            /*
             * The last token, add this to our InternetAddress vector. Note that this block of code should be identical to the block above
             * for "case ','".
             */
            if (end == -1) {
                end = length;
            }

            String addr = s.substring(start, end).trim();
            String pers = null;
            if (rfc822 && start_personal >= 0) {
                pers = unquote(s.substring(start_personal, end_personal).trim());
                if (isEmpty(pers)) {
                    pers = null;
                }
            }

            /*
             * If the personal name field has an "@" and the address field does not, assume they were reversed, e.g., ``"joe doe"
             * (john.doe@example.com)''.
             */
            if (parseHdr && !strict && pers != null && pers.indexOf('@') >= 0 && addr.indexOf('@') < 0 && addr.indexOf('!') < 0) {
                final String tmp = addr;
                addr = pers;
                pers = tmp;
            }
            if (rfc822 || strict || parseHdr) {
                String ace = toACE(addr);
                ace = MimeMessageUtility.decodeMultiEncodedHeader(ace);
                if (!ignoreErrors) {
                    checkAddress(ace, route_addr, false);
                }
                qia = new QuotedInternetAddress();
                qia.setAddress(ace);
                if (pers != null) {
                    qia.encodedPersonal = pers;
                }
                list.add(qia);
            } else {
                // maybe we passed over more than one space-separated addr
                final StringTokenizer st = new StringTokenizer(addr);
                while (st.hasMoreTokens()) {
                    final String a = st.nextToken();
                    final String ace = toACE(a);
                    checkAddress(ace, false, false);
                    qia = new QuotedInternetAddress();
                    qia.setAddress(ace);
                    list.add(qia);
                }
            }
        }

        return list.toArray(new InternetAddress[list.size()]);
    }

    /**
     * Check that the address is a valid "mailbox" per RFC822. (We also allow simple names.) XXX - much more to check XXX - doesn't handle
     * domain-literals properly (but no one uses them)
     */
    private static void checkAddress(final String addr, final boolean routeAddr, final boolean validate) throws AddressException {
        int i, start = 0;

        final int len = addr.length();
        if (len == 0) {
            throw new AddressException("Empty address", addr.toString());
        }

        /*
         * routeAddr indicates that the address is allowed to have an RFC 822 "route".
         */
        if (routeAddr && addr.charAt(0) == '@') {
            /*
             * Check for a legal "route-addr": [@domain[,@domain ...]:]local@domain
             */
            for (start = 0; (i = indexOfAny(addr, ",:", start)) >= 0; start = i + 1) {
                if (addr.charAt(start) != '@') {
                    throw new AddressException("Illegal route-addr", addr.toString());
                }
                if (addr.charAt(i) == ':') {
                    // end of route-addr
                    start = i + 1;
                    break;
                }
            }
        }

        /*
         * The rest should be "local@domain", but we allow simply "local" unless called from validate. local-part must follow RFC 822 - no
         * specials except '.' unless quoted.
         */

        char c = (char) -1;
        char lastc = (char) -1;
        boolean inquote = false;
        for (i = start; i < len; i++) {
            lastc = c;
            c = addr.charAt(i);
            // a quoted-pair is only supposed to occur inside a quoted string,
            // but some people use it outside so we're more lenient
            if (c == '\\' || lastc == '\\') {
                continue;
            }
            if (c == '"') {
                if (inquote) {
                    // peek ahead, next char must be "@"
                    if (validate && i + 1 < len && addr.charAt(i + 1) != '@') {
                        throw new AddressException("Quote not at end of local address", addr.toString());
                    }
                    inquote = false;
                } else {
                    if (validate && i != 0) {
                        throw new AddressException("Quote not at start of local address", addr.toString());
                    }
                    inquote = true;
                }
                continue;
            }
            if (inquote) {
                continue;
            }
            if (c == '@') {
                if (i == 0) {
                    throw new AddressException("Missing local name", addr.toString());
                }
                break; // done with local part
            }
            if (c <= 32 || c >= 127) {
                throw new AddressException("Local address contains control/whitespace or non-ascii character", addr.toString());
            }
            if (SPECIALS_NO_DOT.indexOf(c) >= 0) {
                throw new AddressException("Local address contains illegal character", addr.toString());
            }
        }
        if (inquote) {
            throw new AddressException("Unterminated quote", addr.toString());
        }

        /*
         * Done with local part, now check domain. Note that the MimeMessage class doesn't remember addresses as separate objects; it writes
         * them out as headers and then parses the headers when the addresses are requested. In order to support the case where a "simple"
         * address is used, but the address also has a personal name and thus looks like it should be a valid RFC822 address when parsed, we
         * only check this if we're explicitly called from the validate method.
         */

        if (c != '@') {
            if (validate && !MsisdnCheck.checkMsisdn(addr)) {
                throw new AddressException("Missing final '@domain'", addr.toString());
            }
            return;
        }

        // check for illegal chars in the domain, but ignore domain literals

        start = i + 1;
        if (start >= len) {
            throw new AddressException("Missing domain", addr.toString());
        }

        if (addr.charAt(start) == '.') {
            throw new AddressException("Domain starts with dot", addr.toString());
        }
        for (i = start; i < len; i++) {
            c = addr.charAt(i);
            if (c == '[') {
                return; // domain literal, don't validate
            }
            if (c <= 32 || c >= 127) {
                throw new AddressException("Domain contains control or whitespace", addr.toString());
            }
            if (SPECIALS_NO_DOT.indexOf(c) >= 0) {
                throw new AddressException("Domain contains illegal character", addr.toString());
            }
            if (c == '.' && lastc == '.') {
                throw new AddressException("Domain contains dot-dot", addr.toString());
            }
            lastc = c;
        }
        if (lastc == '.') {
            throw new AddressException("Domain ends with dot", addr.toString());
        }
    }

    /**
     * Converts a unicode representation of an internet address to ASCII using the procedure in RFC3490 section 4.1. Unassigned characters
     * are not allowed and STD3 ASCII rules are enforced.
     * <p>
     * This implementation already supports EsZett character. Thanks to <a
     * href="http://blog.http.net/code/gnu-libidn-eszett-hotfix/">http.net</a>!
     * <p>
     * <code>"someone@m&uuml;ller.de"</code> is converted to <code>"someone@xn--mller-kva.de"</code>
     *
     * @param idnAddress The unicode representation of an internet address
     * @return The ASCII-encoded (punycode) of given internet address
     * @throws AddressException If ASCII representation of given internet address cannot be created
     */
    public static String toACE(final String idnAddress) throws AddressException {
        return IDNA.toACE(idnAddress);
    }

    /**
     * Converts an ASCII-encoded address to its unicode representation. Unassigned characters are not allowed and STD3 hostnames are
     * enforced.
     * <p>
     * This implementation already supports EsZett character. Thanks to <a
     * href="http://blog.http.net/code/gnu-libidn-eszett-hotfix/">http.net</a>!
     * <p>
     * <code>"someone@xn--mller-kva.de"</code> is converted to <code>"someone@m&uuml;ller.de"</code>
     *
     * @param aceAddress The ASCII-encoded (punycode) address
     * @return The unicode representation of given internet address
     * @see #getIDNAddress()
     */
    public static String toIDN(final String aceAddress) {
        return IDNA.toIDN(aceAddress);
    }

    private String jcharset;

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     */
    public QuotedInternetAddress() {
        super();
        jcharset = MailProperties.getInstance().getDefaultMimeCharset();
    }

    /**
     * Copy constructor.
     */
    private QuotedInternetAddress(final InternetAddress src) throws AddressException {
        this();
        address = toACE(src.getAddress());
        try {
            setPersonal(getPersonal(), null);
        } catch (final UnsupportedEncodingException e) {
            // Cannot occur
            throw new IllegalStateException("Unsupported default charset.");
        }
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * <p>
     * Parse the given string and create an InternetAddress. See the parse method for details of the parsing. The address is parsed using
     * "strict" parsing. This constructor does not perform the additional syntax checks that the InternetAddress(String address, boolean
     * strict) constructor does when strict is true. This constructor is equivalent to InternetAddress(address, false).
     *
     * @param address The address in RFC822 format
     * @throws AddressException If parsing the address fails
     */
    public QuotedInternetAddress(final String address) throws AddressException {
        super();
        parseAddress0(address);
        jcharset = MailProperties.getInstance().getDefaultMimeCharset();
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * <p>
     * Parse the given string and create an InternetAddress. If strict is <code>false</code>, the detailed syntax of the address isn't
     * checked. toACE
     *
     * @param address The address in RFC822 format
     * @param strict <code>true</code> enforce RFC822 syntax; otherwise <code>false</code>
     * @throws AddressException If parsing the address fails
     */
    public QuotedInternetAddress(final String address, final boolean strict) throws AddressException {
        this(init(address, true));
        if (strict) {
            if (isGroup()) {
                getGroup(true); // throw away the result
            } else {
                checkAddress(this.address, true, true);
            }
        }
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * <p>
     * Parse the given string and create an InternetAddress. If strict is <code>false</code>, the detailed syntax of the address isn't
     * checked. toACE
     *
     * @param address The address in RFC822 format
     * @param strict <code>true</code> enforce RFC822 syntax; otherwise <code>false</code>
     * @param suppressControlOrWhitespace Whether to suppress control or whitespace characters possibly contained in given address string
     * @throws AddressException If parsing the address fails
     */
    public QuotedInternetAddress(final String address, final boolean strict, final boolean suppressControlOrWhitespace) throws AddressException {
        this(init(address, suppressControlOrWhitespace));
        if (strict) {
            if (isGroup()) {
                getGroup(true); // throw away the result
            } else {
                checkAddress(this.address, true, true);
            }
        }
    }

    private static final Pattern WHITESPACE_OR_CONTROL = Pattern.compile("[\\p{Space}&&[^ ]]|\\p{Cntrl}|[^\\p{Print}\\p{L}]");

    private static String init(final String address, final boolean suppressControlOrWhitespace) {
        if (!suppressControlOrWhitespace) {
            return address;
        }
        return WHITESPACE_OR_CONTROL.matcher(address).replaceAll("");
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * <p>
     * Construct an instance given the address and personal name. The address is assumed to be a syntactically valid RFC822 address.
     *
     * @param address The address in RFC822 format
     * @param personal The personal name
     * @throws AddressException If parsing the address fails
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public QuotedInternetAddress(final String address, final String personal) throws AddressException, UnsupportedEncodingException {
        this(address, personal, null);
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     *
     * @param address The address in RFC822 format
     * @param personal The personal name
     * @param charset The MIME charset for the name
     * @throws AddressException If parsing the address fails
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public QuotedInternetAddress(final String address, final String personal, final String charset) throws AddressException, UnsupportedEncodingException {
        super();
        this.address = toACE(address);
        if (charset == null) {
            // use default charset
            jcharset = MailProperties.getInstance().getDefaultMimeCharset();
        } else {
            // MIME charset -> java charset
            String javaCharset = MimeUtility.javaCharset(charset);
            if ("utf8".equalsIgnoreCase(javaCharset)) {
                javaCharset = "UTF-8";
            }
            jcharset = javaCharset;
        }
        setPersonal(personal, charset);
    }

    /**
     * Parses the given string into this {@link QuotedInternetAddress}.
     *
     * @param address The address in RFC822 format
     * @throws AddressException If parsing the address fails
     */
    public void parseAddress(final String address) throws AddressException {
        parseAddress0(address);
    }

    /**
     * Internal parse routine.
     *
     * @param address The address in RFC822 format
     * @throws AddressException If parsing the address fails
     */
    private void parseAddress0(final String address) throws AddressException {
        InternetAddress[] a;
        try {
            // use our address parsing utility routine to parse the string
            a = parse0(address, true);

            // if we got back anything other than a single address, it's an error
            if (a.length != 1) {
                a = parse(address, true, false, false);
                if (a.length != 1) {
                    throw new AddressException("Illegal address", address);
                }
            }
        } catch (AddressException e) {
            // use our address parsing utility routine to parse the string
            a = parse(address, true, false, false);

            // if we got back anything other than a single address, it's an error
            if (a.length != 1) {
                throw new AddressException("Illegal address", address);
            }
        }

        /*
         * Now copy the contents of the single address we parsed into the current object, which will be returned from the constructor. XXX -
         * this sure is a round-about way of getting this done.
         */
        final QuotedInternetAddress internetAddress = (QuotedInternetAddress) a[0];
        this.address = internetAddress.address;
        personal = internetAddress.personal;
        encodedPersonal = internetAddress.encodedPersonal;
    }

    /**
     * Gets the email address in its internationalized, unicode form.
     *
     * @return The IDN email address
     * @see #toIDN(String)
     */
    public String getIDNAddress() {
        return toIDN(address);
    }

    @Override
    public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
        personal = name;
        if (name != null) {
            if (charset == null) {
                // use default charset
                jcharset = MailProperties.getInstance().getDefaultMimeCharset();
            } else {
                // MIME charset -> java charset
                String javaCharset = MimeUtility.javaCharset(charset);
                if ("utf8".equalsIgnoreCase(javaCharset)) {
                    javaCharset = "UTF-8";
                }
                jcharset = javaCharset;
            }
            encodedPersonal = MimeUtility.encodeWord(name, charset, null);
        } else {
            encodedPersonal = null;
        }
    }

    /**
     * Sets the email address.
     *
     * @param address The email address
     */
    @Override
    public void setAddress(final String address) {
        try {
            this.address = toACE(address);
        } catch (final AddressException e) {
            LOG.error("ACE string could not be parsed from IDN string: {}", address, e);
            this.address = address;
        }
    }

    /**
     * Gets the email address in Unicode characters.
     *
     * @return The email address in Unicode characters
     */
    public String getUnicodeAddress() {
        return toIDN(address);
    }

    /**
     * Get the personal name. If the name is encoded as per RFC 2047, it is decoded and converted into Unicode. If the decoding or
     * conversion fails, the raw data is returned as is.
     *
     * @return personal name
     */
    @Override
    public String getPersonal() {
        if (personal != null) {
            return personal;
        }

        if (encodedPersonal != null) {
            try {
                personal = MimeMessageUtility.decodeMultiEncodedHeader(encodedPersonal);
                return personal;
            } catch (final Exception ex) {
                // 1. ParseException: either its an unencoded string or
                // it can't be parsed
                // 2. UnsupportedEncodingException: can't decode it.
                return encodedPersonal;
            }
        }
        // No personal or encodedPersonal, return null
        return null;
    }

    /**
     * Convert this address into a RFC 822 / RFC 2047 encoded address. The resulting string contains only US-ASCII characters, and hence is
     * mail-safe.
     *
     * @return possibly encoded address string
     */
    @Override
    public String toString() {
        if (encodedPersonal == null && personal != null) {
            try {
                encodedPersonal = MimeUtility.encodeWord(personal, jcharset, null);
            } catch (final UnsupportedEncodingException ex) {
                LOG.error("", ex);
            }
        }

        if (encodedPersonal != null) {
            if (encodedPersonal.length() > 0) {
                if (null == personal) {
                    try {
                        personal = MimeUtility.decodeText(encodedPersonal);
                    } catch (final Exception ex) {
                        // 1. ParseException: either its an unencoded string or
                        // it can't be parsed
                        // 2. UnsupportedEncodingException: can't decode it.
                        personal = encodedPersonal;
                    }
                }

                if (quoted(personal)) {
                    if (checkQuotedPersonal(personal)) {
                        return new StringBuilder(32).append(encodedPersonal).append(" <").append(address).append('>').toString();
                    }
                    personal = personal.substring(1, personal.length() - 1);
                    try {
                        encodedPersonal = MimeUtility.encodeWord(personal, jcharset, null);
                    } catch (final UnsupportedEncodingException ex) {
                        LOG.error("", ex);
                    }
                }

                if (needQuoting(personal, true)) {
                    try {
                        encodedPersonal = MimeUtility.encodeWord(quotePhrase(personal, true), jcharset, null);
                    } catch (final UnsupportedEncodingException e) {
                        LOG.error("", e);
                    }
                } else if (!isAscii(personal)) {
                    try {
                        encodedPersonal = MimeUtility.encodeWord(quotePhrase(personal, true), jcharset, null);
                    } catch (final UnsupportedEncodingException e) {
                        LOG.error("", e);
                    }
                }
                return new StringBuilder(32).append(encodedPersonal).append(" <").append(address).append('>').toString();
            } else if (toUpperCase(address).endsWith("/TYPE=PLMN")) {
                return new StringBuilder().append('<').append(address).append('>').toString();
            } else if (isGroup() || isSimple()) {
                return address;
            } else {
                return new StringBuilder().append('<').append(address).append('>').toString();
            }
        } else if (isGroup() || isSimple()) {
            return address;
        } else {
            return new StringBuilder().append('<').append(address).append('>').toString();
        }
    }

    /**
     * Returns a properly formatted address (RFC 822 syntax) of Unicode characters.
     *
     * @return The Unicode address string
     */
    @Override
    public String toUnicodeString() {
        final String p = getPersonal();
        if (p != null) {
            if (p.length() > 0) {
                if (quoted(p)) {
                    return new StringBuilder(32).append(p).append(" <").append(toIDN(address)).append('>').toString();
                }
                return new StringBuilder(32).append(quotePhrase(p, true)).append(" <").append(toIDN(address)).append('>').toString();
            } else if (toUpperCase(address).endsWith("/TYPE=PLMN")) {
                return new StringBuilder().append('<').append(address).append('>').toString();
            } else if (isGroup() || isSimple()) {
                return toIDN(address);
            } else {
                return new StringBuilder(32).append('<').append(toIDN(address)).append('>').toString();
            }
        } else if (isGroup() || isSimple()) {
            return toIDN(address);
        } else {
            return new StringBuilder(32).append('<').append(toIDN(address)).append('>').toString();
        }
    }

    // @Override
    // public boolean equals(final Object a) {
    // if (this == a) {
    // return true;
    // }
    // if (!(a instanceof InternetAddress)) {
    // return false;
    // }
    // final String s = ((InternetAddress) a).getAddress();
    // if (address == null) {
    // if (s != null) {
    // return false;
    // }
    // } else if (!address.equalsIgnoreCase(s) && !toIDN(address).equalsIgnoreCase(s)) {
    // return false;
    // }
    // return true;
    // }

    /**
     * Is this a "simple" address? Simple addresses don't contain quotes or any RFC822 special characters other than '@' and '.'.
     */
    private boolean isSimple() {
        return null == address || indexOfAny(address, SPECIALS_NO_DOT_NO_AT) < 0;
    }

    /**
     * Return the first index of any of the characters in "any" in "s", or -1 if none are found. This should be a method on String.
     */
    private static int indexOfAny(final CharSequence s, final String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(final CharSequence s, final String any, final int start) {
        try {
            final int len = s.length();
            for (int i = start; i < len; i++) {
                if (any.indexOf(s.charAt(i)) >= 0) {
                    return i;
                }
            }
            return -1;
        } catch (final StringIndexOutOfBoundsException e) {
            return -1;
        }
    }

    private static final String SPECIALS_NO_DOT_NO_AT = "()<>,;:\\\"[]";

    private static final String SPECIALS_NO_DOT = "()<>@,;:\\\"[]";

    private final static String RFC822 = "()<>@,;:\\\".[]";

    private static String quotePhrase(final String phrase, final boolean allowNonAscii) {
        final int len = phrase.length();
        boolean needQuoting = false;

        for (int i = 0; i < len; i++) {
            final char c = phrase.charAt(i);
            if (c == '"' || c == '\\') {
                // need to escape them and then quote the whole string
                final StringBuilder sb = new StringBuilder(len + 3);
                sb.append('"');
                for (int j = 0; j < len; j++) {
                    final char cc = phrase.charAt(j);
                    if (cc == '"' || cc == '\\') {
                        // Escape the character
                        sb.append('\\');
                    }
                    sb.append(cc);
                }
                sb.append('"');
                return sb.toString();
            } else if ((c < 32 && c != '\r' && c != '\n' && c != '\t') || (!allowNonAscii && c >= 127) || RFC822.indexOf(c) >= 0) {
                // These characters cause the string to be quoted
                needQuoting = true;
            }
        }

        if (needQuoting) {
            final StringBuilder sb = new StringBuilder(len + 2);
            sb.append('"').append(phrase).append('"');
            return sb.toString();
        }
        return phrase;
    }

    private static boolean needQuoting(final String phrase, final boolean allowNonAscii) {
        final int len = phrase.length();
        boolean needQuoting = false;

        for (int i = 0; !needQuoting && i < len; i++) {
            final char c = phrase.charAt(i);
            if (c == '"' || c == '\\') {
                // need to escape them and then quote the whole string
                needQuoting = true;
            } else if ((c < 32 && c != '\r' && c != '\n' && c != '\t') || (!allowNonAscii && c >= 127) || RFC822.indexOf(c) >= 0) {
                // These characters cause the string to be quoted
                needQuoting = true;
            }
        }
        return needQuoting;
    }

    private static boolean quoted(final String s) {
        final int length = s.length();
        if (length <= 0) {
            return false;
        }
        return ('"' == s.charAt(0) && length > 1 && '"' == s.charAt(length - 1));
    }

    private static boolean checkQuotedPersonal(final String p) {
        // Every '"' and '\' needs a heading '\' character
        final String phrase = p.substring(1, p.length() - 1);
        final int len = phrase.length();
        boolean valid = true;

        int i = 0;
        while (valid && i < len) {
            final char c = phrase.charAt(i);
            if (c == '"') {
                valid = i > 1 && '\\' == phrase.charAt(i - 1);
                i++;
            } else if (c == '\\') {
                final int ni = i + 1;
                final char c2 = ni < len ? phrase.charAt(ni) : '\0';
                valid = (c2 == '"' || c2 == '\\');
                i += 2;
            } else {
                i++;
            }
        }

        return valid;
    }

    private static String unquote(final String str) {
        if (isEmpty(str)) {
            return str;
        }
        String s = str;
        int length = s.length();
        if (1 == length) {
            return str;
        }
        if ('"' == s.charAt(0) && '"' == s.charAt(length - 1)) {
            s = s.substring(1, length - 1);
            // check for any escaped characters
            if (s.indexOf('\\') >= 0) {
                length = length - 2;
                final StringBuilder sb = new StringBuilder(length); // approx
                for (int i = 0; i < length; i++) {
                    char c = s.charAt(i);
                    if (c == '\\' && i < length - 1) {
                        c = s.charAt(++i);
                    }
                    sb.append(c);
                }
                s = sb.toString();
            }
        }
        return s;
    }

    private static boolean isEmpty(final String str) {
        if (null == str || 0 == str.length()) {
            return true;
        }
        final int len = str.length();
        boolean ret = true;
        for (int i = 0; ret && i < len; i++) {
            ret = Strings.isWhitespace(str.charAt(i));
        }
        return ret;
    }

    /**
     * Determines whether a String is purely ASCII, meaning its characters' code points are all less than 128.
     */
    private static boolean isAscii(final String str) {
        if (null == str || 0 == str.length()) {
            return true;
        }
        final int len = str.length();
        boolean ret = true;
        for (int i = 0; ret && i < len; i++) {
            final char c = str.charAt(i);
            ret = (c > 32 && c <= 127);
        }
        return ret;
    }

}
