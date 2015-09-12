package no.dv8.xhtml.serializer;

import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.generation.support.Str;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class StringSerializer implements XHTMLSerializer<String> {
    @Override
    public Element<?> generateElement(String obj, int levels) {
        return string(obj);
    }

    public Str string(Object obj) {
        if (obj instanceof List) {
            List<?> l = (List<?>) obj;
            List<String> sl = l.stream().map(i -> i.toString()).collect(toList());
            return new Str(String.join(", ", sl));
        } else {
            return new Str(obj == null ? null : obj.toString());
        }
    }

}
