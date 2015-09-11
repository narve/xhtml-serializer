package no.dv8.xhtml.serializer;

import lombok.extern.java.Log;
import no.dv8.xhtml.generation.elements.*;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.generation.support.Str;

import javax.xml.bind.annotation.XmlTransient;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Log
public class XHTMLSerializer<T> {

    DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;

    public Element<?> generateElement(final Object obj, final int levels) {
        Objects.requireNonNull(obj, "Can't serialize null");
        if (levels < 0)
            throw new IllegalArgumentException("levels < 0: " + levels);

        if (obj instanceof List) {
            List<?> l = (List) obj;
            ul res = new ul();
            l.stream()
              .map(i -> generateElement(i, levels))
              .map(i -> new li().add(i))
              .forEach(res::add);
            return res;
        }

        if (obj instanceof Date) {
            Date d = (Date) obj;
            Instant i = Instant.ofEpochMilli(d.getTime());
            return dateElement(i);
        }

        if (obj instanceof Instant) {
            Instant i = (Instant) obj;
            return dateElement(i);
        }

        if (obj instanceof String) {
//            return new span(obj == null ? "<null>" : obj.toString());
            return string(obj);
        }

        if (levels == 0) {
            return string(obj);
        }

        return new div()
          .add(header(obj))
          .add(propList(obj, levels));
    }

    public Element<?> string(Object obj) {
        if (obj instanceof List) {
            List<?> l = (List<?>) obj;
            List<String> sl = l.stream().map(i -> i.toString()).collect(toList());
            return new Str(String.join(", ", sl));
        } else {
            return new Str(obj == null ? null : obj.toString());
        }
    }

    public Element dateElement(Instant i) {
        return new span(i == null ? "<null>" : dtf.format(i));
    }

    public Element<?> header(Object obj) {
        return new h1(obj.getClass() + ": " + obj);
    }

    public boolean shouldIgnore(PropertyDescriptor pd) {
        Set<String> ignorables = new HashSet<>(asList("jsonignore", "xmltransient"));
        List<Annotation> annotations = asList(pd.getReadMethod().getAnnotations());
        Annotation[] aa = annotations.toArray(new Annotation[0]);
        if (Annotations.hasAnnotation(XmlTransient.class, aa))
            return true;
        return annotations.stream().anyMatch(a -> ignorables.contains(a.getClass().getSimpleName().toLowerCase()));
    }

    public dl propList(Object item, int levels) {
        dl props = new dl().clz("props");
        try {
            List<PropertyDescriptor> pda = asList(Introspector.getBeanInfo(item.getClass()).getPropertyDescriptors());
            pda
              .stream()
              .filter(pd -> !shouldIgnore(pd))
              .filter(pd -> !"class".equals(pd.getName()))
              .forEach(pd -> props.add(toXHTML(pd, item, levels)));
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    public List<Element<?>> toXHTML(PropertyDescriptor f, Object item, int levels) {
        if (levels < 0)
            throw new IllegalArgumentException("levels < 0: " + levels);
        Object val = null;
        try {
            val = f.getReadMethod().invoke(item);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        if (levels == 0) {
            return asList(string(val));
        } else {
            return definition(f.getName(), val, levels);
        }
    }


    public List<Element<?>> definition(String name, Object val, int levels) {
        Element<?> content = val == null ? string(val) : generateElement(val, levels - 1);
        return asList(
          new dt(name),
          new dd().add(content)
        );
    }

}
