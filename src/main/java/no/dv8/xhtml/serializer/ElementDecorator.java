package no.dv8.xhtml.serializer;

import no.dv8.xhtml.generation.support.Element;

@FunctionalInterface
public interface ElementDecorator {

    <T extends Element> Element<T> decorate( Element<T> t, Object obj, int level );

}
