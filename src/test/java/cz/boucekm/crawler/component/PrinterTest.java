package cz.boucekm.crawler.component;

import cz.boucekm.crawler.model.Node;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test case for <code>Printer</code> component.
 */
public class PrinterTest {

    @Test
    public void oneLevel() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new Printer().print(new Node("root"), new PrintStream(buffer));

        assertThat(buffer.toString(), is("root\n"));
    }

    @Test
    public void twoLevels() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        Node root = new Node("root");
        root.getChildren().add(new Node("a"));
        root.getChildren().add(new Node("b"));

        new Printer().print(root, new PrintStream(buffer));

        assertThat(buffer.toString(), is("root\n" +
                " a\n" +
                " b\n"));
    }
}
