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
package org.commonjava.web.json.model;

import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Listing<T>
{

    private List<T> items;

    public Listing()
    {
    }

    public Listing( final T... elements )
    {
        items = Arrays.asList( elements );
    }

    public Listing( final Collection<T> elements )
    {
        this.items = new ArrayList<T>( elements );
    }

    public List<T> getItems()
    {
        return items;
    }

    public void setItems( final List<T> items )
    {
        this.items = items;
    }

    @Override
    public String toString()
    {
        return "Listing: [items:\n\t" + join( items, "\n\t" ) + "\n]";
    }

}
