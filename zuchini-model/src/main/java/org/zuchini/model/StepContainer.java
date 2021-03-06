package org.zuchini.model;

import java.util.ArrayList;
import java.util.List;

public abstract class StepContainer implements Named, Commented, Tagged, LocationAware {
    private final int lineNumber;
    private final Feature feature;
    private final String keyword;
    private final String name;
    private final List<String> tags = new ArrayList<>();
    private final List<String> comments = new ArrayList<>();
    private final List<Step> steps = new ArrayList<>();

    public StepContainer(Feature feature, int lineNumber, String keyword, String name) {
        this.feature = feature;
        this.lineNumber = lineNumber;
        this.keyword = keyword;
        this.name = name;
    }

    @Override
    public String getUri() {
        return feature.getUri();
    }

    public Feature getFeature() {
        return feature;
    }

    public List<Background> getBackground() {
        return feature.getBackground();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getKeyword() {
        return keyword;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getComments() {
        return comments;
    }

    public String getName() {
        return name;
    }

    public List<Step> getSteps() {
        return steps;
    }


}
