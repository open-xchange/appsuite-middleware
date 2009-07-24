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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

/**
 * {@link QuotedInternetAddress} - A quoted version of {@link InternetAddress}.
 * <p>
 * Quotes are added to encoded personal names to maintain them when converting to mail-safe version. Parental {@link InternetAddress} class
 * ignores quotes when when converting to mail-safe version:
 * <p>
 * <code>``"Müller,&nbsp;Jan"&nbsp;&lt;mj@foo.de&gt;''</code><br>
 * is converted to<br>
 * <code>``=?UTF-8?Q?M=C3=BCller=2C_Jan?=&nbsp;&lt;mj@foo.de&gt;''</code>
 * <p>
 * This class maintains the quotes in mail-safe version:
 * <p>
 * <code>``"Müller,&nbsp;Jan"&nbsp;&lt;mj@foo.de&gt;''</code><br>
 * is converted to<br>
 * <code>``=?UTF-8?Q?=22M=C3=BCller=2C_Jan=22?=&nbsp;&lt;mj@foo.de&gt;''</code>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuotedInternetAddress extends InternetAddress {

    private static final long serialVersionUID = -2523736473507495692L;

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(QuotedInternetAddress.class);

    private static final boolean ignoreBogusGroupName = getBooleanSystemProperty("mail.mime.address.ignorebogusgroupname", true);

    private static boolean getBooleanSystemProperty(final String name, final boolean def) {
        return Boolean.parseBoolean(System.getProperty(name, def ? "true" : "false"));
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
        return parse(addresslist, strict, false);
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
        return parse(addresslist, strict, true);
    }

    /*
     * RFC822 Address parser. XXX - This is complex enough that it ought to be a real parser, not this ad-hoc mess, and because of that,
     * this is not perfect. XXX - Deal with encoded Headers too.
     */
    private static InternetAddress[] parse(final String s, final boolean strict, final boolean parseHdr) throws AddressException {
        int start, end, index, nesting;
        int start_personal = -1, end_personal = -1;
        final int length = s.length();
        final boolean ignoreErrors = parseHdr && !strict;
        boolean in_group = false; // we're processing a group term
        boolean route_addr = false; // address came from route-addr term
        boolean rfc822 = false; // looks like an RFC822 address
        char c;
        final List<InternetAddress> list = new ArrayList<InternetAddress>();
        QuotedInternetAddress qia;

        for (start = end = -1, index = 0; index < length; index++) {
            c = s.charAt(index);

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
                        qia.setAddress(addr);
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
                        inquote = !inquote;
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
                    qia.setAddress(s.substring(start, end).trim());
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
                    if (pers.trim().length() == 0) {
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
                    if (!ignoreErrors) {
                        checkAddress(addr, route_addr, false);
                    }
                    qia = new QuotedInternetAddress();
                    qia.setAddress(addr);
                    if (pers != null) {
                        qia.encodedPersonal = pers;
                    }
                    list.add(qia);
                } else {
                    // maybe we passed over more than one space-separated addr
                    final StringTokenizer st = new StringTokenizer(addr);
                    while (st.hasMoreTokens()) {
                        final String a = st.nextToken();
                        checkAddress(a, false, false);
                        qia = new QuotedInternetAddress();
                        qia.setAddress(a);
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
                    if (ignoreBogusGroupName && (gname.equalsIgnoreCase("mailto") || gname.equalsIgnoreCase("From") || gname.equalsIgnoreCase("To") || gname.equalsIgnoreCase("Cc") || gname.equalsIgnoreCase("Subject") || gname.equalsIgnoreCase("Re"))) {
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
                if (pers.trim().length() == 0) {
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
                if (!ignoreErrors) {
                    checkAddress(addr, route_addr, false);
                }
                qia = new QuotedInternetAddress();
                qia.setAddress(addr);
                if (pers != null) {
                    qia.encodedPersonal = pers;
                }
                list.add(qia);
            } else {
                // maybe we passed over more than one space-separated addr
                final StringTokenizer st = new StringTokenizer(addr);
                while (st.hasMoreTokens()) {
                    final String a = st.nextToken();
                    checkAddress(a, false, false);
                    qia = new QuotedInternetAddress();
                    qia.setAddress(a);
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
            throw new AddressException("Empty address", addr);
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
                    throw new AddressException("Illegal route-addr", addr);
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
                        throw new AddressException("Quote not at end of local address", addr);
                    }
                    inquote = false;
                } else {
                    if (validate && i != 0) {
                        throw new AddressException("Quote not at start of local address", addr);
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
                    throw new AddressException("Missing local name", addr);
                }
                break; // done with local part
            }
            if (c <= 040 || c >= 0177) {
                throw new AddressException("Local address contains control or whitespace", addr);
            }
            if (specialsNoDot.indexOf(c) >= 0) {
                throw new AddressException("Local address contains illegal character", addr);
            }
        }
        if (inquote) {
            throw new AddressException("Unterminated quote", addr);
        }

        /*
         * Done with local part, now check domain. Note that the MimeMessage class doesn't remember addresses as separate objects; it writes
         * them out as headers and then parses the headers when the addresses are requested. In order to support the case where a "simple"
         * address is used, but the address also has a personal name and thus looks like it should be a valid RFC822 address when parsed, we
         * only check this if we're explicitly called from the validate method.
         */

        if (c != '@') {
            if (validate) {
                throw new AddressException("Missing final '@domain'", addr);
            }
            return;
        }

        // check for illegal chars in the domain, but ignore domain literals

        start = i + 1;
        if (start >= len) {
            throw new AddressException("Missing domain", addr);
        }

        if (addr.charAt(start) == '.') {
            throw new AddressException("Domain starts with dot", addr);
        }
        for (i = start; i < len; i++) {
            c = addr.charAt(i);
            if (c == '[') {
                return; // domain literal, don't validate
            }
            if (c <= 040 || c >= 0177) {
                throw new AddressException("Domain contains control or whitespace", addr);
            }
            if (specialsNoDot.indexOf(c) >= 0) {
                throw new AddressException("Domain contains illegal character", addr);
            }
            if (c == '.' && lastc == '.') {
                throw new AddressException("Domain contains dot-dot", addr);
            }
            lastc = c;
        }
        if (lastc == '.') {
            throw new AddressException("Domain ends with dot", addr);
        }
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     */
    public QuotedInternetAddress() {
        super();
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
        super(address);
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * <p>
     * Parse the given string and create an InternetAddress. If strict is <code>false</code>, the detailed syntax of the address isn't
     * checked.
     * 
     * @param address The address in RFC822 format
     * @param strict <code>true</code> enforce RFC822 syntax; otherwise <code>false</code>
     * @throws AddressException If parsing the address fails
     */
    public QuotedInternetAddress(final String address, final boolean strict) throws AddressException {
        super(address, strict);
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * <p>
     * Construct an instance given the address and personal name. The address is assumed to be a syntactically valid RFC822 address.
     * 
     * @param address The address in RFC822 format
     * @param personal The personal name
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public QuotedInternetAddress(final String address, final String personal) throws UnsupportedEncodingException {
        super(address, personal);
    }

    /**
     * Initializes a new {@link QuotedInternetAddress}.
     * 
     * @param address The address in RFC822 format
     * @param personal The personal name
     * @param charset The MIME charset for the name
     * @throws UnsupportedEncodingException If encoding is not supported
     */
    public QuotedInternetAddress(final String address, final String personal, final String charset) throws UnsupportedEncodingException {
        super(address, personal, charset);
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
                encodedPersonal = MimeUtility.encodeWord(personal);
            } catch (final UnsupportedEncodingException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        }

        if (encodedPersonal != null) {
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
                    encodedPersonal = MimeUtility.encodeWord(personal);
                } catch (final UnsupportedEncodingException ex) {
                    LOG.error(ex.getMessage(), ex);
                }
            }

            if (needQuoting(personal)) {
                try {
                    encodedPersonal = MimeUtility.encodeWord(quotePhrase(personal));
                } catch (final UnsupportedEncodingException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
            return new StringBuilder(32).append(encodedPersonal).append(" <").append(address).append('>').toString();

            /*-
             * 
            if (encodedPersonal.startsWith("=?", 0)) {
                return new StringBuilder(32).append('"').append(encodedPersonal).append("\" <").append(address).append('>').toString();
            }
            return new StringBuilder(32).append(quotePhrase(encodedPersonal)).append(" <").append(address).append('>').toString();
             */
        } else if (isGroup() || isSimple()) {
            return address;
        } else {
            return new StringBuilder().append('<').append(address).append('>').toString();
        }
    }

    /**
     * Returns a properly formatted address (RFC 822 syntax) of Unicode characters.
     * 
     * @return Unicode address string
     */
    @Override
    public String toUnicodeString() {
        final String p = getPersonal();
        if (p != null) {
            if (quoted(p)) {
                return new StringBuilder(32).append(p).append(" <").append(address).append('>').toString();
            }
            return new StringBuilder(32).append(quotePhrase(p)).append(" <").append(address).append('>').toString();
        } else if (isGroup() || isSimple()) {
            return address;
        } else {
            return new StringBuilder().append('<').append(address).append('>').toString();
        }
    }

    /**
     * Is this a "simple" address? Simple addresses don't contain quotes or any RFC822 special characters other than '@' and '.'.
     */
    private boolean isSimple() {
        return address == null || indexOfAny(address, specialsNoDotNoAt) < 0;
    }

    /**
     * Return the first index of any of the characters in "any" in "s", or -1 if none are found. This should be a method on String.
     */
    private static int indexOfAny(final String s, final String any) {
        return indexOfAny(s, any, 0);
    }

    private static int indexOfAny(final String s, final String any, final int start) {
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

    private static final String specialsNoDotNoAt = "()<>,;:\\\"[]";

    private static final String specialsNoDot = "()<>@,;:\\\"[]";

    private final static String RFC822 = "()<>@,;:\\\".[]";

    private static String quotePhrase(final String phrase) {
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
            } else if ((c < 040 && c != '\r' && c != '\n' && c != '\t') || c >= 0177 || RFC822.indexOf(c) >= 0) {
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

    private static boolean needQuoting(final String phrase) {
        final int len = phrase.length();
        boolean needQuoting = false;

        for (int i = 0; !needQuoting && i < len; i++) {
            final char c = phrase.charAt(i);
            if (c == '"' || c == '\\') {
                // need to escape them and then quote the whole string
                needQuoting = true;
            } else if ((c < 040 && c != '\r' && c != '\n' && c != '\t') || c >= 0177 || RFC822.indexOf(c) >= 0) {
                // These characters cause the string to be quoted
                needQuoting = true;
            }
        }
        return needQuoting;
    }

    private static boolean quoted(final String s) {
        return ('"' == s.charAt(0) && '"' == s.charAt(s.length() - 1));
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
                final char c2 = phrase.charAt(i + 1);
                valid = (c2 == '"' || c2 == '\\');
                i += 2;
            } else {
                i++;
            }
        }

        return valid;
    }

    private static String unquote(final String str) {
        String s = str;
        final int length = s.length();
        if ('"' == s.charAt(0) && '"' == s.charAt(length - 1)) {
            s = s.substring(1, length - 1);
            // check for any escaped characters
            if (s.indexOf('\\') >= 0) {
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

}
