package com.openexchange.groupware.contexts;

/**
 * Implements the context interface for testing purposes.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class TestContextImpl implements Context {

    /**
     * Identifier of the context used for the relational database.
     */
    private static final int contextId = 1;

    /**
     * Identifier of the context used for the relational database.
     */
    private static final Integer CONTEXT_ID = new Integer(contextId);

    /**
     * {@inheritDoc}
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(final String attributeName) {
        if (RDB_IDENTIFIER.equals(attributeName)) {
            return CONTEXT_ID;
        }
        return null;
    }
}
