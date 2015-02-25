package com.mobile.android.smartick.util;

/**
 * Created by sbarrio on 20/02/15.
 */

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.protocol.HttpContext;

import java.net.URI;

/**
 * Captura las redirecciones que se producen.
 * Nos quedamos con la primera porque en las siguientes llamadas el urlrewrite del servidor borra la jsessionid
 */
public class RedirectHandler extends DefaultRedirectHandler {
    public URI lastRedirectedUri;

    @Override
    public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
        return super.isRedirectRequested(response, context);
    }

    @Override
    public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
        lastRedirectedUri = super.getLocationURI(response, context);
        lastRedirectedUri.toString();
        return lastRedirectedUri;
    }
}
