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
public class XHTMLSerialize<T> implements XHTMLSerializer {

    public Map<Class<?>, XHTMLSerializer> typeMap = new HashMap<>();

    {
        typeMap.put( Date.class, new DateSerializer() );
        typeMap.put( Instant.class, new InstantSerializer() );
        typeMap.put( String.class, new StringSerializer() );
        typeMap.put( Enum.class, new StringSerializer() );
        typeMap.put( Number.class, new StringSerializer() );
        typeMap.put( List.class, new ListSerializer(this) );
        typeMap.put( ArrayList.class, new ListSerializer(this) );
    }

    public <X> Optional<XHTMLSerializer> serializerFor(Class<X> clz) {
        return typeMap.entrySet().stream().filter(me -> canSerialize(me.getKey(), clz)).map(me -> me.getValue()).findFirst();
    }

    public <X> boolean canSerialize(Class<?> key, Class<X> clz) {
//        return clz.isAssignableFrom(key);
        return key.isAssignableFrom(clz);
    }


    public DateTimeFormatter dtf = DateTimeFormatter.ISO_INSTANT;
    public ElementDecorator decorator = new ElementDecorator() {
        @Override
        public <T extends Element> Element<T> decorate(Element<T> t, Object obj, int level) {
            return t;
        }
    };


    public Str string(Object obj) {
        if (obj instanceof List) {
            List<?> l = (List<?>) obj;
            List<String> sl = l.stream().map(i -> i.toString()).collect(toList());
            return new Str(String.join(", ", sl));
        } else {
            return new Str(obj == null ? null : obj.toString());
        }
    }

    @Override
    public Element<?> generateElement(final Object obj, final int levels) {
        Objects.requireNonNull(obj, "Can't serialize null");
        if (levels < 0)
            throw new IllegalArgumentException("levels < 0: " + levels);


//        if (obj instanceof List) {
//            List<?> l = (List) obj;
//            res = new ul();
//            for( Object o: l ) {
//                res.add( "<!-- nåda -->");
//                li li = new li().add(generateElement(o, levels - 1) );
//                res.add( decorator.decorate(li, o, levels -1 ) );
//            }
//        } else
        Optional<XHTMLSerializer> ser = serializerFor(obj.getClass());

        Element<?> res;
        if (levels == 0) {
            res = string(obj);
            return res;
        }

        if( ser.isPresent() ) {
//            XHTMLSerializer<Object> x = (XHTMLSerializer<Object>) typeMap.get(obj.getClass());
            res = ser.get().generateElement(obj, levels);
        } else {
            res = new div()
              .add(header(obj))
              .add(propList(obj, levels));
        }
        return decorator == null ? res : decorator.decorate(res, obj, levels);
    }


    public Element<?> header(Object obj) {
        return new h1().add(string(obj));
    }

    public boolean shouldIgnore(PropertyDescriptor pd) {
        if (pd.getReadMethod() == null)
            return true;
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
              .filter(pd -> !"declaringClass".equals(pd.getName()))
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
