package nl.kadaster.labs.multiproxy.strategies;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.*;
import org.springframework.http.ResponseEntity;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Rewriting extends Strategy {
    @Override
    public ResponseEntity<String> execute(String path, String query, String user) throws IOException {
        var rewriteStart = System.currentTimeMillis();
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
        var executionStart = System.currentTimeMillis();

        var response = this.execute(path, queryObj.toString());
        var completion = System.currentTimeMillis();

        return ResponseEntity.ok()
                .header(HEADER_SERVER_TIMING, "rewriting;dur=%d, execution;dur=%d".formatted(
                        executionStart - rewriteStart,
                        completion - executionStart
                ))
                .body(response);
    }

    private static class Visitor implements ElementVisitor, ExprVisitor {
        private final Set<Var> vars = new HashSet<>();

        public Set<Var> getVars() {
            return this.vars;
        }

        public void visit(ElementTriplesBlock elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementPathBlock elem) {
            for (var pathBlock : elem.getPattern()) {
                this.maybeRemember(pathBlock.getSubject());
                this.maybeRemember(pathBlock.getPredicate());
                this.maybeRemember(pathBlock.getObject());
            }
        }

        public void visit(ElementFilter elem) {
            elem.getExpr().visit(this);
        }

        public void visit(ElementAssign elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementBind elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementUnfold elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementData elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementUnion elem) {
            var els = elem.getElements();
            for (Element el : els) {
                el.visit(this);
            }
        }

        public void visit(ElementOptional elem) {
            elem.getOptionalElement().visit(this);
        }

        public void visit(ElementLateral elem) {
            elem.getLateralElement().visit(this);
        }

        public void visit(ElementGroup elem) {
            for (Element element : elem.getElements()) {
                element.visit(this);
            }
        }

        public void visit(ElementDataset elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementNamedGraph elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementExists elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementNotExists elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementMinus elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementService elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ElementSubQuery elem) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprFunction0 expr) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprFunction1 expr) {
            expr.getArg().visit(this);
        }

        public void visit(ExprFunction2 expr) {
            expr.getArg1().visit(this);
            expr.getArg2().visit(this);
        }

        public void visit(ExprFunction3 expr) {
            expr.getArg1().visit(this);
            expr.getArg2().visit(this);
            expr.getArg3().visit(this);
        }

        public void visit(ExprFunctionN expr) {
            var args = expr.getArgs();
            for (Expr arg : args) {
                arg.visit(this);
            }
        }

        public void visit(ExprFunctionOp expr) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprTripleTerm expr) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(NodeValue node) {
            return;
        }

        public void visit(ExprVar expr) {
            this.vars.add(expr.asVar());
        }

        public void visit(ExprAggregator expr) {
            throw new UnsupportedOperationException(); // TODO
        }

        public void visit(ExprNone expr) {
            return;
        }

        private void maybeRemember(Node node) {
            if (node instanceof Var) {
                this.vars.add((Var) node);
            }
        }
    }
}
