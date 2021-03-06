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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

public final class ServletSerializerUtils
{

    private static final Logger LOGGER = LoggerFactory.getLogger( ServletSerializerUtils.class );

    private ServletSerializerUtils()
    {
    }

    public static <T> T fromRequestBody( final HttpServletRequest req, final JsonSerializer serializer, final Class<T> type )
    {
        String encoding = req.getCharacterEncoding();
        if ( encoding == null )
        {
            encoding = "UTF-8";
        }

        try
        {
            return serializer.fromStream( req.getInputStream(), encoding, type );
        }
        catch ( final IOException e )
        {
            LOGGER.error( "Failed to deserialize type: {} from HttpServletRequest body. Error: {}", e, type.getName(), e.getMessage() );

            throw new RuntimeException( "Cannot read request." );
        }
    }

    public static <T> T fromRequestBody( final HttpServletRequest req, final JsonSerializer serializer, final TypeToken<T> token )
    {
        String encoding = req.getCharacterEncoding();
        if ( encoding == null )
        {
            encoding = "UTF-8";
        }

        try
        {
            return serializer.fromStream( req.getInputStream(), encoding, token );
        }
        catch ( final IOException e )
        {
            LOGGER.error( "Failed to deserialize type: {} from HttpServletRequest body. Error: {}", e, token.getType(), e.getMessage() );

            throw new RuntimeException( "Cannot read request." );
        }
    }
}
