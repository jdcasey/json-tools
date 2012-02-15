package org.commonjava.web.json.ser.fixture;


public class TestData
{
    private String email;

    private String name;

    public TestData()
    {
    }

    public TestData( final String email, final String name )
    {
        this.email = email;
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public String getName()
    {
        return name;
    }

    public void setEmail( final String email )
    {
        this.email = email;
    }

    public void setName( final String name )
    {
        this.name = name;
    }
}