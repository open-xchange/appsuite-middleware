package com.openexchange.admin.plugins;


/**
 * This class is used to return how the list query are expanded by a plugin. Therefore this class
 * contains two attributes: a table name which will be added, and a query part which will be added
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class SQLQueryExtension {

    private final String tablename;
    
    private final String querypart;

    /**
     * Initializes a new {@link SQLQueryExtension}.
     * @param tablename
     * @param querypart
     */
    public SQLQueryExtension(final String tablename, final String querypart) {
        super();
        this.tablename = tablename;
        this.querypart = querypart;
    }

    
    public final String getTablename() {
        return tablename;
    }

    
    public final String getQuerypart() {
        return querypart;
    }
    
}
