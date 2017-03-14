package cz.boucekm.crawler.component;

import cz.boucekm.crawler.model.Node;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Unit test for <code>Crawler</code> component.
 */
public class CrawlerTest {

    /**
     * Test for Crawler component.<br>
     *
     * <b>Constraint:</b> In order to run successfully this test, run first local HTTP server in <code>html</code>
     * directory (for example: <code>python -m SimpleHTTPServer 8080</code>).<br>
     *
     * <b>Possible solution:</b> Start embedded Tomcat in <code>before()</code> method, which points to <code>html</code>
     * folder (or reads files from <code>/src/test/resources</code> folder via classpath, etc.). Also create much better
     * test coverage for production use (more recursive links, multiple directories, external links without protocol, etc.)
     */
    @Test
    public void processLocalFile() throws Exception {
        Node node = new Crawler().processURL(new URL("http://localhost:8080/"));

        assertThat(node, notNullValue());
        assertThat(node.getName(), is("/index.html"));
        assertThat(node.getChildren().size(), is(1));

        Node secondNode = node.getChildren().get(0);
        assertThat(secondNode, notNullValue());
        assertThat(secondNode.getName(), is("/second.html"));
        assertThat(secondNode.getChildren().size(), is(2));

        Node thirdNode = secondNode.getChildren().get(0);
        assertThat(thirdNode.getName(), is("/third.html"));
        assertThat(thirdNode.getChildren().size(), is(2));

        Node styleNode = thirdNode.getChildren().get(0);
        assertThat(styleNode, notNullValue());
        assertThat(styleNode.getName(), is("/style.css"));
        assertThat(styleNode.getChildren().isEmpty(), is(true));

        Node imageNode = thirdNode.getChildren().get(1);
        assertThat(imageNode, notNullValue());
        assertThat(imageNode.getName(), is("/image.png"));
        assertThat(imageNode.getChildren().isEmpty(), is(true));

        Node scriptNode = secondNode.getChildren().get(1);
        assertThat(scriptNode, notNullValue());
        assertThat(scriptNode.getName(), is("/script.js"));
        assertThat(scriptNode.getChildren().isEmpty(), is(true));
    }
}
