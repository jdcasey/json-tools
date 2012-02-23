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

    public AnnotatedTestData( final String value )
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
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
            return new AnnotatedTestData( val );
        }

        @Override
        public JsonElement serialize( final AnnotatedTestData src, final Type typeOfSrc,
                                      final JsonSerializationContext context )
        {
            final JsonObject result = new JsonObject();
            result.addProperty( "foo", src.getValue() );
            return result;
        }

        @Override
        public void register( final GsonBuilder gsonBuilder )
        {
            gsonBuilder.registerTypeAdapter( AnnotatedTestData.class, this );
        }

    }

}
