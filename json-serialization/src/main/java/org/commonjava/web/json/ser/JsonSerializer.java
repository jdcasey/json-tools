/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.commonjava.web.json.ser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.commonjava.web.json.model.Listing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@ApplicationScoped
@Alternative
@Named( "base" )
public class JsonSerializer
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private static final WebSerializationAdapter[] DEFAULT_ADAPTERS = { new ListingAdapter() };

    private final Set<WebSerializationAdapter> baseAdapters = new HashSet<WebSerializationAdapter>();

    @Inject
    @Any
    Instance<WebSerializationAdapter> adapterInstance;

    JsonSerializer()
    {
    }

    public JsonSerializer( final WebSerializationAdapter... baseAdapters )
    {
        this.baseAdapters.addAll( Arrays.asList( baseAdapters ) );
    }

    public void registerSerializationAdapters( final WebSerializationAdapter... adapters )
    {
        this.baseAdapters.addAll( Arrays.asList( adapters ) );
    }

    private Gson getGson( final Type type )
    {
        final GsonBuilder builder = new GsonBuilder();
        if ( type != null )
        {
            registerAnnotationAdapters( type, builder, new HashSet<Type>() );
        }

        if ( adapterInstance != null )
        {
            for ( final WebSerializationAdapter adapter : adapterInstance )
            {
                adapter.register( builder );
            }
        }

        if ( baseAdapters != null )
        {
            for ( final WebSerializationAdapter adapter : baseAdapters )
            {
                adapter.register( builder );
            }
        }

        for ( final WebSerializationAdapter adapter : DEFAULT_ADAPTERS )
        {
            adapter.register( builder );
        }

        return builder.create();
    }

    private void registerAnnotationAdapters( final Type type, final GsonBuilder builder, final Set<Type> seen )
    {
        if ( seen.contains( type ) )
        {
            return;
        }

        seen.add( type );
        if ( type instanceof Class<?> )
        {
            final Class<?> typeCls = (Class<?>) type;
            final JsonAdapters adapters = typeCls.getAnnotation( JsonAdapters.class );
            if ( adapters != null )
            {
                for ( final Class<? extends WebSerializationAdapter> adapterCls : adapters.value() )
                {
                    try
                    {
                        logger.debug( "[REGISTER] JSON adapter from annotation: {}", adapterCls.getName() );
                        adapterCls.newInstance()
                                  .register( builder );
                    }
                    catch ( final InstantiationException e )
                    {
                        throw new RuntimeException( "Cannot instantiate adapter from JsonAdapters annotation: " + adapterCls.getName() );
                    }
                    catch ( final IllegalAccessException e )
                    {
                        throw new RuntimeException( "Cannot instantiate adapter from JsonAdapters annotation: " + adapterCls.getName() );
                    }
                }
            }

            final Field[] fields = typeCls.getDeclaredFields();
            for ( final Field field : fields )
            {
                final Class<?> fieldType = field.getType();
                registerAnnotationAdapters( fieldType, builder, seen );
            }
        }
    }

    public String toString( final Object src )
    {
        return getGson( src.getClass() ).toJson( src );
    }

    public String toString( final Object src, final Type type )
    {
        return getGson( src.getClass() ).toJson( src, type );
    }

    public <T> T fromString( final String src, final Type type )
    {
        final T result = getGson( type ).fromJson( src, type );

        return result;
    }

    public <T> T fromString( final String src, final TypeToken<T> token )
    {
        final T result = getGson( token.getType() ).fromJson( src, token.getType() );

        return result;
    }

    public <T> T fromStream( final InputStream stream, String encoding, final Class<T> type )
    {
        if ( encoding == null )
        {
            encoding = "UTF-8";
        }

        try
        {
            final Reader reader = new InputStreamReader( stream, encoding );
            final String json = IOUtils.toString( reader );
            logger.debug( "JSON:\n\n{}\n\n", json );

            T result = getGson( type ).fromJson( json, type );
            result = postProcess( result );

            return result;
        }
        catch ( final UnsupportedEncodingException e )
        {
            logger.error( "Failed to deserialize type: {}. Error: {}", e, type.getName(), e.getMessage() );
            throw new RuntimeException( "Cannot read stream." );
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to deserialize type: {}. Error: {}", e, type.getName(), e.getMessage() );
            throw new RuntimeException( "Cannot read stream." );
        }
    }

    private <T> T postProcess( final T input )
    {
        // TODO: Read postprocessing annotation and invoke in order...
        return input;
    }

    public <T> T fromStream( final InputStream stream, String encoding, final TypeToken<T> token )
    {
        if ( encoding == null )
        {
            encoding = "UTF-8";
        }

        try
        {
            final Reader reader = new InputStreamReader( stream, encoding );
            final String json = IOUtils.toString( reader );
            logger.debug( "JSON:\n\n{}\n\n", json );

            final T result = getGson( token.getType() ).fromJson( json, token.getType() );

            return result;
        }
        catch ( final UnsupportedEncodingException e )
        {
            logger.error( "Failed to deserialize type: {}. Error: {}", e, token.getType(), e.getMessage() );
            throw new RuntimeException( "Cannot read stream." );
        }
        catch ( final IOException e )
        {
            logger.error( "Failed to deserialize type: {}. Error: {}", e, token.getType(), e.getMessage() );
            throw new RuntimeException( "Cannot read stream." );
        }
    }

    public <T> Listing<T> listingFromStream( final InputStream stream, String encoding, final TypeToken<Listing<T>> token,
                                             final DeserializerPostProcessor<T>... postProcessors )
    {
        if ( encoding == null )
        {
            encoding = "UTF-8";
        }

        try
        {
            Listing<T> result = getGson( null ).fromJson( new InputStreamReader( stream, encoding ), token.getType() );

            if ( result != null && result.getItems() != null )
            {
                final List<T> items = result.getItems();
                Collections.reverse( items );

                result = new Listing<T>( items );
                for ( final T item : result.getItems() )
                {
                    for ( final DeserializerPostProcessor<T> proc : postProcessors )
                    {
                        proc.process( item );
                    }
                }
            }

            return result;
        }
        catch ( final UnsupportedEncodingException e )
        {
            logger.error( "Failed to deserialize type: {}. Error: {}", e, token.getType(), e.getMessage() );

            throw new RuntimeException( "Cannot read stream." );
        }
    }

    public <T> Listing<T> listingFromString( final String src, final TypeToken<Listing<T>> token,
                                             final DeserializerPostProcessor<T>... postProcessors )
    {
        Listing<T> result = getGson( null ).fromJson( src, token.getType() );

        if ( result != null && result.getItems() != null )
        {
            final List<T> items = result.getItems();
            Collections.reverse( items );

            result = new Listing<T>( items );
            for ( final T item : result.getItems() )
            {
                for ( final DeserializerPostProcessor<T> proc : postProcessors )
                {
                    proc.process( item );
                }
            }
        }

        return result;
    }

}
