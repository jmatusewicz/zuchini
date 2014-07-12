package net.jhorstmann.gherkin;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import net.jhorstmann.gherkin.antlr.GherkinListener;
import net.jhorstmann.gherkin.antlr.GherkinParser;
import net.jhorstmann.gherkin.model.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;

class FeatureWalker implements GherkinListener {
    private final String uri;
    private Feature feature;
    private StepContainer stepContainer;
    private Commented commentContainer;
    private Tagged tagContainer;
    private Step step;
    private List<Row> rows;
    private Row row;

    public FeatureWalker(String uri) {
        this.uri = uri;
    }

    private static String trimKeyword(Token token) {
        return token.getText().replaceAll("^[\t ]+|[\t :]+$", "");
    }

    private static String trimComment(Token token) {
        return token.getText().replaceFirst("^[\t #]+", "");
    }

    private static String trimCell(Token token) {
        return token.getText().replaceAll("^[\t ]+|[\t |]+$", "");
    }

    private static String trimTag(Token token) {
        return token.getText().replaceFirst("^@", "");
    }

    public Feature getFeature() {
        return feature;
    }

    @Override
    public void enterFeature(@NotNull GherkinParser.FeatureContext ctx) {
        Token keyword = ctx.FEATURE().getSymbol();
        String description = Joiner.on("\n").join(from(ctx.content()).transform(RuleToString.INSTANCE));
        commentContainer = feature = new Feature(uri, keyword.getLine(), trimKeyword(keyword), description);
    }

    @Override
    public void exitFeature(@NotNull GherkinParser.FeatureContext ctx) {
    }

    @Override
    public void enterBackground(@NotNull GherkinParser.BackgroundContext ctx) {
        Token keyword = ctx.BACKGROUND().getSymbol();
        commentContainer = stepContainer = new Background(feature, keyword.getLine(), trimKeyword(keyword),
                ctx.content().getText());
    }

    @Override
    public void exitBackground(@NotNull GherkinParser.BackgroundContext ctx) {
        feature.getBackground().add((Background) stepContainer);
        stepContainer = null;
        commentContainer = null;
        tagContainer = null;
    }

    @Override
    public void enterStep(@NotNull GherkinParser.StepContext ctx) {
        Token keyword = ctx.STEP().getSymbol();
        step = new Step(stepContainer, keyword.getLine(), trimKeyword(keyword), ctx.content().getText());
        commentContainer = step;
        tagContainer = step;

    }

    @Override
    public void exitStep(@NotNull GherkinParser.StepContext ctx) {
        stepContainer.getSteps().add(step);
        commentContainer = step = null;
    }

    @Override
    public void enterDoc(@NotNull GherkinParser.DocContext ctx) {
        String doc = ctx.DOC_STRING().getText();
        step.getDocs().add(doc);
    }

    @Override
    public void exitDoc(@NotNull GherkinParser.DocContext ctx) {
    }

    @Override
    public void enterComments(@NotNull GherkinParser.CommentsContext ctx) {
        from(ctx.COMMENT()).transform(CommentToString.INSTANCE).copyInto(commentContainer.getComments());
    }

    @Override
    public void exitComments(@NotNull GherkinParser.CommentsContext ctx) {
    }

    @Override
    public void enterTags(@NotNull GherkinParser.TagsContext ctx) {
        from(ctx.TAG()).transform(TagToString.INSTANCE).copyInto(tagContainer.getTags());
    }

    @Override
    public void exitTags(@NotNull GherkinParser.TagsContext ctx) {
    }

    @Override
    public void enterAbstractScenario(@NotNull GherkinParser.AbstractScenarioContext ctx) {
    }

    @Override
    public void exitAbstractScenario(@NotNull GherkinParser.AbstractScenarioContext ctx) {
    }

    @Override
    public void enterScenario(@NotNull GherkinParser.ScenarioContext ctx) {
        Token keyword = ctx.SCENARIO().getSymbol();
        stepContainer = new Scenario(feature, keyword.getLine(), trimKeyword(keyword),
                ctx.content().getText());
        commentContainer = stepContainer;
        tagContainer = stepContainer;
    }

    @Override
    public void exitScenario(@NotNull GherkinParser.ScenarioContext ctx) {
        feature.getScenarios().add(stepContainer);
        stepContainer = null;
        commentContainer = null;
        tagContainer = null;
    }

    @Override
    public void enterOutline(@NotNull GherkinParser.OutlineContext ctx) {
        Token keyword = ctx.SCENARIO_OUTLINE().getSymbol();
        stepContainer = new Outline(feature, keyword.getLine(), trimKeyword(keyword), ctx.content().getText());
        commentContainer = stepContainer;
        tagContainer = stepContainer;
    }

    @Override
    public void exitOutline(@NotNull GherkinParser.OutlineContext ctx) {
        feature.getScenarios().add(stepContainer);
        stepContainer = null;
        commentContainer = null;
        tagContainer = null;
    }

    @Override
    public void enterTable(@NotNull GherkinParser.TableContext ctx) {
        rows = new ArrayList<>();
    }

    @Override
    public void exitTable(@NotNull GherkinParser.TableContext ctx) {
        step.getRows().addAll(rows);
        rows = null;
    }

    @Override
    public void enterRow(@NotNull GherkinParser.RowContext ctx) {
        row = new Row(feature, ctx.TABLE_START().getSymbol().getLine());
        commentContainer = row;
        tagContainer = row;
    }

    @Override
    public void exitRow(@NotNull GherkinParser.RowContext ctx) {
        rows.add(row);
        row = null;
        commentContainer = null;
        tagContainer = null;
    }

    @Override
    public void enterCell(@NotNull GherkinParser.CellContext ctx) {
    }

    @Override
    public void exitCell(@NotNull GherkinParser.CellContext ctx) {
        row.getCells().add(trimCell(ctx.TABLE_CELL().getSymbol()));
    }

    @Override
    public void enterContent(@NotNull GherkinParser.ContentContext ctx) {
    }

    @Override
    public void exitContent(@NotNull GherkinParser.ContentContext ctx) {
    }

    @Override
    public void enterExamples(@NotNull GherkinParser.ExamplesContext ctx) {
        rows = new ArrayList<>();
    }

    @Override
    public void exitExamples(@NotNull GherkinParser.ExamplesContext ctx) {
        ((Outline) stepContainer).getExamples().addAll(rows);
        rows = null;
    }

    @Override
    public void visitTerminal(@NotNull TerminalNode node) {
    }

    @Override
    public void visitErrorNode(@NotNull ErrorNode node) {
    }

    @Override
    public void enterEveryRule(@NotNull ParserRuleContext ctx) {
    }

    @Override
    public void exitEveryRule(@NotNull ParserRuleContext ctx) {
    }

    static enum TagToString implements Function<TerminalNode, String> {
        INSTANCE;

        @Override
        public String apply(org.antlr.v4.runtime.tree.TerminalNode input) {
            Token token = input.getSymbol();
            return trimTag(token);
        }


    }

    static enum CommentToString implements Function<TerminalNode, String> {
        INSTANCE;

        @Override
        public String apply(org.antlr.v4.runtime.tree.TerminalNode input) {
            Token token = input.getSymbol();
            return trimComment(token);
        }

    }

    static enum RuleToString implements Function<RuleNode, String> {
        INSTANCE;

        @Override
        public String apply(org.antlr.v4.runtime.tree.RuleNode input) {
            return input.getText();
        }


    }
}