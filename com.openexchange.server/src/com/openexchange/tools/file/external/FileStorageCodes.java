package com.openexchange.tools.file.external;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Error codes for the file storage exception.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public enum FileStorageCodes implements OXExceptionCode {
    /** An IO error occurred: %s */
    IOERROR(FileStorageExceptionMessage.IOERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 3),
    /** May be used to turn the IOException of getInstance into a proper OXException */
    INSTANTIATIONERROR(FileStorageExceptionMessage.INSTANTIATIONERROR_MSG, Category.CATEGORY_SERVICE_DOWN, 4),
    /** Cannot create directory \"%1$s\" for FileStorage. */
    CREATE_DIR_FAILED(FileStorageExceptionMessage.CREATE_DIR_FAILED_MSG, Category.CATEGORY_CONFIGURATION, 6),
    /** Unsupported encoding. */
    ENCODING(FileStorageExceptionMessage.ENCODING_MSG, Category.CATEGORY_ERROR, 9),
    /** Number parsing problem. */
    NO_NUMBER(FileStorageExceptionMessage.NO_NUMBER_MSG, Category.CATEGORY_ERROR, 10),
    /** File storage is full. */
    STORE_FULL(FileStorageExceptionMessage.STORE_FULL_MSG, Category.CATEGORY_CAPACITY, 11),
    /** Depth mismatch while computing next entry. */
    DEPTH_MISMATCH(FileStorageExceptionMessage.DEPTH_MISMATCH_MSG, Category.CATEGORY_ERROR, 12),
    /** Cannot remove lock file. */
    UNLOCK(FileStorageExceptionMessage.UNLOCK_MSG, Category.CATEGORY_SERVICE_DOWN, 13),
    /** Cannot create lock file here %1$s. Please check for a stale .lock file, permissions or too long usage of the filestore. */
    LOCK(FileStorageExceptionMessage.LOCK_MSG, Category.CATEGORY_SERVICE_DOWN, 14),
    /** Eliminating the FileStorage failed. */
    NOT_ELIMINATED(FileStorageExceptionMessage.NOT_ELIMINATED_MSG, Category.CATEGORY_SERVICE_DOWN, 16),
    /** File does not exist in filestore \"%1$s\". Consider running consistency tool. */
    FILE_NOT_FOUND(FileStorageExceptionMessage.FILE_NOT_FOUND_MSG, Category.CATEGORY_SERVICE_DOWN, 17);

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
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private FileStorageCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.category = category;
        this.number = detailNumber;
    }

    @Override
    public String getPrefix() {
        return "FLS";
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
