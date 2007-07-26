package com.openexchange.admin.console;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.openexchange.admin.console.AdminParser.MissingOptionException;
import com.openexchange.admin.console.CmdLineParser.IllegalOptionValueException;
import com.openexchange.admin.console.CmdLineParser.UnknownOptionException;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

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
    
    protected void createMessageForStderr(final Integer id, final Integer ctxid, final String type) {
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

    protected void printFirstPartOfErrorText(final Integer id, final Integer ctxid) {
        if (getClass().getName().matches("^.*\\.\\w*(?i)create\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be created: ");
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)change\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be changed: ");
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)delete\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be deleted: ");
        } else if (getClass().getName().matches("^.*\\.\\w*(?i)list\\w*$")) {
            createMessageForStderr(id, ctxid, "could not be listed: ");
        }
    }

    protected void printErrors(final Integer id, final Integer ctxid, final Exception e, AdminParser parser) {
        // Remember that all the exceptions in this list must be written in the order with the lowest exception first
        // e.g. if Aexception extends Bexception then Aexception has to be written before Bexception in this list. Otherwise
        // the if clause for Bexception will match beforehand
        if (e instanceof ConnectException) {
            final ConnectException new_name = (ConnectException) e;
            printError(id, ctxid, new_name.getMessage());
            sysexit(SYSEXIT_COMMUNICATION_ERROR);
        } else if (e instanceof NumberFormatException) {
            printInvalidInputMsg(id, ctxid, "Ids must be numbers!");
            sysexit(1);
        } else if (e instanceof MalformedURLException) {
            final MalformedURLException exc = (MalformedURLException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof RemoteException) {
            final RemoteException exc = (RemoteException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } else if (e instanceof NotBoundException) {
            final NotBoundException exc = (NotBoundException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof InvalidCredentialsException) {
            final InvalidCredentialsException exc = (InvalidCredentialsException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } else if (e instanceof NoSuchContextException) {
            final NoSuchContextException exc = (NoSuchContextException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_NO_SUCH_CONTEXT);
        } else if (e instanceof InvocationTargetException) {
            printError(id, ctxid, e.getMessage());
            sysexit(1);
        } else if (e instanceof StorageException) {
            final StorageException exc = (StorageException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_SERVERSTORAGE_ERROR);
        } else if (e instanceof InvalidDataException) {
            final InvalidDataException exc = (InvalidDataException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_INVALID_DATA);
        } else if (e instanceof IllegalArgumentException) {
            printError(id, ctxid, e.getMessage());
            sysexit(1);
        } else if (e instanceof IllegalAccessException) {
            printError(id, ctxid, e.getMessage());
            sysexit(1);
        } else if (e instanceof IllegalOptionValueException) {
            final IllegalOptionValueException exc = (IllegalOptionValueException) e;
            printError(id, ctxid, "Illegal option value : " + exc.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } else if (e instanceof UnknownOptionException) {
            final UnknownOptionException exc = (UnknownOptionException) e;
            printError(id, ctxid, "Unrecognized options on the command line: " + exc.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } else if (e instanceof MissingOptionException) {
            final MissingOptionException missing = (MissingOptionException) e;
            printError(id, ctxid, missing.getMessage());
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } else if (e instanceof DatabaseUpdateException) {
            final DatabaseUpdateException exc = (DatabaseUpdateException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof NoSuchUserException) {
            final NoSuchUserException exc = (NoSuchUserException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_NO_SUCH_USER);
        } else if (e instanceof NoSuchGroupException) {
            final NoSuchGroupException exc = (NoSuchGroupException) e;
            printServerException(id, ctxid, exc);
            sysexit(SYSEXIT_NO_SUCH_GROUP);
        } else if (e instanceof NoSuchResourceException) {
            printServerException(id, ctxid, e);
            sysexit(SYSEXIT_NO_SUCH_RESOURCE);
        } else if (e instanceof DuplicateExtensionException) {
            final DuplicateExtensionException exc = (DuplicateExtensionException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof ContextExistsException) {
            final ContextExistsException exc = (ContextExistsException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        } else if (e instanceof URISyntaxException) {
            final URISyntaxException exc = (URISyntaxException) e;
            printServerException(id, ctxid, exc);
            sysexit(1);
        }
    }
}
