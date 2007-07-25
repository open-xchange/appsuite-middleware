package com.openexchange.admin.console;

import java.io.PrintStream;

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
        if (getObjectName().getClass().getName().matches("create")) {
            createMessageForStderr(id, ctxid, "create");
        }
        System.err.println("Error:\n "+msg+"\n");    
    }

    protected void printInvalidInputMsg(final Integer id, final Integer ctxid, final String msg) {
        System.err.println("Invalid input detected: "+msg);    
    }    

}
