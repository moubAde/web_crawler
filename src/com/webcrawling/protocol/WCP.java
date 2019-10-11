package com.webcrawling.protocol;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.SelectionKey;
import java.util.List;

public interface WCP {
    /**server side protocol**/
    default void sendURL() throws IOException {}
    default void getResponse(SelectionKey sk) throws IOException {}

    /**client side protocol**/
    default void sendUrlList(List<URL> urls)throws IOException {}
    default void getUrl()throws IOException{}
}
