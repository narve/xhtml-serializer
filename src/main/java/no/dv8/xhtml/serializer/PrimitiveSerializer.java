package no.dv8.xhtml.serializer;

import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.generation.support.Str;

public class PrimitiveSerializer implements XHTMLSerializer<Object> {
    @Override
    public Element<?> generateElement(Object obj, int levels) {
        return new Str( obj==null?"":obj.toString() );
    }
}
