package org.commonjava.web.json.ser;

import java.lang.reflect.Type;
import java.util.List;

import org.commonjava.web.json.model.Listing;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ListingAdapter
    implements WebSerializationAdapter, JsonSerializer<Listing<?>>
{

    @Override
    public void register( final GsonBuilder gsonBuilder )
    {
        gsonBuilder.registerTypeAdapter( Listing.class, this );
    }

    @Override
    public JsonElement serialize( final Listing<?> src, final Type typeOfSrc, final JsonSerializationContext context )
    {
        final JsonObject result = new JsonObject();

        final JsonArray array = new JsonArray();
        result.add( "items", array );

        final List<?> items = src.getItems();
        for ( final Object item : items )
        {
            final JsonElement element = context.serialize( item );
            array.add( element );
        }

        return result;
    }

}
