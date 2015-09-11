package no.dv8.xhtml.serializer;

import junit.framework.TestCase;
import lombok.Getter;
import no.dv8.xhtml.generation.support.Element;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class XHTMLSerializerTest extends TestCase {

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

    public void testGenerateElement() throws Exception {
        XHTMLSerializer ser = new XHTMLSerializer();
        Element element = ser.generateElement(new Concert(), 5);

        assertThat(element.toHTML(), containsString("other1"));

        System.out.println( element.toHTML() );
    }

    public void testProps0() throws Exception {
        XHTMLSerializer ser = new XHTMLSerializer();
        Element element = ser.generateElement(new Concert(), 0);
        assertThat(element.toHTML(), equalToIgnoringWhiteSpace("title1"));
        System.out.println(element.toHTML());
    }

    public void testProps1() throws Exception {
        XHTMLSerializer ser = new XHTMLSerializer();
        Element element = ser.generateElement(new Concert(), 1);
        assertThat(element.toHTML(), not(equalTo("title")));
        assertThat(element.toHTML(), containsString("performer"));

        System.out.println(element.toHTML());
        assertThat(element.toHTML(), not(containsString("other1")));
    }

    @Getter
    private static class Performer {
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
}