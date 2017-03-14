package cz.boucekm.crawler.component;

import cz.boucekm.crawler.model.Node;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Crawler component, which scans base URL and goes from there. It starts on <code>index.html</code> file.
 */
@Component
public class Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Crawler.class);

    public Node processURL(URL baseURL) {
        List<URL> visitedLinks = new ArrayList<>();
        return processURL(baseURL, "/index.html", visitedLinks);
    }

    private Node processURL(URL baseURL, String path, List<URL> visitedLinks) {
        LOGGER.info("Processing path: {}", path);

        URL targetURL = parseURL(baseURL, path);
        if (targetURL == null) {
            LOGGER.warn("Unable to parse ULR: {}", path);
            return null;
        }

        if (!shouldFollowLink(baseURL, targetURL, visitedLinks)) {
            LOGGER.info("Ignoring URL: {}", targetURL);
            return null;
        }
        visitedLinks.add(targetURL);

        ResponseEntity<byte[]> entity = getResponseEntity(targetURL);
        if (entity == null) {
            return null;
        }

        Node currentNode = new Node(targetURL.getPath());

        // follow only HTML
        if (MediaType.TEXT_HTML.isCompatibleWith(entity.getHeaders().getContentType())) {
            processHTML(baseURL, visitedLinks, entity, currentNode);
        }

        return currentNode;
    }

    /**
     * <p>Parses given URL. If the URL is not absolute (does not starts with protocol), it tries to append base URL.</p>
     *
     * <p>
     *     <b>Limitation</b> - it expects either absolute URL with protocol (not only e.g. <code>www.google.com</code>) or
     *     the URL is considered to be relative.<br>
     *     <b>Discussion</b> - Ideally this should be resolved in much clever and detailed way (let's say by using some
     *     3rd party library). Basically nobody is typing <code>https://www.google.com</code> and also cases like
     *     sub-domains needs to be carefully checked (like <code>api.google.com/v1/whatever</code>). Also the URL should
     *     be normalized and absolute, without any relative path, possibly even stripped from any fragments and maybe even
     *     from parameters (?).
     * </p>
     *
     * @param baseURL base URL of whole website
     * @param url target ULR (either absolute with protocol, or relative)
     * @return parsed URL or null, if malformed
     */
    private URL parseURL(URL baseURL, String url) {
        try {
            return new URL(url);
        }
        catch (MalformedURLException e) {
            try {
                return new URL(baseURL, url);
            }
            catch (MalformedURLException e1) {
                return null;
            }
        }
    }

    /**
     * <p>Scans HTML for known limited set of HTML tags, which might contain references.</p>
     *
     * <p>
     *     <b>Limitation</b> - it scans only anchors, links, scripts and images<br>
     *     <b>Discussion</b> - We should scan every possible HTML element, which contains any kind of link. CSS can also
     *     import additional resources via <code>@import</code> directive.
     * </p>
     */
    private void processHTML(URL baseURL, List<URL> visitedLinks, ResponseEntity<byte[]> responseEntity, Node currentNode) {

        Document document = parseDocument(responseEntity);
        if (document == null) {
            return;
        }

        scanTag("a", "href", currentNode, document, baseURL, visitedLinks);
        scanTag("link", "href", currentNode, document, baseURL, visitedLinks);
        scanTag("script", "src", currentNode, document, baseURL, visitedLinks);
        scanTag("img", "src", currentNode, document, baseURL, visitedLinks);
    }

    /**
     * <p>Parses response to HTML document.</p>
     *
     * <p>
     *     <b>Limitation</b> - it expects valid HTML response<br>
     *     <b>Discussion</b> - some HTML doesn't have to be valid and it should be also sanitized first. Even though this
     *     method should be called only when HTML is sent back, it might be possible that server sends wrong MIME type
     *     (for example: it sends "text/html" for PNG image).
     * </p>
     */
    private Document parseDocument(ResponseEntity<byte[]> responseEntity) {

        byte[] body = responseEntity.getBody();
        if (body == null) {
            return null;
        }

        String html = new String(body);
        if (StringUtils.isEmpty(html)) {
            return null;
        }

        return Jsoup.parse(html);
    }

    private void scanTag(String tagName, String attributeName, Node currentNode, Document document, URL baseURL, List<URL> visitedLinks) {
        for (Element element: document.select(tagName)) {
            String path = element.attr(attributeName);

            Node node = processURL(baseURL, path, visitedLinks);
            if (node != null) {
                currentNode.getChildren().add(node);
            }
        }
    }

    private ResponseEntity<byte[]> getResponseEntity(URL url) {
        try {
            return new RestTemplate().getForEntity(url.toURI(), byte[].class);
        }
        catch (RestClientException | URISyntaxException e) {
            LOGGER.error("Unable to load URL: {}, reason: {}", url, e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Checks, if crawler should follow the link.
     *
     * <p>
     *     <b>Limitation</b> - it requires known host on base and target URL. Also it requires that visited links are generic
     *     enough to capture every possible relative back-link.<br>
     *     <b>Discussion</b> - it has been already discussed how links should be sanitized, so I will focus here on
     *     "known host name" problem only. The host can be potentially unknown, if the URL points to localhost via file
     *     protocol. This should be sanitized.
     * </p>
     */
    private boolean shouldFollowLink(URL baseURL, URL targetURL, List<URL> visitedLinks) {
        String targetHostName = targetURL.getHost();
        String originHostName = baseURL.getHost();

        return originHostName.equals(targetHostName) && !visitedLinks.contains(targetURL);
    }
}
