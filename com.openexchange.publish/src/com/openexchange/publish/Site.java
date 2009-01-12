package com.openexchange.publish;

import java.util.Collection;
import java.util.Iterator;


public class Site implements Iterable<Publication>{
    private Path path;
    
    private Collection<Publication> publications;
    
    public Path getPath() {
        return path;
    }
    
    public void setPath(Path path) {
        this.path = path;
    }
    
    public Collection<Publication> getPublications() {
        return publications;
    }
    
    public void addPublication(Publication publication) {
        publications.add( publication );
    }
    
    public void removePublication(Publication publication) {
        publications.remove( publication );
    }

    public Iterator<Publication> iterator() {
        return publications.iterator();
    }
}
