
package com.openexchange.subscribe.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;
import org.xml.sax.helpers.DefaultHandler;
import com.openexchange.subscribe.Subscription;

/**
 * 
 * {@link AbstractMicroformatSAXHandler}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 * @param <T> Type this handler handles, an ox data object like Task or ContactObject or CalendarDataObject
 */
public abstract class AbstractMicroformatSAXHandler<T> extends DefaultHandler {

    protected Collection<T> objects;
    protected T currentObject;
    public String currentlyReading;

    /**
     * Create an instance of the object this data is parsed into.
     * @return
     */
    public abstract T instantiate();
    
    public AbstractMicroformatSAXHandler() {
        super();
        objects = new LinkedList<T>();
    }

    /**
     * Takes a character array, as used by SAX, and returns a substring from beginning to end
     * @param arr
     * @param start
     * @param end
     * @return
     */
    protected String stringArray(char[] arr, int start, int end) {
        StringBuilder bob = new StringBuilder( new String(arr));
        return bob.substring(start, end);
    }

    /**
     * Read the site of a subscription and return its content as a string
     * @param subscription
     * @return
     * @throws IOException
     */
    protected String readSubscription(Subscription subscription) throws IOException{ //TODO: refactor to composite pattern
        URL url = null; //new URL(subscription.getUrl());
        URLConnection connection = url.openConnection();
        BufferedReader buffy = new BufferedReader( new InputStreamReader( connection.getInputStream() ) );
        StringBuilder bob = new StringBuilder();
        String line = buffy.readLine();
        while (line != null){
            bob.append (line);
            bob.append ("\n");
            line = buffy.readLine();
        }
        return bob.toString();
    }
    
    /**
     * This should be called each time an object is done reading
     */
    protected void newObjectEncountered() {
        if( null != currentObject ){
            objects.add( currentObject ); //clone?
        }
        currentObject = instantiate();
    }





    public void endElement(String uri, String name, String qName) {
        currentlyReading = null;
    }

    public Collection<T> getObjects() {
        if(currentObject != null){
            objects.add(currentObject);
        }
        return objects;
    }

}
