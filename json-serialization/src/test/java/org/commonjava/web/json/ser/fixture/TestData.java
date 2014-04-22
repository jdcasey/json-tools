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
