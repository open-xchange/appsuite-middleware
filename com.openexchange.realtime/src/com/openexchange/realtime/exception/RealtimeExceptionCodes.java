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

package com.openexchange.realtime.exception;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link RealtimeStanzaExceptionCodes} - Stanza error codes for the realtime framework.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public enum RealtimeExceptionCodes implements DisplayableOXExceptionCode {
    /*
     * Define Channel specific Codes first as generic codes should be able to reference them.
     */

    // XMPP
    //--- The following conditions are defined for use in stanza errors. http://xmpp.org/rfcs/rfc3920.html#stanzas 9.3.3
    /*
    <bad-request/> -- the sender has sent XML that is malformed or that cannot be processed (e.g., an IQ stanza that includes an unrecognized value of the 'type' attribute); the associated error type SHOULD be "modify".
    <conflict/> -- access cannot be granted because an existing resource or session exists with the same name or address; the associated error type SHOULD be "cancel".
    <feature-not-implemented/> -- the feature requested is not implemented by the recipient or server and therefore cannot be processed; the associated error type SHOULD be "cancel".
    <forbidden/> -- the requesting entity does not possess the required permissions to perform the action; the associated error type SHOULD be "auth".
    <gone/> -- the recipient or server can no longer be contacted at this address (the error stanza MAY contain a new address in the XML character data of the <gone/> element); the associated error type SHOULD be "modify".
    <internal-server-error/> -- the server could not process the stanza because of a misconfiguration or an otherwise-undefined internal server error; the associated error type SHOULD be "wait".
    <item-not-found/> -- the addressed JID or item requested cannot be found; the associated error type SHOULD be "cancel".
    <jid-malformed/> -- the sending entity has provided or communicated an XMPP address (e.g., a value of the 'to' attribute) or aspect thereof (e.g., a resource identifier) that does not adhere to the syntax defined in Addressing Scheme; the associated error type SHOULD be "modify".
    <not-acceptable/> -- the recipient or server understands the request but is refusing to process it because it does not meet criteria defined by the recipient or server (e.g., a local policy regarding acceptable words in messages); the associated error type SHOULD be "modify".
    <not-allowed/> -- the recipient or server does not allow any entity to perform the action; the associated error type SHOULD be "cancel".
    <not-authorized/> -- the sender must provide proper credentials before being allowed to perform the action, or has provided improper credentials; the associated error type SHOULD be "auth".
    <payment-required/> -- the requesting entity is not authorized to access the requested service because payment is required; the associated error type SHOULD be "auth".
    <recipient-unavailable/> -- the intended recipient is temporarily unavailable; the associated error type SHOULD be "wait" (note: an application MUST NOT return this error if doing so would provide information about the intended recipient's network availability to an entity that is not authorized to know such information).
    <redirect/> -- the recipient or server is rediRealtimeExceptionCodesrecting requests for this information to another entity, usually temporarily (the error stanza SHOULD contain the alternate address, which MUST be a valid JID, in the XML character data of the <redirect/> element); the associated error type SHOULD be "modify".
    <registration-required/> -- the requesting entity is not authorized to access the requested service because registration is required; the associated error type SHOULD be "auth".
    <remote-server-not-found/> -- a remote server or service specified as part or all of the JID of the intended recipient does not exist; the associated error type SHOULD be "cancel".
    <remote-server-timeout/> -- a remote server or service specified as part or all of the JID of the intended recipient (or required to fulfill a request) could not be contacted within a reasonable amount of time; the associated error type SHOULD be "wait".
    <resource-constraint/> -- the server or recipient lacks the system resources necessary to service the request; the associated error type SHOULD be "wait".
    <service-unavailable/> -- the server or recipient does not currently provide the requested service; the associated error type SHOULD be "cancel".
    <subscription-required/> -- the requesting entity is not authorized to access the requested service because a subscription is required; the associated error type SHOULD be "auth".
    <undefined-condition/> -- the error condition is not one of those defined by the other conditions in this list; any error type may be associated with this condition, and it SHOULD be used only in conjunction with an application-specific condition.
    <unexpected-request/> -- the recipient or server understood the request but was not expecting it at this time (e.g., the request was out of order); the associated error type SHOULD be "wait".
    */
    //--- elements from stanza error namespace http://xmpp.org/rfcs/rfc3920.html#def C.7.

    /** "The client has sent a message that is malformed or cannot be processed: %1$s" */
    STANZA_BAD_REQUEST("The client has sent a message that is malformed or cannot be processed: %1$s", CATEGORY_USER_INPUT, 1, null),

    STANZA_CONFILCT("", CATEGORY_SERVICE_DOWN, 2, null),

    STANZA_FEATURE_NOT_IMPLEMENTED("", CATEGORY_SERVICE_DOWN, 3, null),

    STANZA_FORBIDDEN("", CATEGORY_SERVICE_DOWN, 4, null),

    STANZA_GONE("", CATEGORY_SERVICE_DOWN, 5, null),

    /** The server could not process the stanza because of a misconfiguration or an otherwise-undefined internal server error: %1$s */
    STANZA_INTERNAL_SERVER_ERROR("The server could not process the stanza because of a misconfiguration or an otherwise-undefined internal"
        + " server error: %1$s", CATEGORY_SERVICE_DOWN, 6, null),

    STANZA_ITEM_NOT_FOUND("", CATEGORY_SERVICE_DOWN, 7, null),

    STANZA_JID_MALFORMED("", CATEGORY_SERVICE_DOWN, 8, null),

    STANZA_NOT_ACCEPTABLE("", CATEGORY_SERVICE_DOWN, 9, null),

    STANZA_NOT_AUTHORIZED("", CATEGORY_SERVICE_DOWN, 10, null),

    STANZA_NOT_ALLOWED("", CATEGORY_SERVICE_DOWN, 11, null),

    STANZA_PAYMENT_REQUIRED("", CATEGORY_SERVICE_DOWN, 12, null),

    STANZA_POLICY_VIOLATION("", CATEGORY_SERVICE_DOWN, 13, null),

    /** The recipient you tried to reach is currently unavailable: %1$s */
    STANZA_RECIPIENT_UNAVAILABLE("The recipient you tried to reach is currently unavailable: %1$s", CATEGORY_TRY_AGAIN, 14, null),

    STANZA_REDIRECT("", CATEGORY_SERVICE_DOWN, 15, null),

    STANZA_REGISTRATION_REQUIRED("", CATEGORY_SERVICE_DOWN, 16, null),

    STANZA_REMOTE_SERVER_NOT_FOUND("", CATEGORY_SERVICE_DOWN, 17, null),

    STANZA_REMOTE_SERVER_TIMEOUT("", CATEGORY_SERVICE_DOWN, 18, null),

    STANZA_RESOURCE_CONSTRAINT("", CATEGORY_SERVICE_DOWN, 19, null),

    /** The following needed service is missing: "%1$s" */
    STANZA_SERVICE_UNAVAILABLE("The following needed service is missing: \"%1$s\"", CATEGORY_SERVICE_DOWN, 20, null),

    STANZA_SUBSCRIPTION_REQUIRED("", CATEGORY_SERVICE_DOWN, 21, null),

    STANZA_UNDEFINED_CONDITION("", CATEGORY_SERVICE_DOWN, 22, null),

    STANZA_UNEXPECTED_REQUEST("", CATEGORY_SERVICE_DOWN, 23, null),

    // Generic (start with code 1000)
    /** No appropriate channel found for recipient %1$s with payload namespace %2$s */
    NO_APPROPRIATE_CHANNEL("No appropriate channel found for recipient %1$s with payload namespace %2$s", CATEGORY_CONNECTIVITY, 1000,
        null, STANZA_INTERNAL_SERVER_ERROR, STANZA_INTERNAL_SERVER_ERROR),

    /** The following needed service is missing: "%1$s" */
    NEEDED_SERVICE_MISSING("The following needed service is missing: \"%1$s\"", CATEGORY_SERVICE_DOWN, 1001, null,
        STANZA_INTERNAL_SERVER_ERROR, STANZA_INTERNAL_SERVER_ERROR),

    /** Unexpected error: %1$s */
    UNEXPECTED_ERROR("Unexpected error: %1$s", CATEGORY_ERROR, 1002, null, STANZA_INTERNAL_SERVER_ERROR, STANZA_INTERNAL_SERVER_ERROR),

    /** Invalid ID: %1$s */
    INVALID_ID("Invalid ID: %1$s", CATEGORY_ERROR, 1003, null, STANZA_INTERNAL_SERVER_ERROR, STANZA_INTERNAL_SERVER_ERROR),

    /** Resource not available. */
    RESOURCE_NOT_AVAILABLE("Resource not available.", CATEGORY_ERROR, 1004, null, STANZA_INTERNAL_SERVER_ERROR,
        STANZA_INTERNAL_SERVER_ERROR),

    /** Your session is invalid */
    SESSION_INVALID("Your session is invalid.", CATEGORY_ERROR, 1005, null,
        STANZA_NOT_ACCEPTABLE, STANZA_NOT_ACCEPTABLE),

    /** Invalid sequence detected.*/
    SEQUENCE_INVALID("The sent sequence is invalid.", CATEGORY_ERROR, 1006, null, STANZA_BAD_REQUEST, STANZA_BAD_REQUEST),

    /** The server has no state associated with this client. Please enrol first. */
    STATE_MISSING("The server has no state associated with this client. Please enrol first.", CATEGORY_ERROR, 1007, null,
        STANZA_BAD_REQUEST, STANZA_BAD_REQUEST),

    /** You are no member of the GroupDispatcher: %1$s. Please join first. */
    NOT_A_MEMBER("You are no member of the GroupDispatcher: %1$s. Please join first.", CATEGORY_ERROR, 1008,
        null, STANZA_BAD_REQUEST, STANZA_BAD_REQUEST),

    /** The request didn't yield any response. */
    RESULT_MISSING("The request didn't yield any response.", CATEGORY_ERROR, 1009, null,
        null, null),

    /** The GroupDispatcher was already disposed. */
    GROUP_DISPOSED("The GroupDispatcher %1$s was already disposed.", CATEGORY_ERROR, 1010, null,
        null, null),

    /** The GroupDispatcher %1$s failed to process a Stanza from %2$s to %3$s. */
    STANZA_PROCESSING_FAILED("The GroupDispatcher %1$s failed to process a Stanza from %2$s to %3$s.", CATEGORY_ERROR, 1011, null,
        null, null),

   /** Joining the GroupDispatcher %1$s failed. */
   JOIN_FAILED("Joining the GroupDispatcher %1$s failed.", CATEGORY_ERROR, 1012, null,
            null, null),

   /** The GroupDispatcher failed to process the Stanza */
   LEAVE_FAILED("Leaving the GroupDispatcher %1$s failed.", CATEGORY_ERROR, 1013, null,
            null, null),

   /** ResponseChannel timed out while simulating synchronous behaviour. */
   RESPONSE_AWAIT_TIMEOUT("ResponseChannel timed out while simulating synchronous behaviour.", CATEGORY_ERROR, 1014, null,
       null, null),

    /** You are already a member of the GroupDispatcher %1$s. */
    ALREADY_MEMBER("You are already a member of the GroupDispatcher %1$s", CATEGORY_ERROR, 1015, null, null, null),

    /** GroupDispatcher denies to create another component handle for the given ID %1$s */
    COMPONENT_HANDLE_CREATION_DENIED("GroupDispatcher denies to create another component handle for the given ID %1$s", CATEGORY_ERROR, 1016, null, null, null),

     ;

    private int number;

    private Category category;

    private String message;

    private String displayMessage;

    private Transformer transformer;

    private RealtimeExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage) {
        this(message, category, detailNumber, displayMessage, null, null);
    }

    private RealtimeExceptionCodes(final String message, final Category category, final int detailNumber, String displayMessage, RealtimeExceptionCodes atmosphere, RealtimeExceptionCodes xmpp) {
        this.message = message;
        this.number = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.transformer = new Transformer(this, atmosphere == null ? this : atmosphere, xmpp == null ? this : xmpp);

    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return "RT_STANZA";
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(final OXException e) {
        return RealtimeExceptionFactory.getInstance().equals(this, e);
    }

    public Transformer getTransformer() {
        return transformer;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public RealtimeException create() {
        return RealtimeExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public RealtimeException create(final Object... args) {
        return RealtimeExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public RealtimeException create(final Throwable cause, final Object... args) {
        return RealtimeExceptionFactory.getInstance().create(this, cause, args);
    }

    /**
     * Create a RealtimeException based on the code number of the RealtimeExceptionCode. Useful to instantiate a POJO from other
     * representations. This POJO can then be tranformed to the appropriate channel representation.
     *
     * @param codeNumber The code number of the {@link RealtimeExceptionCodes}
     * @param cause the cause
     * @param args the log arguments
     * @return the initialized RealtimeException
     */
    public static RealtimeException create(final int codeNumber, final Throwable cause, final Object... args) {
        return fromCodeNumber(codeNumber).create(cause, args);
    }

    /**
     * Lookup RealtimeExceptionCode based in its number.
     *
     * @param wantedNumber the number of the exception code that we are looking for
     * @return the matching RealtimeExceptionCode
     * @throws IllegalStateException if no matching RealtimeExceptionCode can be found. All number must be matchable to
     *             RealtimeExceptionCodes.
     */
    private static RealtimeExceptionCodes fromCodeNumber(int wantedNumber) {
        for (RealtimeExceptionCodes realtimeExceptionCode : RealtimeExceptionCodes.values()) {
            int codeNumber = realtimeExceptionCode.getNumber();
            if (codeNumber == wantedNumber) {
                return realtimeExceptionCode;
            }
        }
        throw new IllegalStateException("Couldn't find matching RealtimeExceptionCode");
    }

}
