package no.dv8.xhtml.serializer;

import no.dv8.xhtml.generation.elements.li;
import no.dv8.xhtml.generation.elements.ul;
import no.dv8.xhtml.generation.support.Element;

import java.util.List;

public class ListSerializer implements XHTMLSerializer<List> {
    XHTMLSerialize<?> parent;
    public <T> ListSerializer(XHTMLSerialize parent) {
        this.parent = parent;
    }

    @Override
    public Element<?> generateElement(List l, int levels) {
        ul res = new ul();
        for( Object o: l ) {
            li li = new li().add(parent.generateElement(o, levels - 1) );
            res.add(li);
        }
        return res;
    }
}
