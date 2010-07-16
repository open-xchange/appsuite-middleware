package com.openexchange.groupware.importexport;

import com.openexchange.groupware.AbstractOXException;
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
    private AbstractOXException exception;
    
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
    
    public void setId(String id) {
        this.id = id;
    }
    
    public MailMessage getMail() {
        return mail;
    }

    
    public void setMail(MailMessage mail) {
        this.mail = mail;
    }

    /**
     * 
     * @return true if contains Exception.
     */
    public boolean hasError() {
        return hasError;
    }
    
    public AbstractOXException getException() {
        return exception;
    }
    
    public void setException(AbstractOXException exception) {
        hasError = true;
        this.exception = exception;
    }

}
