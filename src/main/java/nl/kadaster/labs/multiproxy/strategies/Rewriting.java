package nl.kadaster.labs.multiproxy.strategies;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.*;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Rewriting extends Strategy {
    @Override
    public String execute(String path, String query, String user) throws IOException {
        var queryObj = QueryFactory.create(query);
        var visitor = new Visitor();
        var pattern = (ElementGroup) queryObj.getQueryPattern(); // TODO unchecked
        pattern.visit(visitor);

        var builder = new SelectBuilder();
        for (var var : visitor.getVars()) {
            builder.addFilter(TEMPLATE_FILTER
                    .replace(":node", var.getVarName())
                    .replace(":user", user)
            );
        }

        for (var filter : builder.getHandlerBlock().getWhereHandler().getClause().getElements()) {
            pattern.addElement(filter);
        }

        return this.execute(path, queryObj.toString());
    }

    private static class Visitor implements ElementVisitor, ExprVisitor {
        private final Set<Var> vars = new HashSet<>();

        public Set<Var> getVars() {
            return this.vars;
        }

        public void visit(ElementTriplesBlock elementTriplesBlock) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementPathBlock elementPathBlock) {
            for (var pathBlock : elementPathBlock.getPattern()) {
                this.maybeRemember(pathBlock.getSubject());
                this.maybeRemember(pathBlock.getPredicate());
                this.maybeRemember(pathBlock.getObject());
            }
        }

        public void visit(ElementFilter elementFilter) {
            elementFilter.getExpr().visit(this);
        }

        public void visit(ElementAssign elementAssign) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementBind elementBind) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementUnfold elementUnfold) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementData elementData) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementUnion elementUnion) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementOptional elementOptional) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementLateral elementLateral) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementGroup elementGroup) {
            for (Element element : elementGroup.getElements()) {
                element.visit(this);
            }
        }

        public void visit(ElementDataset elementDataset) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementNamedGraph elementNamedGraph) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementExists elementExists) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementNotExists elementNotExists) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementMinus elementMinus) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementService elementService) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementSubQuery elementSubQuery) {
            throw new UnsupportedOperationException(); // TODO
        }

        private void maybeRemember(Node node) {
            if (node instanceof Var) {
                this.vars.add((Var) node);
            }
        }

        public void visit(ExprFunction0 exprFunction0) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprFunction1 exprFunction1) {
            exprFunction1.getArg().visit(this);
        }

        public void visit(ExprFunction2 exprFunction2) {
            exprFunction2.getArg1().visit(this);
            exprFunction2.getArg2().visit(this);
        }

        public void visit(ExprFunction3 exprFunction3) {
            exprFunction3.getArg1().visit(this);
            exprFunction3.getArg2().visit(this);
            exprFunction3.getArg3().visit(this);
        }

        public void visit(ExprFunctionN exprFunctionN) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprFunctionOp exprFunctionOp) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprTripleTerm exprTripleTerm) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(NodeValue nodeValue) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprVar exprVar) {
            this.vars.add(exprVar.asVar());
        }

        public void visit(ExprAggregator exprAggregator) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprNone exprNone) {
            throw new UnsupportedOperationException(); // TODO
        }
    }
}
