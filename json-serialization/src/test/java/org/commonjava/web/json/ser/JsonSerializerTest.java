package org.commonjava.web.json.ser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.commonjava.web.json.model.Listing;
import org.commonjava.web.json.ser.fixture.AnnotatedTestData;
import org.commonjava.web.json.ser.fixture.TestData;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

public class JsonSerializerTest
{

    @Test
    public void deserializeEmptyStringReturnsNull()
    {
        final String json = "";
        final TestData data = new JsonSerializer().fromString( json, TestData.class );
        assertThat( data, nullValue() );
    }

    @Test
    public void serializeTestObject()
    {
        final String json = new JsonSerializer().toString( new TestData( "email@nowhere.com", "my name" ) );
        System.out.println( json );
    }

    @Test
    public void roundTripSet()
    {
        final Set<String> set = new HashSet<String>();
        set.add( "foo" );
        set.add( "bar" );
        set.add( "blat" );

        final String json = new JsonSerializer().toString( set );

        System.out.println( json );

        final Set<String> result = new JsonSerializer().fromString( json, new TypeToken<Set<String>>()
        {
        } );

        assertThat( result.contains( "foo" ), equalTo( true ) );
        assertThat( result.contains( "bar" ), equalTo( true ) );
        assertThat( result.contains( "blat" ), equalTo( true ) );
    }

    @Test
    public void serializeListing()
    {
        final Listing<TestData> listing =
            new Listing<TestData>( new TestData( "email@nowhere.com", "my name" ), new TestData( "root@nowhere.com", "other name" ) );

        final String json = new JsonSerializer().toString( listing, new TypeToken<Listing<TestData>>()
        {
        }.getType() );
        System.out.println( json );

        assertThat( json.contains( "items" ), equalTo( true ) );
        assertThat( json.contains( "email@nowhere.com" ), equalTo( true ) );
        assertThat( json.contains( "my name" ), equalTo( true ) );
        assertThat( json.contains( "root@nowhere.com" ), equalTo( true ) );
        assertThat( json.contains( "other name" ), equalTo( true ) );
    }

    @Test
    public void roundTripWithJsonAdaptersAnnotation()
    {
        final AnnotatedTestData td = new AnnotatedTestData( "This is a test", "test number 2" );
        final JsonSerializer ser = new JsonSerializer();
        final String json = ser.toString( td );

        assertThat( json.indexOf( "foo" ) > -1, equalTo( true ) );

        System.out.println( json );
        final AnnotatedTestData td2 = ser.fromString( json, AnnotatedTestData.class );

        assertThat( td2.getValue(), equalTo( td.getValue() ) );
        assertThat( td2.getValue2()
                       .getValue(), equalTo( td.getValue2()
                                               .getValue() ) );

    }

}
