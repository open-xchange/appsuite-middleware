package com.openexchange.publish;

import java.util.Collection;


public interface PublicationService {
    public void publish(Publication publication);
    public void unpublish(Publication publication);
    
    public void create(Site site);
    public void delete(Site site);
    
    public Site getSite(Path path);
    public Site getSite(String path);
    public Collection<Site> getSites();
}
