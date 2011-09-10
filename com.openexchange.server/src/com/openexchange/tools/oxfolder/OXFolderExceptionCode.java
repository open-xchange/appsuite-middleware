package com.openexchange.tools.oxfolder;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The error code enumeration for folders.
 */
public enum OXFolderExceptionCode implements OXExceptionCode {
    /**
     * User %1$s has no access to module %2$s in context %3$s due to user configuration
     * <p>
     * Requested operation was canceled because underlying user configuration denies folder access due to module restrictions
     * </p>
     */
    NO_MODULE_ACCESS("User %1$s has no access to module %2$s in context %3$s due to user configuration", Category.CATEGORY_PERMISSION_DENIED, 2, Generic.NO_PERMISSION),
    /**
     * Folder \"%1$s\" not visible to user %2$s in context %3$s
     * <p>
     * Either underlying user configuration or folder CATEGORY_PERMISSION_DENIED setting denies visibility of folder in question
     * </p>
     */
    NOT_VISIBLE("Folder \"%1$s\" not visible to user %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 3, Generic.NO_PERMISSION),
    /**
     * User %1$s has no access on shared folder %2$s in context %3$s due to user configuration
     * <p>
     * Underlying user configuration denies access to shared folder(s)
     * </p>
     */
    NO_SHARED_FOLDER_ACCESS("User %1$s has no access on shared folder %2$s in context %3$s due to user configuration", Category.CATEGORY_PERMISSION_DENIED, 4, Generic.NO_PERMISSION),
    /**
     * User %1$s has no write access on public folder %2$s in context %3$s due to user configuration
     * <p>
     * Underlying user configuration denies write access to public folder(s)
     * </p>
     */
    NO_PUBLIC_FOLDER_WRITE_ACCESS("User %1$s has no write access on public folder %2$s in context %3$s due to user configuration", Category.CATEGORY_PERMISSION_DENIED, 5, Generic.NO_PERMISSION),
    /**
     * User %1$s has no create-subfolder permission on folder %2$s in context %3$s
     * <p>
     * Folder CATEGORY_PERMISSION_DENIED setting denies subfolder creation beneath folder in question
     * </p>
     */
    NO_CREATE_SUBFOLDER_PERMISSION("User %1$s has no create-subfolder permission on folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 6, Generic.NO_PERMISSION),
    /**
     * Missing field %1$s in folder %2$s in context %3$s
     * <p>
     * Operation was canceled due to a missing folder attribute
     * </p>
     */
    MISSING_FOLDER_ATTRIBUTE("Missing field %1$s in folder %2$s in context %3$s", Category.CATEGORY_ERROR, 7),
    /**
     * Folder %1$s does not exist in context %2$s
     * <p>
     * Folder does not exist
     * </p>
     */
    NOT_EXISTS("Folder %1$s does not exist in context %2$s", Category.CATEGORY_ERROR, 8, Generic.NOT_FOUND),
    /**
     * Folder %1$s has been modified after last sync timestamp in context %2$s
     * <p>
     * Client timestamp is before folder's last-changed timestamp
     * </p>
     */
    CONCURRENT_MODIFICATION("Folder %1$s has been modified after last sync timestamp in context %2$s", Category.CATEGORY_CONFLICT, 9, Generic.CONFLICT),
    /**
     * User %1$s has no admin access to folder %2$s in context %3$s
     * <p>
     * No necessary admin access granted for update operation
     * </p>
     */
    NO_ADMIN_ACCESS("User %1$s has no admin access to folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 10, Generic.NO_PERMISSION),
    /**
     * Default folder %1$s cannot be renamed in context %2$s
     * <p>
     * Default folder must not be renamed
     * </p>
     */
    NO_DEFAULT_FOLDER_RENAME("Default folder %1$s cannot be renamed in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 11, Generic.NO_PERMISSION),
    /**
     * A duplicate folder exists in parent folder %1$s in context %2$s
     * <p>
     * Rename aborted cause a duplicate folder exists beneath parent folder
     * </p>
     */
    NO_DUPLICATE_FOLDER("A duplicate folder exists in parent folder %1$s in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 12),
    /**
     * Parent folder %1$s does not allow folder's type setting %2$s in context %3$s
     * <p>
     * Folder's type setting is invalid
     * </p>
     */
    INVALID_TYPE("Parent folder %1$s does not allow folder's type setting %2$s in context %3$s", Category.CATEGORY_ERROR, 13),
    /**
     * Parent folder %1$s does not allow folder's module setting (%2$s) in context %3$s
     * <p>
     * Folder's module setting is invalid
     * </p>
     */
    INVALID_MODULE("Parent folder %1$s does not allow folder's module setting (%2$s) in context %3$s", Category.CATEGORY_ERROR, 14),
    /**
     * Only one admin CATEGORY_PERMISSION_DENIED is allowed on a private folder
     * <p>
     * Only one admin is allowed on a private folder
     * </p>
     */
    ONLY_ONE_PRIVATE_FOLDER_ADMIN("Only one admin CATEGORY_PERMISSION_DENIED is allowed on a private folder", Category.CATEGORY_ERROR, 15),
    /**
     * A group must not hold admin CATEGORY_PERMISSION_DENIED on a private folder
     * <p>
     * No admin group CATEGORY_PERMISSION_DENIED is allowed on a private folder
     * </p>
     */
    NO_PRIVATE_FOLDER_ADMIN_GROUP("A group must not hold admin CATEGORY_PERMISSION_DENIED on a private folder", Category.CATEGORY_ERROR, 16),
    /**
     * Only folder owner may hold admin CATEGORY_PERMISSION_DENIED on a private folder
     * <p>
     * Only folder owner may hold admin CATEGORY_PERMISSION_DENIED on a private folder
     * </p>
     */
    ONLY_PRIVATE_FOLDER_OWNER_ADMIN("Only folder owner may hold admin CATEGORY_PERMISSION_DENIED on a private folder", Category.CATEGORY_ERROR, 17),
    /**
     * Administration rights required. In the Rights tab add at least one user with administration rights.
     * <p>
     * No entity has been defined as admin
     * </p>
     */
    NO_FOLDER_ADMIN("Administration rights required. In the Rights tab add at least one user with administration rights.", Category.CATEGORY_ERROR, 18),
    /**
     * Invalid object ID %1$s
     * <p>
     * An invalid object ID
     * </p>
     */
    INVALID_OBJECT_ID("Invalid object ID %1$s", Category.CATEGORY_ERROR, 19),
    /**
     * Not allowed to change parent id of folder %1$s through an update call. Use move method instead
     * <p>
     * Folder's parent id is changed during an update operation
     * </p>
     */
    NO_MOVE_THROUGH_UPDATE("Not allowed to change parent id of folder %1$s through an update call. Use move method instead", Category.CATEGORY_ERROR, 20),
    /**
     * Not allowed to move default folder %1$s in context %2$s
     * <p>
     * Default folder must not be moved
     * </p>
     */
    NO_DEFAULT_FOLDER_MOVE("Not allowed to move default folder %1$s in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 21),
    /**
     * Target folder %1$s contains a duplicate folder in context %2$s
     * <p>
     * Target folder contains a duplicate folder
     * </p>
     */
    TARGET_FOLDER_CONTAINS_DUPLICATE("Target folder %1$s contains a duplicate folder in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 22),
    /**
     * Shared folder %1$s cannot be moved in context %2$s
     * <p>
     * A shared folder must not be moved
     * </p>
     */
    NO_SHARED_FOLDER_MOVE("Shared folder %1$s cannot be moved in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 23),
    /**
     * Shared folder %1$s cannot be target of move operation in context %2$s
     * <p>
     * A shared folder must not be target of a move operation
     * </p>
     */
    NO_SHARED_FOLDER_TARGET("Shared folder %1$s cannot be target of move operation in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 24),
    /**
     * System folder %1$s cannot be moved in context %2$s
     * <p>
     * A system folder must not be moved
     * </p>
     */
    NO_SYSTEM_FOLDER_MOVE("System folder %1$s cannot be moved in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 25),
    /**
     * Private folder %1$s can only be moved to a private folder in context %2$s
     * <p>
     * A private folder may only be moved to a private folder
     * </p>
     */
    ONLY_PRIVATE_TO_PRIVATE_MOVE("Private folder %1$s can only be moved to a private folder in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 26),
    /**
     * Public folder %1$s may only be moved to a public folder in context %2$s
     * <p>
     * A public folder may only be moved to a public folder
     * </p>
     */
    ONLY_PUBLIC_TO_PUBLIC_MOVE("Public folder %1$s can only be moved to a public folder in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 27),
    /**
     * Target and source folder must not be equal in context %1$s
     * <p>
     * Target and source folder must not be equal
     * </p>
     */
    NO_EQUAL_MOVE("Target and source folder cannot be equal in context %1$s", Category.CATEGORY_PERMISSION_DENIED, 28),
    /**
     * User %1$s is not allowed to move all subfolders of folder %2$s in context %3$s
     * <p>
     * User is not allowed to move source folder's subfolder
     * </p>
     */
    NO_SUBFOLDER_MOVE_ACCESS("User %1$s is not allowed to move all subfolders of folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 29),
    /**
     * User %1$s is not allowed to delete shared folder %2$s in context %3$s
     * <p>
     * A shared folder must not be deleted
     * </p>
     */
    NO_SHARED_FOLDER_DELETION("User %1$s is not allowed to delete shared folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 30),
    /**
     * User %1$s is not allowed to delete default folder %2$s in context %3$s
     * <p>
     * Default folder(s) must not be deleted
     * </p>
     */
    NO_DEFAULT_FOLDER_DELETION("User %1$s is not allowed to delete default folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 31),
    /**
     * User %1$s is not allowed to delete all contained objects in folder %2$s in context %3$s
     * <p>
     * User is not allowed to delete all objects contained in folder in question
     * </p>
     */
    NOT_ALL_OBJECTS_DELETION("User %1$s is not allowed to delete all contained objects in folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 32),
    /**
     * No admin user found in context %1$s
     * <p>
     * No admin user was found in context in question
     * </p>
     */
    NO_ADMIN_USER_FOUND_IN_CONTEXT("No admin user found in context %1$s", Category.CATEGORY_ERROR, 33),
    /**
     * No default folder could be found in module %1$s for user %2$s in context %3$s
     * <p>
     * No default folder was found for current user in given module and context
     * </p>
     */
    NO_DEFAULT_FOLDER_FOUND("No default folder could be found in module %1$s for user %2$s in context %3$s", Category.CATEGORY_ERROR, 34),
    /**
     * Folder %1$s could not be loaded in context %2$s
     * <p>
     * Folder could not be loaded from storage
     * </p>
     */
    FOLDER_COULD_NOT_BE_LOADED("Folder %1$s could not be loaded in context %2$s", Category.CATEGORY_ERROR, 35),
    /**
     * Folder %1$s could not be put into cache in context %2$s
     * <p>
     * Folder could not be put into cache
     * </p>
     */
    FOLDER_COULD_NOT_BE_PUT_INTO_CACHE("Folder %1$s could not be put into cache in context %2$s", Category.CATEGORY_ERROR, 36),
    /**
     * Effective CATEGORY_PERMISSION_DENIED of folder %1$s could not be determined for user %2$s in context %3$s
     * <p>
     * User's effective CATEGORY_PERMISSION_DENIED on folder could not be determined
     * </p>
     */
    NO_EFFECTIVE_PERMISSION("Effective CATEGORY_PERMISSION_DENIED of folder %1$s could not be determined for user %2$s in context %3$s", Category.CATEGORY_ERROR, 37),
    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", Category.CATEGORY_ERROR, 38),
    /**
     * A DBPool error occurred in context %1$s
     */
    DBPOOLING_ERROR("A DBPool error occurred in context %1$s", Category.CATEGORY_ERROR, 39),
    /**
     * Delivered sequence id %1$s from database is less than allowed min. folder id of %2$s in context %3$s
     */
    INVALID_SEQUENCE_ID("Delivered sequence id %1$s from database is less than allowed min. folder id of %2$s in context %3$s", Category.CATEGORY_ERROR, 41),
    /**
     * Module %1$s is unknown in context %2$s
     */
    UNKNOWN_MODULE("Module %1$s is unknown in context %2$s", Category.CATEGORY_ERROR, 42),
    /**
     * Folder %1$s could not be updated in context %2$s
     * <p>
     * Folder update failed for any reason
     * </p>
     */
    UPDATE_FAILED("Folder %1$s could not be updated in context %2$s", Category.CATEGORY_ERROR, 43),
    /**
     * Invalid entity id %1$s detected in permissions of folder %2$s in context %3$s
     */
    INVALID_ENTITY("Invalid entity id %1$s detected in permissions of folder %2$s in context %3$s", Category.CATEGORY_ERROR, 44),
    /**
     * Folder %1$s must not be moved to one of its subfolders in context %2$s
     */
    NO_SUBFOLDER_MOVE("Folder %1$s must not be moved to one of its subfolders in context %2$s", Category.CATEGORY_ERROR, 45),
    /**
     * Inserted for those exception that will turn to an <code>OXException</code> in future
     */
    UNKNOWN_EXCEPTION("UNKNOWN EXCEPTION", Category.CATEGORY_ERROR, 46),
    /**
     * A LDAP error occurred in context %1$s
     */
    LDAP_ERROR("A LDAP error occurred in context %1$s", Category.CATEGORY_ERROR, 47),
    /**
     * Attribute \"%1$s\" is not set in FolderObject instance %2$s in context %3$s
     */
    ATTRIBUTE_NOT_SET("Attribute \"%1$s\" is not set in FolderObject instance %2$s in context %3$s", Category.CATEGORY_ERROR, 48),
    /**
     * A source folder of module %1$s must not be moved to a target folder of module %2$s
     */
    INCOMPATIBLE_MODULES("A source folder of module %1$s must not be moved to a target folder of module %2$s", Category.CATEGORY_PERMISSION_DENIED, 49),
    /**
     * Operation not executable on folder %1$s in context %2$s
     */
    UNSUPPORTED_OPERATION("Operation not executable on folder %1$s in context %2$s", Category.CATEGORY_ERROR, 50),
    /**
     * Folder cache (region name = %1$s) could not be initialized due to following reason: %2$s
     */
    FOLDER_CACHE_INITIALIZATION_FAILED("Folder cache (region name = %1$s) could not be initialized due to following reason: %2$s", Category.CATEGORY_ERROR, 51),
    /**
     * Folder cache has not been enabled in config file %1$s
     */
    CACHE_NOT_ENABLED("Folder cache has not been enabled in config file %1$s", Category.CATEGORY_ERROR, 52),
    /**
     * Folder %1$s could not be removed from folder cache
     */
    CACHE_ERROR_ON_REMOVE("Folder %1$s could not be removed from folder cache", Category.CATEGORY_ERROR, 53),
    /**
     * User %1$s has no write permission on folder %2$s in context %3$s
     */
    NO_WRITE_PERMISSION("User %1$s has no write permission on folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 54, Generic.NO_PERMISSION),
    /**
     * A JSON error occurred: %1$s
     */
    JSON_ERROR("A JSON error occurred: %1$s", Category.CATEGORY_ERROR, 55),
    /**
     * Unknown parameter container type: %1$s
     */
    UNKNOWN_PARAMETER_CONTAINER_TYPE("Unknown parameter container type: %1$s", Category.CATEGORY_ERROR, 56),
    /**
     * Missing parameter %1$s
     */
    MISSING_PARAMETER("Missing parameter %1$s", Category.CATEGORY_ERROR, 57),
    /**
     * Bad value %1$s in parameter %2$s
     */
    BAD_PARAM_VALUE("Bad value %1$s in parameter %2$s", Category.CATEGORY_USER_INPUT, 58),
    /**
     * Unknown field: %1$s
     */
    UNKNOWN_FIELD("Unknown field: %1$s", Category.CATEGORY_ERROR, 59),
    /**
     * Parameter %1$s does not match JSON key %2$s
     */
    PARAMETER_MISMATCH("Parameter %1$s does not match JSON key %2$s", Category.CATEGORY_ERROR, 60),
    /**
     * Invalid CATEGORY_PERMISSION_DENIED values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s
     */
    INVALID_PERMISSION("Invalid CATEGORY_PERMISSION_DENIED values: fp=%1$s orp=%2$s owp=%3$s odp=%4$s", Category.CATEGORY_ERROR, 61),
    /**
     * Unknown action: %1$s
     */
    UNKNOWN_ACTION("Unknown action: %1$s", Category.CATEGORY_ERROR, 62),
    /**
     * Shared folder %1$s MUST NOT be updated in context %2$s
     */
    NO_SHARED_FOLDER_UPDATE("Shared folder %1$s MUST NOT be updated in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 63),
    /**
     * The attribute %1$s contains too much characters. Current length %3$s is more than allowed length of %2$s characters.
     */
    TRUNCATED("The attribute %1$s contains too much characters. Current " + "length %3$d is more than allowed length of %2$d characters.", Category.CATEGORY_TRUNCATED, 64),
    /**
     * Unable to map OCL permission value %1$s to a JSON permission value
     */
    MAP_PERMISSION_FAILED("Unable to map OCL permission value %1$s to a JSON permission value", Category.CATEGORY_ERROR, 65),
    /**
     * Folder existence cannot be checked due to insufficient folder information
     */
    UNSUFFICIENT_FOLDER_INFORMATION("Folder existence cannot be checked due to insufficient folder information)", Category.CATEGORY_ERROR, 66),
    /**
     * A runtime error occurred in context %1$s
     */
    RUNTIME_ERROR("A runtime error occurred in context %1$s", Category.CATEGORY_ERROR, 67),
    /**
     * This method is not applicable to an IMAP permission.
     */
    ACL_PERMISSION_ERROR("This method is not applicable to an IMAP permission.", Category.CATEGORY_ERROR, 68),
    /**
     * No subfolder creation underneath shared folder %1$s in context %2$s
     */
    NO_SUBFOLDER_BELOW_SHARED_FOLDER("No subfolder creation underneath shared folder %1$s in context %2$s", Category.CATEGORY_PERMISSION_DENIED, 69),
    /**
     * User %1$s grants invalid permissions on shared folder %2$s in context %3$s. Only owner of parental shared folder %4$s may be
     * folder admin
     */
    INVALID_SHARED_FOLDER_SUBFOLDER_PERMISSION("User %1$s grants invalid permissions on shared folder %2$s in context %3$s." + " Only owner of parental shared folder %4$s may be folder admin", Category.CATEGORY_PERMISSION_DENIED, 70),
    /**
     * Owner %1$s of default folder %2$s must keep the folder admin permission
     */
    CREATOR_IS_NOT_ADMIN("Owner %1$s of default folder %2$s must keep the folder admin permission", Category.CATEGORY_USER_INPUT, 71),
    /**
     * User %1$s is not allowed to share folder %2$s in context %3$s due to user configuration
     */
    SHARE_FORBIDDEN("User %1$s is not allowed to share folder %2$s in context %3$s due to user configuration", Category.CATEGORY_CONFIGURATION, 72),
    /**
     * The permissions for the user %1$s are limited. It is not possible to grant this user the wanted permissions for the folder %2$s
     * in context %3$s.
     */
    UNAPPLICABLE_FOLDER_PERM("The permissions for the user %1$s are limited. It is not possible to grant this user the wanted permissions for the folder %2$s in context %3$s.", Category.CATEGORY_CONFIGURATION, 73),
    /**
     * Folder %1$s in context %2$s contains a hidden subfolder. User %3$s has no delete rights for this subfolder and consequently
     * cannot delete its parent folder.
     */
    HIDDEN_FOLDER_ON_DELETION("Folder %1$s in context %2$s contains a hidden subfolder." + " User %3$s has no delete rights for this subfolder and consequently cannot delete its parent folder.", Category.CATEGORY_PERMISSION_DENIED, 74),
    /**
     * An infostore folder named %1$s already exists below folder %2$s (%3$s) in context %4$s. Please choose another display name.
     */
    NO_DEFAULT_INFOSTORE_CREATE("An infostore folder named %1$s already exists below folder %2$s (%3$s) in context %4$s." + " Please choose another display name.", Category.CATEGORY_PERMISSION_DENIED, 75),
    /**
     * Folder contains invalid data: "%1$s"
     */
    INVALID_DATA("Folder contains invalid data: \"%1$s\"", Category.CATEGORY_USER_INPUT, 76),
    /**
     * You already share a personal folder named "%1$s" with the same user. You can not share two folders with exactly the same name
     * with a user. Please rename the folder before sharing it with this user.
     */
    SIMILAR_NAMED_SHARED_FOLDER("You already share a personal folder named \"%1$s\" with the same user. You can not share two folders with exactly the same name with a user. Please rename the folder before sharing it with this user.", Category.CATEGORY_USER_INPUT, 77),
    /**
     * Folder module cannot be updated since folder is not empty
     */
    NO_FOLDER_MODULE_UPDATE("Folder module cannot be updated since folder is not empty", Category.CATEGORY_USER_INPUT, 78),
    /**
     * Duplicate permission defined for user %1$s. Only one permission per user is allowed.
     */
    DUPLICATE_USER_PERMISSION("Duplicate permission defined for user %1$s. Only one permission per user is allowed.", Category.CATEGORY_USER_INPUT, 79),
    /**
     * Duplicate permission defined for group %1$s. Only one permission per group is allowed.
     */
    DUPLICATE_GROUP_PERMISSION("Duplicate permission defined for group %1$s. Only one permission per group is allowed.", Category.CATEGORY_USER_INPUT, 80),
    /**
     * Only the folder visibility permission is allowed to be changed for folder %1$s in context %2$s.
     */
    FOLDER_VISIBILITY_PERMISSION_ONLY("Only the folder visibility permission is allowed to be changed for folder %1$s in context %2$s.", Category.CATEGORY_PERMISSION_DENIED, 81),
    /**
     * Only individual user permissions, but no group permissions are allowed for folder %1$s in context %2$s.
     */
    NO_GROUP_PERMISSION("Only individual user permissions, but no group permissions are allowed for folder %1$s in context %2$s.", Category.CATEGORY_PERMISSION_DENIED, 82),
    /**
     * No individual user permissions are allowed for folder %1$s in context %2$s.
     */
    NO_INDIVIDUAL_PERMISSION("No individual user permissions are allowed for folder %1$s in context %2$s.", Category.CATEGORY_PERMISSION_DENIED, 83),
    /**
     * Folder module must not be updated.
     */
    DENY_FOLDER_MODULE_UPDATE("Folder module must not be updated.", Category.CATEGORY_USER_INPUT, 84),
    /**
     * The module of a default folder must not be changed.
     */
    NO_DEFAULT_FOLDER_MODULE_UPDATE("The module of a default folder must not be changed.", Category.CATEGORY_USER_INPUT, 85),
    /**
     * User %1$s must not rename folder %2$s in context %3$s
     * <p>
     * No necessary rename access granted for update operation
     * </p>
     */
    NO_RENAME_ACCESS("User %1$s must not rename folder %2$s in context %3$s", Category.CATEGORY_PERMISSION_DENIED, 86),
    /**
     * Failed to create folder. Please retry.
     */
    CREATE_FAILED("Failed to create folder. Please retry.", Category.CATEGORY_TRY_AGAIN, 87),
    /**
     * Delete cannot be performed because of locked documents in folder %1$s in context %2$s.
     */
    DELETE_FAILED_LOCKED_DOCUMENTS("Delete cannot be performed because of locked documents in folder %1$s in context %2$s.", Category.CATEGORY_PERMISSION_DENIED, 88),
    /**
     * Operation may only be performed for context administrator.
     */
    ADMIN_OP_ONLY("Operation may only be performed for context administrator.", Category.CATEGORY_PERMISSION_DENIED, 89),

    ;

    /**
     * Checks specified exception for a folder-not-found error.
     *
     * @param e The exception to check
     * @return <code>true</code> if a folder-not-found error; otherwise <code>false</code>
     */
    public static boolean isNotFound(final OXException e) {
        return (OXFolderExceptionCode.NOT_EXISTS.getPrefix().equals(e.getPrefix()) && OXFolderExceptionCode.NOT_EXISTS.getNumber() == e.getCode());
    }

    /**
     * Message of the exception.
     */
    private final String message;

    /**
     * Category of the exception.
     */
    private final Category category;

    /**
     * Detail number of the exception.
     */
    private final int number;

    /**
     * The generic type.
     */
    private final Generic generic;

    private OXFolderExceptionCode(final String message, final Category category, final int number) {
        this(message, category, number, Generic.NONE);
    }

    private OXFolderExceptionCode(final String message, final Category category, final int number, final Generic generic) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.generic = generic;
    }

    @Override
    public String getPrefix() {
        return "FLD";
    }

    /**
     * Gets the (detail) number.
     *
     * @return The (detail) number.
     */
    @Override
    public int getNumber() {
        return number;
    }

    /**
     * Gets the (unformatted) message.
     *
     * @return The (unformatted) message.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Gets the category.
     *
     * @return The category.
     */
    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public boolean equals(final OXException e) {
        return getPrefix().equals(e.getPrefix()) && e.getCode() == getNumber();
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