package cz.boucekm.crawler;

import cz.boucekm.crawler.component.Crawler;
import cz.boucekm.crawler.component.Printer;
import cz.boucekm.crawler.model.Node;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Main application entry point. See further JavaDocs for discussion about limitations of this solution.
 */
@SpringBootApplication
public class Application implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static final String DEFAULT_URL = "http://localhost:8080";

    @Autowired
    private Crawler crawler;

    @Autowired
    private Printer printer;

    /**
     * <p>Runs crawler and then prints the result to system output (console). If arguments contains "h" or "help" switch,
     * it prints usage and stops execution. It takes optional argument "url" to specify host name (with protocol).</p>
     *
     * <p>
     *     <b>Limitation</b> - target server has <code>index.html</code> file at root and it has all files in same directory.<br>
     *     <b>Discussion</b> - name of first document is hardcoded in order to simplify duplicity check. Ideally we should
     *     check for redirect from root, then take the real name of document and use that one as a key in duplicity check
     *     collection.<br>
     *     Also, duplicity check is so simple, than if any document would be in different folder and referenced back in
     *     different way (let's say "index.html" -gt; "base/second.htm", which points back to "../index.html"), this would
     *     case extra scan. The solution would be to resolve all links to absolute URL without any relative path navigation
     *     and then use that as a key in duplicity check collection.
     * </p>
     *
     * @param args command line arguments
     */
    @Override
    public void run(ApplicationArguments args) {

        if (args.containsOption("h") || args.containsOption("help")) {
            LOGGER.info("Usage: java -jar crawler.jar [--url <URL>]");
            LOGGER.info("\t--url\tURL to be scanned (optional)");
            return;
        }

        URL url = getURLFromArguments(args);
        if (url == null) {
            return;
        }

        Node rootNode = crawler.processURL(url);
        printer.print(rootNode, System.out);
    }

    private URL getURLFromArguments(ApplicationArguments args) {
        String url = null;

        List<String> listOfURL = args.getOptionValues("url");
        if (listOfURL != null && !listOfURL.isEmpty()) {
            url = listOfURL.get(0);
        }

        try {
            return new URL(StringUtils.defaultIfBlank(url, DEFAULT_URL));
        }
        catch (MalformedURLException e) {
            LOGGER.error("Unable to parse URL: {}, reason: {}", url, e.getLocalizedMessage());
            return null;
        }
    }

    public static void main(String args[]) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
