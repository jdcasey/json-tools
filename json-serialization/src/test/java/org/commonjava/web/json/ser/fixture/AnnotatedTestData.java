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
package org.commonjava.web.json.ser.fixture;

import java.lang.reflect.Type;

import org.commonjava.web.json.ser.JsonAdapters;
import org.commonjava.web.json.ser.WebSerializationAdapter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@JsonAdapters( AnnotatedTestData.Ser.class )
public class AnnotatedTestData
{

    private final String value;

    private final TestValue value2;

    public AnnotatedTestData( final String value, final String value2 )
    {
        this.value = value;
        this.value2 = new TestValue( value2 );
    }

    public TestValue getValue2()
    {
        return value2;
    }

    public String getValue()
    {
        return value;
    }

    @JsonAdapters( Ser2.class )
    public static final class TestValue
    {
        private final String innerValue;

        public TestValue( final String innerValue )
        {
            this.innerValue = innerValue;
        }

        public String getValue()
        {
            return innerValue;
        }
    }

    public static final class Ser
        implements WebSerializationAdapter, JsonSerializer<AnnotatedTestData>, JsonDeserializer<AnnotatedTestData>
    {

        @Override
        public AnnotatedTestData deserialize( final JsonElement json, final Type typeOfT,
                                              final JsonDeserializationContext context )
            throws JsonParseException
        {
            final String val = json.getAsJsonObject()
                                   .get( "foo" )
                                   .getAsString();

            final String val2 = json.getAsJsonObject()
                                    .get( "bar" )
                                    .getAsJsonObject()
                                    .get( "baz" )
                                    .getAsString();

            return new AnnotatedTestData( val, val2 );
        }

        @Override
        public JsonElement serialize( final AnnotatedTestData src, final Type typeOfSrc,
                                      final JsonSerializationContext context )
        {
            final JsonObject result = new JsonObject();
            result.addProperty( "foo", src.getValue() );

            final JsonElement v2 = context.serialize( src.getValue2() );
            result.add( "bar", v2 );
            return result;
        }

        @Override
        public void register( final GsonBuilder gsonBuilder )
        {
            gsonBuilder.registerTypeAdapter( AnnotatedTestData.class, this );
        }

    }

    public static final class Ser2
        implements WebSerializationAdapter, JsonSerializer<TestValue>
    {

        @Override
        public JsonElement serialize( final TestValue src, final Type typeOfSrc, final JsonSerializationContext context )
        {
            final JsonObject result = new JsonObject();
            result.addProperty( "baz", src.getValue() );
            return result;
        }

        @Override
        public void register( final GsonBuilder gsonBuilder )
        {
            gsonBuilder.registerTypeAdapter( TestValue.class, this );
        }

    }

}
