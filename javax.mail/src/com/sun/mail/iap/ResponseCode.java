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

package com.sun.mail.iap;

import java.util.Map;
import com.google.common.collect.ImmutableMap;

/**
 * {@link ResponseCode} - An enumeration of IMAP response code as outlined in
 * <a href="https://tools.ietf.org/html/rfc3501#section-7.1">RFC 350; section 7.1</a> and
 * <a href="https://tools.ietf.org/html/rfc5530">RFC 5530</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public enum ResponseCode {

    // ----------------------------------------------------- RFC 3501 -----------------------------------------------------------------

    /**
     * The human-readable text contains a special alert that MUST be
     * presented to the user in a fashion that calls the user's
     * attention to the message.
     */
    ALERT("ALERT"),
    /**
     * Optionally followed by a parenthesized list of charsets.  A
     * SEARCH failed because the given charset is not supported by
     * this implementation.  If the optional list of charsets is
     * given, this lists the charsets that are supported by this
     * implementation.
     */
    BADCHARSET("BADCHARSET"),
    /**
     * Followed by a list of capabilities.  This can appear in the
     * initial OK or PREAUTH response to transmit an initial
     * capabilities list.  This makes it unnecessary for a client to
     * send a separate CAPABILITY command if it recognizes this
     * response.
     */
    CAPABILITY("CAPABILITY"),
    /**
     * The human-readable text represents an error in parsing the
     * [RFC-2822] header or [MIME-IMB] headers of a message in the
     * mailbox.
     *
     * @see https://tools.ietf.org/html/rfc2822
     */
    PARSE("PARSE"),
    /**
     * Followed by a parenthesized list of flags, indicates which of
     * the known flags the client can change permanently.  Any flags
     * that are in the FLAGS untagged response, but not the
     * PERMANENTFLAGS list, can not be set permanently.  If the client
     * attempts to STORE a flag that is not in the PERMANENTFLAGS
     * list, the server will either ignore the change or store the
     * state change for the remainder of the current session only.
     * The PERMANENTFLAGS list can also include the special flag \*,
     * which indicates that it is possible to create new keywords by
     * attempting to store those flags in the mailbox.
     */
    PERMANENTFLAGS("PERMANENTFLAGS"),
    /**
     * The mailbox is selected read-only, or its access while selected
     * has changed from read-write to read-only.
     */
    READ_ONLY("READ-ONLY"),
    /**
     * The mailbox is selected read-write, or its access while
     * selected has changed from read-only to read-write.
     */
    READ_WRITE("READ-WRITE"),
    /**
     * Followed by a decimal number, indicates the next unique
     * identifier value.
     */
    UIDNEXT("UIDNEXT"),
    /**
     * Followed by a decimal number, indicates the unique identifier
     * validity value.
     */
    UIDVALIDITY("UIDVALIDITY"),
    /**
     * Followed by a decimal number, indicates the number of the first
     * message without the \Seen flag set.
     */
    UNSEEN("UNSEEN"),

    // ----------------------------------------------------- RFC 5530 -----------------------------------------------------------------

    /**
     * Temporary failure because a subsystem is down. For example, an
     * IMAP server that uses a Lightweight Directory Access Protocol
     * (LDAP) or Radius server for authentication might use this
     * response code when the LDAP/Radius server is down.
     *
     * <pre>
     * C: a LOGIN "fred" "foo"
     * S: a NO [UNAVAILABLE] User's backend down for maintenance
     * </pre>
     */
    UNAVAILABLE("UNAVAILABLE"),
    /**
     * Authentication failed for some reason on which the server is
     * unwilling to elaborate. Typically, this includes "unknown
     * user" and "bad password".
     *
     * This is the same as not sending any response code, except that
     * when a client sees AUTHENTICATIONFAILED, it knows that the
     * problem wasn't, e.g., UNAVAILABLE, so there's no point in
     * trying the same login/password again later.
     *
     * <pre>
     * C: b LOGIN "fred" "foo"
     * S: b NO [AUTHENTICATIONFAILED] Authentication failed
     * </pre>
     *
     */
    AUTHENTICATIONFAILED("AUTHENTICATIONFAILED"),
    /**
     * Authentication succeeded in using the authentication identity,
     * but the server cannot or will not allow the authentication
     * identity to act as the requested authorization identity. This
     * is only applicable when the authentication and authorization
     * identities are different.
     *
     * <pre>
     * C: c1 AUTHENTICATE PLAIN
     * [...]
     * S: c1 NO [AUTHORIZATIONFAILED] No such authorization-ID
     *
     * C: c2 AUTHENTICATE PLAIN
     * [...]
     * S: c2 NO [AUTHORIZATIONFAILED] Authenticator is not an admin
     * </pre>
     *
     */
    AUTHORIZATIONFAILED("AUTHORIZATIONFAILED"),
    /**
     *
     * Either authentication succeeded or the server no longer had the
     * necessary data; either way, access is no longer permitted using
     * that passphrase. The client or user should get a new
     * passphrase.
     *
     * <pre>
     * C: d login "fred" "foo"
     * S: d NO [EXPIRED] That password isn't valid any more
     * </pre>
     */
    EXPIRED("EXPIRED"),
    /**
     *
     * The operation is not permitted due to a lack of privacy. If
     * Transport Layer Security (TLS) is not in use, the client could
     * try STARTTLS (see Section 6.2.1 of [RFC3501]) and then repeat
     * the operation.
     *
     * <pre>
     * C: d login "fred" "foo"
     * S: d NO [PRIVACYREQUIRED] Connection offers no privacy
     *
     * C: d select inbox
     * S: d NO [PRIVACYREQUIRED] Connection offers no privacy
     * </pre>
     */
    PRIVACYREQUIRED("PRIVACYREQUIRED"),
    /**
     *
     * The user should contact the system administrator or support
     * desk.
     *
     * <pre>
     * C: e login "fred" "foo"
     * S: e OK [CONTACTADMIN]
     */
    CONTACTADMIN("CONTACTADMIN"),
    /**
     *
     * The access control system (e.g., Access Control List (ACL), see
     * [RFC4314]) does not permit this user to carry out an operation,
     * such as selecting or creating a mailbox.
     *
     * <pre>
     * C: f select "/archive/projects/experiment-iv"
     * S: f NO [NOPERM] Access denied
     * </pre>
     */
    NOPERM("NOPERM"),
    /**
     *
     * An operation has not been carried out because it involves
     * sawing off a branch someone else is sitting on. Someone else
     * may be holding an exclusive lock needed for this operation, or
     * the operation may involve deleting a resource someone else is
     * using, typically a mailbox.
     *
     * The operation may succeed if the client tries again later.
     *
     * <pre>
     * C: g delete "/archive/projects/experiment-iv"
     * S: g NO [INUSE] Mailbox in use
     * </pre>
     */
    INUSE("INUSE"),
    /**
     *
     * Someone else has issued an EXPUNGE for the same mailbox. The
     * client may want to issue NOOP soon. [RFC2180] discusses this
     * subject in depth.
     *
     * <pre>
     * C: h search from fred@example.com
     * S: * SEARCH 1 2 3 5 8 13 21 42
     * S: h OK [EXPUNGEISSUED] Search completed
     * </pre>
     */
    EXPUNGEISSUED("EXPUNGEISSUED"),
    /**
     *
     * The server discovered that some relevant data (e.g., the
     * mailbox) are corrupt. This response code does not include any
     * information about what's corrupt, but the server can write that
     * to its logfiles.
     *
     * <pre>
     * C: i select "/archive/projects/experiment-iv"
     * S: i NO [CORRUPTION] Cannot open mailbox
     * </pre>
     */
    CORRUPTION("CORRUPTION"),
    /**
     *
     * The server encountered a bug in itself or violated one of its
     * own invariants.
     *
     * <pre>
     * C: j select "/archive/projects/experiment-iv"
     * S: j NO [SERVERBUG] This should not happen
     * </pre>
     */
    SERVERBUG("SERVERBUG"),
    /**
     *
     * The server has detected a client bug. This can accompany all
     * of OK, NO, and BAD, depending on what the client bug is.
     *
     * <pre>
     * C: k1 select "/archive/projects/experiment-iv"
     * [...]
     * S: k1 OK [READ-ONLY] Done
     * C: k2 status "/archive/projects/experiment-iv" (messages)
     * [...]
     * S: k2 OK [CLIENTBUG] Done
     * </pre>
     */
    CLIENTBUG("CLIENTBUG"),
    /**
     * The operation violates some invariant of the server and can
     * never succeed.
     *
     * <pre>
     * C: l create "///////"
     * S: l NO [CANNOT] Adjacent slashes are not supported
     * </pre>
     *
     */
    CANNOT("CANNOT"),
    /**
     * The operation ran up against an implementation limit of some
     * kind, such as the number of flags on a single message or the
     * number of flags used in a mailbox.
     *
     * <pre>
     * C: m STORE 42 FLAGS f1 f2 f3 f4 f5 ... f250
     * S: m NO [LIMIT] At most 32 flags in one mailbox supported
     * </pre>
     *
     */
    LIMIT("LIMIT"),
    /**
     *
     * The user would be over quota after the operation. (The user
     * may or may not be over quota already.)
     *
     * Note that if the server sends OVERQUOTA but doesn't support the
     * IMAP QUOTA extension defined by [RFC2087], then there is a
     * quota, but the client cannot find out what the quota is.
     *
     * <pre>
     * C: n1 uid copy 1:* oldmail
     * S: n1 NO [OVERQUOTA] Sorry
     *
     * C: n2 uid copy 1:* oldmail
     * S: n2 OK [OVERQUOTA] You are now over your soft quota
     * </pre>
     */
    OVERQUOTA("OVERQUOTA"),
    /**
     *
     * The operation attempts to create something that already exists,
     * such as when the CREATE or RENAME directories attempt to create
     * a mailbox and there is already one of that name.
     *
     * <pre>
     * C: o RENAME this that
     * S: o NO [ALREADYEXISTS] Mailbox "that" already exists
     * </pre>
     */
    ALREADYEXISTS("ALREADYEXISTS"),
    /**
     *
     * The operation attempts to delete something that does not exist.
     * Similar to ALREADYEXISTS.
     *
     * <pre>
     * C: p RENAME this that
     * S: p NO [NONEXISTENT] No such mailbox
     * </pre>
     */
    NONEXISTENT("NONEXISTENT"),
    /**
     *
     * The operation tries to append or move a message to a mailbox,
     * which does not exist. The server MUST send the response code
     * "[TRYCREATE]" to give a hint to the client that it can attempt
     * a CREATE command and retry the APPEND/MOVE if the CREATE is
     * successful.
     *
     * <pre>
     * C: p MOVE 66466 SomeMailbox
     * S: p NO [TRYCREATE] Mailbox doesn't exist: SomeMailbox
     * </pre>
     */
    TRYCREATE("TRYCREATE"),

    ;

    private final String name;

    private ResponseCode(String name) {
        this.name = name;
    }

    /**
     * Gets this response code's name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    private static final Map<String, ResponseCode> CACHE;
    static {
        ImmutableMap.Builder<String, ResponseCode> builder = ImmutableMap.builder();
        for (ResponseCode rc : ResponseCode.values()) {
            builder.put(rc.name, rc);
        }
        CACHE = builder.build();
    }

    /**
     * Gets the response code for given name.
     *
     * @param name The name; either plain (<code>"INUSE"</code>) or with surrounding brackets (<code>"[INUSE]"</code>)
     * @return The associated response code or <code>null</code>
     */
    public static ResponseCode responseCodeFor(String name) {
        if (null == name) {
            return null;
        }
        String toLookUp = name;
        if (toLookUp.startsWith("[") && toLookUp.endsWith("]")) {
            int endIndex = toLookUp.length() - 1;
            if (endIndex > 1) {
                toLookUp = toLookUp.substring(1, endIndex);
            }
        }
        toLookUp = toUpperCase(toLookUp);
        return CACHE.get(toLookUp);
    }

    /** ASCII-wise upper-case */
    private static String toUpperCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        int length = chars.length();
        StringBuilder builder = null;
        for (int i = 0; i < length; i++) {
            char c = chars.charAt(i);
            if (null == builder) {
                if ((c >= 'a') && (c <= 'z')) {
                    builder = new StringBuilder(length);
                    if (i > 0) {
                        builder.append(chars, 0, i);
                    }
                    builder.append((char) (c & 0x5f));
                }
            } else {
                builder.append((c >= 'a') && (c <= 'z') ? (char) (c & 0x5f) : c);
            }
        }
        return null == builder ? chars.toString() : builder.toString();
    }
}
