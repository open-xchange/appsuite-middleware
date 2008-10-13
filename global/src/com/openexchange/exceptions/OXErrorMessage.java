package com.openexchange.exceptions;

import com.openexchange.groupware.AbstractOXException;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface OXErrorMessage {

    public int getErrorCode();

    public String getMessage();

    public String getHelp();

    public AbstractOXException.Category getCategory();

}
