package org.commonjava.web.json.ser;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.commonjava.util.logging.Logger;

import com.google.gson.reflect.TypeToken;

public final class ServletSerializerUtils
{

    private static final Logger LOGGER = new Logger( ServletSerializerUtils.class );

    private ServletSerializerUtils()
    {
    }

    public static <T> T fromRequestBody( final HttpServletRequest req, final JsonSerializer serializer,
                                         final Class<T> type )
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
            LOGGER.error( "Failed to deserialize type: %s from HttpServletRequest body. Error: %s", e, type.getName(),
                          e.getMessage() );

            throw new RuntimeException( "Cannot read request." );
        }
    }

    public static <T> T fromRequestBody( final HttpServletRequest req, final JsonSerializer serializer,
                                         final TypeToken<T> token )
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
            LOGGER.error( "Failed to deserialize type: %s from HttpServletRequest body. Error: %s", e, token.getType(),
                          e.getMessage() );

            throw new RuntimeException( "Cannot read request." );
        }
    }
}
