package net.jhorstmann.gherkin.model;

import java.util.ArrayList;
import java.util.List;

public class Row implements Commented, Tagged, LocationAware {
    private final Feature feature;
    private final int lineNumber;
    private final List<String> comments = new ArrayList<>();
    private final List<String> tags = new ArrayList<>();
    private final List<String> cells = new ArrayList<>();

    public Row(Feature feature, int lineNumber) {
        this.feature = feature;
        this.lineNumber = lineNumber;
    }

    @Override
    public String getUri() {
        return feature.getUri();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getComments() {
        return comments;
    }

    public List<String> getCells() {
        return cells;
    }
}