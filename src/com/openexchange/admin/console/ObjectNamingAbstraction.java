package com.openexchange.admin.console;

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
        createMessage(id, ctxid, "created");
    }

    protected final void displayChangedMessage(final Integer id, final Integer ctxid) {
        createMessage(id, ctxid, "changed");
    }

    protected final void displayDeletedMessage(Integer id, Integer ctxid) {
        createMessage(id, ctxid, "deleted");
    }
    
    protected void createMessage(final Integer id, final Integer ctxid, final String type) {
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
        System.out.println(sb.toString());
    }

}
