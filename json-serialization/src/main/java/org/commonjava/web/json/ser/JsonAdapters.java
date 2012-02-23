package org.commonjava.web.json.ser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface JsonAdapters
{

    Class<? extends WebSerializationAdapter>[] value();

}
