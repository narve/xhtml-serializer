package no.dv8.xhtml.serializer;

import no.dv8.xhtml.generation.elements.span;
import no.dv8.xhtml.generation.support.Element;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateSerializer implements XHTMLSerializer<Date> {
    public DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;

    @Override
    public Element<?> generateElement(Date obj, int levels) {
        Date d = (Date) obj;
        Instant i = Instant.ofEpochMilli(d.getTime());
        return dateElement(i);
    }

    public Element dateElement(Instant i) {
        return new span(i == null ? "<null>" : dtf.format(i));
    }
}
