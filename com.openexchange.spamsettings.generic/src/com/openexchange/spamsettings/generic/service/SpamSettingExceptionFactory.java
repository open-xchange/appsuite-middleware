
package com.openexchange.spamsettings.generic.service;

import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.exceptions.Exceptions;

/**
 * {@link SpamSettingExceptionFactory} - Factory for creating {@link SpamSettingException}.
 *
 * @author francisco.laguna@open-xchange.com
 */
public final class SpamSettingExceptionFactory extends Exceptions<SpamSettingException> {

    private static final SpamSettingExceptionFactory SINGLETON = new SpamSettingExceptionFactory();

    /**
     * Prevent instantiation.
     */
    private SpamSettingExceptionFactory() {
        super();
    }

    /**
     * @return the singleton instance.
     */
    public static SpamSettingExceptionFactory getInstance() {
        return SINGLETON;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SpamSettingException createException(final ErrorMessage message, final Throwable cause, final Object... args) {
        return new SpamSettingException(message, cause, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void knownExceptions() {
        declareAll(SpamSettingExceptionCodes.values());
    }
}
