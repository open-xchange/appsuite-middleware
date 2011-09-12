package com.openexchange.mail;

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Mail exception codes.
 * <p>
 * The detail number range in subclasses generated in mail bundles is supposed to start with <code>2000</code> and may go up to
 * <code>2999</code>.
 * <p>
 * The detail number range in subclasses generated in transport bundles is supposed to start with <code>3000</code> and may go up to
 * <code>3999</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MailExceptionCode implements OXExceptionCode {
    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", Category.CATEGORY_ERROR, 0),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAMETER("Missing parameter %1$s", Category.CATEGORY_ERROR, 1),
    /**
     * Invalid CATEGORY_PERMISSION_DENIED values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s
     */
    INVALID_PERMISSION("Invalid CATEGORY_PERMISSION_DENIED values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s", Category.CATEGORY_ERROR, 2),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", Category.CATEGORY_ERROR, 3),
    /**
     * Missing parameter in user's mail config: %1$s
     */
    MISSING_CONNECT_PARAM("Missing parameter in user's mail config: %1$s", Category.CATEGORY_ERROR, 4),
    /**
     * Configuration error: %1$s
     */
    CONFIG_ERROR("Configuration error: %1$s", Category.CATEGORY_CONFIGURATION, 5),
    /**
     * Invalid multipart content. Number of enclosed contents is 0
     */
    INVALID_MULTIPART_CONTENT("Invalid multipart content. Number of enclosed contents is 0", Category.CATEGORY_ERROR, 6),
    /**
     * A part's content could not be read from message %1$s in mail folder %2$s
     */
    UNREADBALE_PART_CONTENT("A part's content could not be read from message %1$s in mail folder %2$s", Category.CATEGORY_ERROR, 7),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", Category.CATEGORY_ERROR, 8),
    /**
     * Invalid message path: %1$s
     */
    INVALID_MAIL_IDENTIFIER("Invalid message path: %1$s", Category.CATEGORY_ERROR, 9),
    /**
     * Unknown color label index: %1$s
     */
    UNKNOWN_COLOR_LABEL("Unknown color label: %1$s", Category.CATEGORY_ERROR, 10),
    /**
     * Cannot instantiate class %1$s.
     */
    INSTANTIATION_PROBLEM("Cannot instantiate class %1$s.", Category.CATEGORY_CONFIGURATION, 11),
    /**
     * Cannot initialize mail module
     */
    INITIALIZATION_PROBLEM("Cannot initialize mail module", Category.CATEGORY_CONFIGURATION, 12),
    /**
     * No mail module access permitted
     */
    NO_MAIL_ACCESS("No mail module access permitted", Category.CATEGORY_PERMISSION_DENIED, 13),
    /**
     * No mail account exists for admin user in context %1$s
     */
    ACCOUNT_DOES_NOT_EXIST("No mail account exists for admin user in context %1$s", Category.CATEGORY_CONFIGURATION, 14),
    /**
     * Process was interrupted. Please try again.
     */
    INTERRUPT_ERROR("Process was interrupted. Please try again.", Category.CATEGORY_TRY_AGAIN, 15),
    /**
     * Unsupported charset-encoding: %1$s
     */
    ENCODING_ERROR("Unsupported charset-encoding: %1$s", Category.CATEGORY_ERROR, 16),
    /**
     * Header %1$s could not be properly parsed
     */
    HEADER_PARSE_ERROR("Header %1$s could not be properly parsed", Category.CATEGORY_ERROR, 17),
    /**
     * Missing default %1$s folder in user mail settings
     */
    MISSING_DEFAULT_FOLDER_NAME("Missing default %1$s folder in user mail settings", Category.CATEGORY_ERROR, 18),
    /**
     * Spam handler initialization failed: %1$s
     */
    SPAM_HANDLER_INIT_FAILED("Spam handler initialization failed: %1$s", Category.CATEGORY_CONFIGURATION, 19),
    /**
     * Invalid Content-Type value: %1$s
     */
    INVALID_CONTENT_TYPE("Invalid Content-Type value: %1$s", Category.CATEGORY_ERROR, 20),
    /**
     * Messaging error: %1$s. TODO: Maybe change to: Broken/Bad message
     */
    MESSAGING_ERROR("Messaging error: %1$s", Category.CATEGORY_ERROR, 21),
    /**
     * Message field %1$s cannot be handled
     */
    INVALID_FIELD("Message field %1$s cannot be handled", Category.CATEGORY_ERROR, 22),
    /**
     * Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    INVALID_FIELD_EXT("Message field %1$s cannot be handled on server %2$s with login %3$s (user=%4$s, context=%5$s)", Category.CATEGORY_ERROR, 22),
    /**
     * Versit error: %1$s
     */
    VERSIT_ERROR("Versit error: %1$s", Category.CATEGORY_ERROR, 23),
    /**
     * No attachment was found with id %1$s in message
     */
    NO_ATTACHMENT_FOUND("No attachment was found with id %1$s in message", Category.CATEGORY_USER_INPUT, 24),
    /**
     * Versit attachment could not be saved due to an unsupported MIME type: %1$s
     */
    UNSUPPORTED_VERSIT_ATTACHMENT("Versit attachment could not be saved due to an unsupported MIME type: %1$s", Category.CATEGORY_USER_INPUT, 25),
    /**
     * Invalid parameter name: %1$s
     */
    INVALID_PARAMETER("Invalid parameter name: %1$s", Category.CATEGORY_ERROR, 26),
    /**
     * Could not create a PartModifier instance from name %1$s
     */
    PART_MODIFIER_CREATION_FAILED("Could not create a PartModifier instance from name %1$s", Category.CATEGORY_ERROR, 27),
    /**
     * Upload quota (%1$s) exceeded for file %2$s (size=%3$s)
     */
    UPLOAD_QUOTA_EXCEEDED_FOR_FILE("Upload quota (%1$s) exceeded for file %2$s (size=%3$s)", Category.CATEGORY_USER_INPUT, 28),
    /**
     * Upload quota (%1$s) exceeded
     */
    UPLOAD_QUOTA_EXCEEDED("Upload quota (%1$s) exceeded", Category.CATEGORY_USER_INPUT, 29),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAM(MISSING_PARAMETER),
    /**
     * Invalid integer value %1$s
     */
    INVALID_INT_VALUE("Invalid integer value %1$s", Category.CATEGORY_ERROR, 31),
    /**
     * Mail(s) %1$s could not be found in folder %2$s
     */
    MAIL_NOT_FOUND("Mail(s) %1$s could not be found in folder %2$s", Category.CATEGORY_ERROR, 32),
    /**
     * Action %1$s is not supported by %2$s
     */
    UNSUPPORTED_ACTION("Action %1$s is not supported by %2$s", Category.CATEGORY_ERROR, 33),
    /**
     * Message could not be sent
     */
    SEND_FAILED_UNKNOWN("Message could not be sent", Category.CATEGORY_ERROR, 35),
    /**
     * Unknown action: %1$s
     */
    UNKNOWN_ACTION("Unknown or unsupported action: %1$s", Category.CATEGORY_ERROR, 36),
    /**
     * Missing field %1$s
     */
    MISSING_FIELD("Missing field %1$s", Category.CATEGORY_ERROR, 37),
    /**
     * Unsupported MIME type %1$s
     */
    UNSUPPORTED_MIME_TYPE("Unsupported MIME type %1$s", Category.CATEGORY_ERROR, 38),
    /**
     * Mail could not be moved to trash folder. Quota exceeded
     */
    DELETE_FAILED_OVER_QUOTA("Mail could not be moved to trash folder. Quota exceeded", Category.CATEGORY_CAPACITY, 39),
    /**
     * The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s
     */
    PART_NOT_FOUND("The message part with sequence ID %1$s could not be found in message %2$s in folder %3$s", Category.CATEGORY_ERROR, 40),
    /**
     * No content available in mail part
     */
    NO_CONTENT("No content available in mail part", Category.CATEGORY_ERROR, 41),
    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder due to exceeded quota.
     */
    COPY_TO_SENT_FOLDER_FAILED_QUOTA("Message has been successfully sent, but a copy could not be placed in your sent folder due to exceeded quota.", Category.CATEGORY_CAPACITY, 42),
    /**
     * Message has been successfully sent, but a copy could not be placed in your sent folder
     */
    COPY_TO_SENT_FOLDER_FAILED("Message has been successfully sent, but a copy could not be placed in your sent folder.", Category.CATEGORY_CAPACITY, 43),
    /**
     * No provider could be found for protocol/URL "%1$s"
     */
    UNKNOWN_PROTOCOL("No provider could be found for protocol/URL \"%1$s\"", Category.CATEGORY_CONFIGURATION, 44),
    /**
     * Protocol cannot be parsed: %1$s
     */
    PROTOCOL_PARSE_ERROR("Protocol cannot be parsed: %1$s", Category.CATEGORY_ERROR, 45),
    /**
     * Bad value %1$s in parameter %2$s
     */
    BAD_PARAM_VALUE("Bad value %1$s in parameter %2$s", Category.CATEGORY_USER_INPUT, 46),
    /**
     * No reply on multiple message possible
     */
    NO_MULTIPLE_REPLY("No reply on multiple message possible", Category.CATEGORY_USER_INPUT, 47),
    /**
     * legal system flag argument %1$s. Flag must be to the power of 2
     */
    ILLEGAL_FLAG_ARGUMENT("Illegal system flag argument %1$s. Flag must be to the power of 2", Category.CATEGORY_ERROR, 48),
    /**
     * Attachment %1$s not found inside mail %2$s of mail folder %3$s
     */
    ATTACHMENT_NOT_FOUND("Attachment %1$s not found inside mail %2$s of mail folder %3$s", Category.CATEGORY_ERROR, 49),
    /**
     * Folder %1$s does not hold messages and is therefore not selectable
     */
    FOLDER_DOES_NOT_HOLD_MESSAGES("Folder %1$s does not hold messages and is therefore not selectable", Category.CATEGORY_PERMISSION_DENIED, 50),
    /**
     * Folder %1$s does not hold messages and is therefore not selectable on server %2$s with login %3$s (user=%4$s, context=%5$s)
     */
    FOLDER_DOES_NOT_HOLD_MESSAGES_EXT("Folder %1$s does not hold messages and is therefore not selectable on server %2$s with login %3$s (user=%4$s, context=%5$s)", Category.CATEGORY_PERMISSION_DENIED, 50),
    /**
     * Insufficient folder attributes: Either existence status or fullname have to be present to determine if a mail folder create or
     * update shall be performed
     */
    INSUFFICIENT_FOLDER_ATTR("Insufficient folder attributes: Either existence status " + "or fullname have to be present to determine if a " + "mail folder create or update shall be performed", Category.CATEGORY_ERROR, 51),
    /**
     * Root folder must not be modified or deleted
     */
    NO_ROOT_FOLDER_MODIFY_DELETE("Root folder must not be modified or deleted", Category.CATEGORY_ERROR, 52),
    /**
     * No transport provider could be found for protocol/URL "%1$s"
     */
    UNKNOWN_TRANSPORT_PROTOCOL("No transport provider could be found for protocol/URL \"%1$s\"", Category.CATEGORY_CONFIGURATION, 53),
    /**
     * Missing mail folder fullname
     */
    MISSING_FULLNAME("Missing mail folder fullname", Category.CATEGORY_ERROR, 54),
    /**
     * Image attachment with Content-Id "%1$s" not found inside mail %2$s of mail folder %3$s
     */
    IMAGE_ATTACHMENT_NOT_FOUND("Image attachment with Content-Id \"%1$s\" not found inside mail %2$s of mail folder %3$s", Category.CATEGORY_ERROR, 55),
    /**
     * The specified email address %1$s is not covered by allowed email address aliases
     */
    INVALID_SENDER("The specified email address %1$s is not covered by allowed email address aliases", Category.CATEGORY_USER_INPUT, 56),
    /**
     * Checking default folders on server %1$s for user %2$s (%3$s) in context on %4$s failed: %5$s
     */
    DEFAULT_FOLDER_CHECK_FAILED("Checking default folders on server %1$s for user %2$s (%3$s) in context on %4$s failed: %5$s", Category.CATEGORY_ERROR, 57),
    /**
     * The types of specified data source are not supported
     */
    UNSUPPORTED_DATASOURCE("The types of specified data source are not supported", Category.CATEGORY_ERROR, 58),
    /**
     * Mail cannot be parsed. Invalid or incomplete mail data.
     */
    UNPARSEABLE_MESSAGE("Mail cannot be parsed. Invalid or incomplete mail data.", Category.CATEGORY_ERROR, 59),
    /**
     * Mail folder cannot be created/renamed. Empty folder name.
     */
    INVALID_FOLDER_NAME_EMPTY("Mail folder cannot be created/renamed. Empty folder name.", Category.CATEGORY_USER_INPUT, 60),
    /**
     * Mail folder cannot be created/rename. Name must not contain character '%1$s'
     */
    INVALID_FOLDER_NAME("Mail folder cannot be created/renamed. Name must not contain character '%1$s'", Category.CATEGORY_USER_INPUT, 61),
    /**
     * Invalid Content-Disposition value: %1$s
     */
    INVALID_CONTENT_DISPOSITION("Invalid Content-Disposition value: %1$s", Category.CATEGORY_ERROR, 62),
    /**
     * A folder named %1$s already exists.
     */
    DUPLICATE_FOLDER("A folder named %1$s already exists.", Category.CATEGORY_PERMISSION_DENIED, 63),
    /**
     * A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    DUPLICATE_FOLDER_EXT("A folder named %1$s already exists on server %2$s with login %3$s (user=%4$s, context=%5$s).", Category.CATEGORY_PERMISSION_DENIED, 63),
    /**
     * No create access on mail folder %1$s.
     */
    NO_CREATE_ACCESS("No create access on mail folder %1$s.", Category.CATEGORY_PERMISSION_DENIED, 64),
    /**
     * No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).
     */
    NO_CREATE_ACCESS_EXT("No create access on mail folder %1$s on server %2$s with login %3$s (user=%4$s, context=%5$s).", Category.CATEGORY_PERMISSION_DENIED, 64),
    /**
     * Mail account %1$s with ID %2$s does not support mail transport.
     */
    NO_TRANSPORT_SUPPORT("Mail account %1$s with ID %2$s does not support mail transport.", Category.CATEGORY_ERROR, 65),
    /**
     * Mail folder could not be found: %1$s.
     */
    FOLDER_NOT_FOUND("Mail folder could not be found: %1$s.", Category.CATEGORY_ERROR, 66),
    /**
     * Referenced mail %1$s could not be found in folder %2$s. Therefore reply/forward operation cannot be performed.
     */
    REFERENCED_MAIL_NOT_FOUND("Referenced mail %1$s could not be found in folder %2$s. Therefore reply/forward operation cannot be performed.", Category.CATEGORY_ERROR, 67),
    /**
     * In order to accomplish the search, %1$d or more characters are required.
     */
    PATTERN_TOO_SHORT("In order to accomplish the search, %1$d or more characters are required.", Category.CATEGORY_USER_INPUT, 68),
    /**
     * Mail folder must not be deleted: %1$s.
     */
    FOLDER_DELETION_DENIED("Mail folder must not be deleted: %1$s.", Category.CATEGORY_ERROR, 69),
    /**
     * No delete access on mail folder: %1$s.
     */
    NO_DELETE_ACCESS("No delete access on mail folder: %1$s.", Category.CATEGORY_PERMISSION_DENIED, 70),
    /**
     * Mail folder must not be moved: %1$s.
     */
    FOLDER_MOVE_DENIED("Mail folder must not be moved: %1$s.", Category.CATEGORY_ERROR, 71),
    /**
     * Mail folder must not be updated: %1$s.
     */
    FOLDER_UPDATE_DENIED("Mail folder must not be updated: %1$s.", Category.CATEGORY_ERROR, 72),
    /**
     * No write access on mail folder: %1$s.
     */
    NO_WRITE_ACCESS("No write access on mail folder: %1$s.", Category.CATEGORY_PERMISSION_DENIED, 73),
    /**
     * No connection available to access mailbox
     */
    NOT_CONNECTED("No connection available to access mailbox", Category.CATEGORY_ERROR, 74),
    /**
     * Mail could not be found in folder %1$s for Message-Id: %2$s
     */
    MAIL_NOT_FOUN_BY_MESSAGE_ID("Mail could not be found in folder %1$s for Message-Id: %2$s", Category.CATEGORY_ERROR, 32),

    /**
     * Sent quota exceeded, you are only allowed to sent 1 mail in %1$s seconds.
     */
    SENT_QUOTA_EXCEEDED("Sent quota exceeded, you are only allowed to send 1 mail in %1$s seconds.", Category.CATEGORY_ERROR, 75),

    /**
     * Sent quota exceeded, you are only allowed to sent 1 mail in %1$s seconds.
     */
    RECIPIENTS_EXCEEDED("Please limit your recipients to %1$s  (including To/Cc/Bcc), and click 'Send' again.", Category.CATEGORY_ERROR, 76),
    /**
     * Unable to parse mail server URI "%1$s".
     */
    URI_PARSE_FAILED("Unable to parse mail server URI \"%1$s\".", Category.CATEGORY_USER_INPUT, 77),
    /**
     * Mail attachment expired or absent.
     */
    ATTACHMENT_EXPIRED("Mail attachment expired or absent.", Category.CATEGORY_USER_INPUT, 78),

    ;

    private final String message;

    private final int number;

    private final Category category;

    private final boolean display;

    private MailExceptionCode(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.number = detailNumber;
        this.category = category;
        display = category.getLogLevel().implies(LogLevel.DEBUG);
    }

    private MailExceptionCode(final MailExceptionCode source) {
        message = source.message;
        number = source.number;
        category = source.category;
        display = category.getLogLevel().implies(LogLevel.DEBUG);
    }

    @Override
    public String getPrefix() {
        return "MSG";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}