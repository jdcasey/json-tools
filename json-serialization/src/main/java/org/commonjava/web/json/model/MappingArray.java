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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MappingArray
    implements Iterable<String>
{

    private String[] elements;

    public String[] getElements()
    {
        return elements;
    }

    public void setElements( final String[] elements )
    {
        this.elements = elements;
    }

    @Override
    public Iterator<String> iterator()
    {
        List<String> eltList;
        if ( elements == null )
        {
            eltList = Collections.emptyList();
        }
        else
        {
            eltList = Arrays.asList( elements );
        }

        return eltList.iterator();
    }

    @Override
    public String toString()
    {
        return elements == null ? "-NONE-" : join( elements, ", " );
    }

}
