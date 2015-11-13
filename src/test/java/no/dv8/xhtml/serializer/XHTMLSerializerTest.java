package no.dv8.xhtml.serializer;

import junit.framework.TestCase;
import lombok.Getter;
import no.dv8.xhtml.generation.elements.a;
import no.dv8.xhtml.generation.elements.span;
import no.dv8.xhtml.generation.support.Element;
import no.dv8.xhtml.generation.support.Str;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

//@Ignore
public class XHTMLSerializerTest extends TestCase {

    public static enum TestEnum {
        A, B, C
    }

    @Getter
    private static class Performer {
        Integer id = 3;
        TestEnum testEnu = TestEnum.A;
        String name;
        String other;
        public Performer(String name, String other) {
            this.name = name;
            this.other = other;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Getter
    public static class Concert {
        public List<Performer> performers = asList( new Performer( "performer1", "other1"), new Performer( "performer2", "other2"));
        public Performer performer = new Performer( "performer3", "other3");
        public String title = "title1";
        public Date date = new Date();


        @Override
        public String toString() {
            return title;
        }
    }

    public void testInteger() {
        XHTMLSerialize ser = new XHTMLSerialize();
        Element element = ser.generateElement(new Performer("per1", "per2"), 5);
        fail( element.toHTML());
    }

    public void testGenerateElement() throws Exception {
        XHTMLSerialize ser = new XHTMLSerialize();

        assertTrue( "should contain list", ser.typeMap.containsKey(List.class) );
        assertTrue( "should contain list", ser.typeMap.containsKey(ArrayList.class) );

        Class clz = asList(new Performer("performer1", "other1"), new Performer("performer2", "other2")).getClass();
        assertTrue( "can serialize??", ser.canSerialize(List.class, clz));

        assertTrue( "Should be ListSerializer", ser.serializerFor(List.class).get() instanceof ListSerializer );

//        assertTrue( "should contain type of list of performers", ser.typeMap.containsKey( clz ) );

        Element element = ser.generateElement(new Concert(), 5);

        System.out.println( element.toHTML() );
        assertThat(element.toHTML(), containsString("other1"));

    }

    public void testXMLTransient() throws Exception {
        XHTMLSerializer ser = new XHTMLSerialize();
        Element element = ser.generateElement(new Concert(), 5);

        System.out.println( element.toHTML() );

        assertThat(element.toHTML(), containsString("other1"));
    }

    public void testProps0() throws Exception {
        XHTMLSerializer ser = new XHTMLSerialize();
        Element element = ser.generateElement(new Concert(), 0);
        assertThat(element.toHTML(), equalToIgnoringWhiteSpace("title1"));
        System.out.println(element.toHTML());
    }

    public void testProps1() throws Exception {
        XHTMLSerializer ser = new XHTMLSerialize();
        Element element = ser.generateElement(new Concert(), 1);
        assertThat(element.toHTML(), not(equalTo("title")));
        assertThat(element.toHTML(), containsString("performer"));

        System.out.println(element.toHTML());
        assertThat(element.toHTML(), not(containsString("other1")));
    }


    @Test
    public void testRegistration() {
        XHTMLSerialize ser = new XHTMLSerialize();
        ser.typeMap.put(Concert.class, new XHTMLSerializer<Concert>() {
            @Override
            public Element<?> generateElement(Concert transition, int i) {
                return new Str("hei");
            }
        });
        Element element1 = ser.generateElement(new Concert(), 1);
        assertThat(element1.toHTML(), equalTo("hei\r\n"));
    }

    @Test
      public void testListReg() {
        XHTMLSerialize ser = new XHTMLSerialize();
        ser.typeMap.put(Concert.class, new XHTMLSerializer<Concert>() {
            @Override
            public Element<?
              > generateElement(Concert transition, int i) {
                return new a("hei");
            }
        });

        Element element = ser.generateElement(asList( new Concert()), 2);

        System.out.println(element.toHTML());
        assertThat( element.toHTML(), containsString( "hei"));
    }



    public static class ListItem {
        final String name;
        public ListItem( String n ) {
            this.name = n;
        }
        public String toString() {
            return name;
        }
    }

    public void testListLevel() throws Exception {
        XHTMLSerialize ser = new XHTMLSerialize();
        ser.decorator = new ElementDecorator() {
            @Override
            public <T extends Element> Element<T> decorate(Element<T> t, Object obj, int level) {
                return t.addClz( "level"+level);
            }
        };

//        ser.decorator = (Element t,Object o,int ll) -> t.addClz( "level"+ll);

        Element element = ser.generateElement(asList( new ListItem( "item1"),  new ListItem( "item2")), 1);
        assertThat(element.toHTML(), containsString("item1"));

        System.out.println(element.toHTML());
    }

}