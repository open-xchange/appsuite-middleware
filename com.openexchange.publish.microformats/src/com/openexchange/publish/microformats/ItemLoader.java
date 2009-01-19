package com.openexchange.publish.microformats;

import com.openexchange.publish.Publication;


public interface ItemLoader<T> {
    public T load(Publication publication);
}
