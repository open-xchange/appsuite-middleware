package com.openexchange.admin.console;

import java.io.PrintStream;
import java.rmi.NotBoundException;

/**
 * This abstract class declares an abstract method to get the object name with which the command line tool
 * deals. This is used for output
 * 
 * @author d7
 *
 */
public abstract class ObjectNamingAbstraction extends BasicCommandlineOptions {

    protected abstract String getObjectName();

    protected final void displayCreatedMessage(final Integer id, final Integer ctxid) {
        createMessageForStdout(id, ctxid, "created");
    }

    protected final void displayChangedMessage(final Integer id, final Integer ctxid) {
        createMessageForStdout(id, ctxid, "changed");
    }

    protected final void displayDeletedMessage(Integer id, Integer ctxid) {
        createMessageForStdout(id, ctxid, "deleted");
    }
    
    protected void createMessageForStdout(final Integer id, final Integer ctxid, final String type) {
        createMessage(id, ctxid, type, System.out);
    }

    private void createMessage(final Integer id, final Integer ctxid, final String type, final PrintStream ps) {
        final StringBuilder sb = new StringBuilder(getObjectName());
        if (null != id) {
            sb.append(" ");
            sb.append(id);
        }
        if (null != ctxid) {
            sb.append(" in context ");
            sb.append(ctxid);
        }
        sb.append(" ");
        sb.append(type);
        ps.println(sb.toString());
    }
    
    private void createMessageForStderr(final Integer id, final Integer ctxid, final String type) {
        createMessage(id, ctxid, type, System.err);
    }
    
    protected final void printError(final Integer id, final Integer ctxid, final String msg) {
        printFirstPartOfErrorText(id, ctxid);
        printError(msg);    
    }

    protected final void printInvalidInputMsg(final Integer id, final Integer ctxid, final String msg) {
        printFirstPartOfErrorText(id, ctxid);
        printInvalidInputMsg(msg);    
    }    

    protected void printServerException(final Integer id, final Integer ctxid, final Exception e) {
        printFirstPartOfErrorText(id, ctxid);
        printServerException(e);
    }

    protected final void printNotBoundResponse(final Integer id, final Integer ctxid, final NotBoundException nbe){
        System.err.println("RMI module "+nbe.getMessage()+" not available on server");
    }

    private void printFirstPartOfErrorText(final Integer id, final Integer ctxid) {
        if (getClass().getName().matches("^.*\\..*(?i)create.*$")) {
            createMessageForStderr(id, ctxid, "could not be created: ");
        } else if (getClass().getName().matches("^.*\\..*(?i)change.*$")) {
            createMessageForStderr(id, ctxid, "could not be changed: ");
        } else if (getClass().getName().matches("^.*\\..*(?i)delete.*$")) {
            createMessageForStderr(id, ctxid, "could not be deleted: ");
        } else if (getClass().getName().matches("^.*\\..*(?i)list.*$")) {
            createMessageForStderr(id, ctxid, "could not be listed: ");
        } else if (getClass().getName().matches("^.*\\..*(?i)disable.*$")) {
            createMessageForStderr(id, ctxid, "could not be disabled: ");
        } else if (getClass().getName().matches("^.*\\..*(?i)move.*$")) {
            createMessageForStderr(id, ctxid, "could not be moved: ");
        }
    }
}
