package com.openexchange.groupware.importexport;

import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;

/**
 * Result object for mail imports.
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 *
 */
public class MailImportResult {
    
    public static String ERROR = "Error";
    public static String FILENAME = "Filename";
    
    private String id;
    private MailMessage mail;
    private boolean hasError;
    private OXException exception;
    
    public MailImportResult() {
        super();
        id = null;
        mail = null;
        hasError = false;
        exception = null;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public MailMessage getMail() {
        return mail;
    }

    
    public void setMail(final MailMessage mail) {
        this.mail = mail;
    }

    /**
     * 
     * @return true if contains Exception.
     */
    public boolean hasError() {
        return hasError;
    }
    
    public OXException getException() {
        return exception;
    }
    
    public void setException(final OXException exception) {
        hasError = true;
        this.exception = exception;
    }

}
