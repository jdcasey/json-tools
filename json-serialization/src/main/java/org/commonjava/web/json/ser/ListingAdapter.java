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
