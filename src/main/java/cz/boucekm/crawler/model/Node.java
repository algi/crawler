package cz.boucekm.crawler.model;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private String name;
    private List<Node> children = new ArrayList<>();

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Node> getChildren() {
        return children;
    }
}
