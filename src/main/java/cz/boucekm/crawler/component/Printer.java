package cz.boucekm.crawler.component;

import cz.boucekm.crawler.model.Node;
import org.springframework.stereotype.Component;

import java.io.PrintStream;

import static org.apache.commons.lang3.StringUtils.leftPad;

/**
 * Printer component, which prints root node to selected output stream.
 */
@Component
public class Printer {

    public void print(Node node, PrintStream out) {
        print(node, out, 0);
    }

    /**
     * <p>
     *     <b>Limitation</b> - it prints in one fixed, simple format<br>
     *     <b>Discussion</b> - It should print in nicer structure and probably even sort the data first alphabetically.
     *     Implementation could also print name of the page (taken from <code>title</code> tag) and then full link
     *     to the page (or resource).
     * </p>
     */
    private void print(Node node, PrintStream out, int level) {
        String nodeName = node.getName();
        String message = leftPad(nodeName, nodeName.length() + level);

        out.println(message);

        int nextLevel = level + 1;
        for (Node child : node.getChildren()) {
            print(child, out, nextLevel);
        }
    }
}
