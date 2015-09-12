package no.dv8.xhtml.serializer;

import no.dv8.xhtml.generation.support.Element;

public interface XHTMLSerializer<T> {
    Element<?> generateElement(T obj, int levels);
}
