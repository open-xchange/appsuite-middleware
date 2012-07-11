

package com.openexchange.custom.parallels.soap.dataobjects;


/**
 * Contains all information describing a bundle
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Bundle {

    private final String name;

    private final String status;



    /**
     * Initializes a new {@link Bundle}.
     * @param name the name of the bundle
     * @param status the state of the bundle
     */
    public Bundle(final String name, final String status) {
        super();
        this.name = name;
        this.status = status;
    }



    /**
     * Returns the name of the bundle
     * 
     * @return the name of the bundle
     */
    public String getName() {
        return name;
    }



    /**
     * Returns the state of the bundle
     * 
     * @return the state of the bundle
     */
    public String getStatus() {
        return status;
    }

}
