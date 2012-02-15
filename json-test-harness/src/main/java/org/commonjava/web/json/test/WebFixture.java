/*******************************************************************************
 * Copyright 2011 John Casey
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.commonjava.web.json.test;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.io.IOUtils.copy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HttpContext;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.json.model.Listing;
import org.commonjava.web.json.ser.JsonSerializer;
import org.junit.rules.ExternalResource;

import com.google.gson.reflect.TypeToken;

public class WebFixture
    extends ExternalResource
{
    private final Logger logger = new Logger( getClass() );

    public static final String DEFAULT_HOST = "localhost";

    public static final String DEFAULT_PORT = "8080";

    private static final String QARQAS_PROPERTIES = "qarqas.properties";

    private static final String QARQAS_HTTP_PROP = "qarqas.export.http";

    private static final String HTTP_PROP = "HTTP_PORT";

    private static final String DEFAULT_BASE = "/test/api";

    private JsonSerializer serializer;

    private DefaultHttpClient http;

    private int port;

    private String host = DEFAULT_HOST;

    private String apiVersion = "1.0";

    private String user;

    private String pass;

    private String basePath;

    public WebFixture()
    {
        this.serializer = new JsonSerializer();
        initPort();
    }

    public WebFixture( final JsonSerializer serializer )
    {
        this.serializer = serializer;
        initPort();
    }

    private void initPort()
    {
        final InputStream stream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream( QARQAS_PROPERTIES );

        final Properties props = new Properties();
        if ( stream != null )
        {
            try
            {
                props.load( stream );
            }
            catch ( final IOException e )
            {
            }
            finally
            {
                closeQuietly( stream );
            }

            final StringWriter sw = new StringWriter();
            props.list( new PrintWriter( sw ) );
            logger.info( "Loaded properties from: %s\n\n%s", QARQAS_PROPERTIES, sw.toString() );
        }

        String portStr = props.getProperty( QARQAS_HTTP_PROP );
        if ( portStr == null )
        {
            portStr = System.getProperty( HTTP_PROP, DEFAULT_PORT );
        }

        logger.info( "HTTP port: %s", portStr );
        this.port = Integer.parseInt( portStr );
    }

    public void disableRedirection()
    {
        http.setRedirectStrategy( new DefaultRedirectStrategy()
        {
            @Override
            public boolean isRedirected( final HttpRequest request, final HttpResponse response,
                                         final HttpContext context )
                throws ProtocolException
            {
                return false;
            }
        } );
    }

    public void enableRedirection()
    {
        http.setRedirectStrategy( new DefaultRedirectStrategy() );
    }

    @Override
    protected void before()
        throws Exception
    {
        final ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager();
        ccm.setMaxTotal( 20 );

        http = new DefaultHttpClient( ccm );
        http.setCredentialsProvider( new CredentialsProvider()
        {

            @Override
            public void setCredentials( final AuthScope authscope, final Credentials credentials )
            {
            }

            @Override
            public Credentials getCredentials( final AuthScope authscope )
            {
                if ( user != null )
                {
                    return new UsernamePasswordCredentials( user, pass );
                }

                return null;
            }

            @Override
            public void clear()
            {
            }
        } );
    }

    public void assertLocationHeader( final HttpResponse response, final String value )
    {
        final Header[] headers = response.getHeaders( HttpHeaders.LOCATION );
        assertThat( headers, notNullValue() );
        assertThat( headers.length, equalTo( 1 ) );

        final String header = headers[0].getValue();
        assertThat( header, equalTo( value ) );
    }

    public <T> T get( final String url, final Class<T> type )
        throws Exception
    {
        logger.info( "WebFixture: GET '%s', expecting: 200, return-type: %s", url, type.getName() );
        final HttpGet get = new HttpGet( url );
        get.setHeader( HttpHeaders.ACCEPT, "application/json" );
        try
        {
            return http.execute( get, new ResponseHandler<T>()
            {
                @SuppressWarnings( "unchecked" )
                @Override
                public T handleResponse( final HttpResponse response )
                    throws ClientProtocolException, IOException
                {
                    final StatusLine sl = response.getStatusLine();
                    assertThat( sl.getStatusCode(), equalTo( HttpStatus.SC_OK ) );

                    return serializer.fromStream( response.getEntity()
                                                          .getContent(), "UTF-8", type );
                }
            } );
        }
        finally
        {
            get.abort();
        }
    }

    public void get( final String url, final int expectedStatus )
        throws Exception
    {
        logger.info( "WebFixture: GET '%s', expecting: %s", url, expectedStatus );
        final HttpGet get = new HttpGet( url );
        try
        {
            http.execute( get, new ResponseHandler<Void>()
            {
                @Override
                public Void handleResponse( final HttpResponse response )
                    throws ClientProtocolException, IOException
                {
                    final StatusLine sl = response.getStatusLine();
                    assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );

                    return null;
                }
            } );
        }
        finally
        {
            get.abort();
        }
    }

    public String getString( final String url, final int expectedStatus )
        throws ClientProtocolException, IOException
    {
        final HttpResponse response = http.execute( new HttpGet( url ) );
        final StatusLine sl = response.getStatusLine();

        assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );
        assertThat( response.getEntity(), notNullValue() );

        final StringWriter sw = new StringWriter();
        copy( response.getEntity()
                      .getContent(), sw );

        return sw.toString();
    }

    public HttpResponse getWithResponse( final String url, final int expectedStatus )
        throws Exception
    {
        final HttpGet get = new HttpGet( url );
        try
        {
            final HttpResponse response = http.execute( get );
            final StatusLine sl = response.getStatusLine();
            assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );

            return response;
        }
        finally
        {
            get.abort();
        }
    }

    public HttpResponse getWithResponse( final String url, final int expectedStatus, final String accept )
        throws Exception
    {
        final HttpGet get = new HttpGet( url );
        get.setHeader( "Accept", accept );

        try
        {
            final HttpResponse response = http.execute( get );
            final StatusLine sl = response.getStatusLine();
            assertThat( sl.getStatusCode(), equalTo( expectedStatus ) );

            return response;
        }
        finally
        {
            get.abort();
        }
    }

    public <T> Listing<T> getListing( final String url, final TypeToken<Listing<T>> token )
        throws Exception
    {
        final HttpGet get = new HttpGet( url );
        try
        {
            return http.execute( get, new ResponseHandler<Listing<T>>()
            {
                @SuppressWarnings( "unchecked" )
                @Override
                public Listing<T> handleResponse( final HttpResponse response )
                    throws ClientProtocolException, IOException
                {
                    final StatusLine sl = response.getStatusLine();
                    assertThat( sl.getStatusCode(), equalTo( HttpStatus.SC_OK ) );

                    return serializer.listingFromStream( response.getEntity()
                                                                 .getContent(), "UTF-8", token );
                }
            } );
        }
        finally
        {
            get.abort();
        }
    }

    public HttpResponse delete( final String url )
        throws Exception
    {
        final HttpDelete request = new HttpDelete( url );
        try
        {
            final HttpResponse response = http.execute( request );

            assertThat( response.getStatusLine()
                                .getStatusCode(), equalTo( HttpStatus.SC_OK ) );

            return response;
        }
        finally
        {
            request.abort();
        }
    }

    public HttpResponse put( final String url, final int status )
        throws Exception
    {
        final HttpPut request = new HttpPut( url );

        try
        {
            final HttpResponse response = http.execute( request );

            assertThat( response.getStatusLine()
                                .getStatusCode(), equalTo( status ) );

            return response;
        }
        finally
        {
            request.abort();
        }
    }

    public HttpResponse put( final String url, final Object value, final int status )
        throws Exception
    {
        final HttpPut request = new HttpPut( url );
        if ( value != null )
        {
            request.setEntity( new StringEntity( serializer.toString( value ), "application/json", "UTF-8" ) );
        }

        try
        {
            final HttpResponse response = http.execute( request );

            assertThat( response.getStatusLine()
                                .getStatusCode(), equalTo( status ) );

            return response;
        }
        finally
        {
            request.abort();
        }
    }

    public HttpResponse put( final String url, final int status, final InputStream stream, final String contentType,
                             final int contentLength )
        throws Exception
    {
        final HttpPut request = new HttpPut( url );
        request.setHeader( HttpHeaders.CONTENT_TYPE, contentType );
        request.setEntity( new InputStreamEntity( stream, contentLength ) );

        try
        {
            final HttpResponse response = http.execute( request );

            assertThat( response.getStatusLine()
                                .getStatusCode(), equalTo( status ) );

            return response;
        }
        finally
        {
            request.abort();
        }
    }

    public HttpResponse post( final String url, final Object value, final int status )
        throws Exception
    {
        final HttpPost request = new HttpPost( url );
        request.setEntity( new StringEntity( serializer.toString( value ), "application/json", "UTF-8" ) );

        try
        {
            final HttpResponse response = http.execute( request );

            assertThat( response.getStatusLine()
                                .getStatusCode(), equalTo( status ) );

            return response;
        }
        finally
        {
            request.abort();
        }
    }

    public HttpResponse post( final String url, final Object value, final Type type, final int status )
        throws Exception
    {
        final HttpPost request = new HttpPost( url );
        request.setEntity( new StringEntity( serializer.toString( value, type ), "application/json", "UTF-8" ) );

        try
        {
            final HttpResponse response = http.execute( request );

            assertThat( response.getStatusLine()
                                .getStatusCode(), equalTo( status ) );

            return response;
        }
        finally
        {
            request.abort();
        }
    }

    public String resourceUrl( final String... path )
        throws MalformedURLException
    {
        return buildUrl( "http://" + host + ( port == 80 ? "" : ":" + port ) + getResourceBase(), path );
    }

    public String getApiVersion()
    {
        return apiVersion;
    }

    public void setApiVersion( final String apiVersion )
    {
        this.apiVersion = apiVersion;
    }

    public static String buildUrl( final String baseUrl, final String... parts )
        throws MalformedURLException
    {
        return buildUrl( baseUrl, (Map<String, String>) null, parts );
    }

    public static String buildUrl( final String baseUrl, final Map<String, String> params, final String... parts )
        throws MalformedURLException
    {
        if ( parts == null || parts.length < 1 )
        {
            return baseUrl;
        }

        final StringBuilder urlBuilder = new StringBuilder();

        if ( !parts[0].startsWith( baseUrl ) )
        {
            urlBuilder.append( baseUrl );
        }

        for ( String part : parts )
        {
            if ( part.startsWith( "/" ) )
            {
                part = part.substring( 1 );
            }

            if ( urlBuilder.length() > 0 && urlBuilder.charAt( urlBuilder.length() - 1 ) != '/' )
            {
                urlBuilder.append( "/" );
            }

            urlBuilder.append( part );
        }

        if ( params != null && !params.isEmpty() )
        {
            urlBuilder.append( "?" );
            boolean first = true;
            for ( final Map.Entry<String, String> param : params.entrySet() )
            {
                if ( first )
                {
                    first = false;
                }
                else
                {
                    urlBuilder.append( "&" );
                }

                urlBuilder.append( param.getKey() )
                          .append( "=" )
                          .append( param.getValue() );
            }
        }

        return new URL( urlBuilder.toString() ).toExternalForm();
    }

    public JsonSerializer getSerializer()
    {
        return serializer;
    }

    public DefaultHttpClient getHttp()
    {
        return http;
    }

    public int getPort()
    {
        return port;
    }

    public String getHost()
    {
        return host;
    }

    public void setSerializer( final JsonSerializer serializer )
    {
        this.serializer = serializer;
    }

    public void setPort( final int port )
    {
        this.port = port;
    }

    public void setHost( final String host )
    {
        this.host = host;
    }

    public void setCredentials( final String user, final String pass )
    {
        this.user = user;
        this.pass = pass;
    }

    public String getResourceBase()
    {
        return basePath == null ? DEFAULT_BASE + "/" + getApiVersion() : basePath;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath( final String basePath )
    {
        this.basePath = basePath;
    }
}
