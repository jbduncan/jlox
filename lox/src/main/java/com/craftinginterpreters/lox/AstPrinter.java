package com.craftinginterpreters.lox;

import java.util.StringJoiner;

final class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitGetExpr(Expr.Get expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    return (expr.value == null) ? "nil" : expr.value.toString();
  }

  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitSetExpr(Expr.Set expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitSuperExpr(Expr.Super expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitThisExpr(Expr.This expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme(), expr.right);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    throw new UnsupportedOperationException("Not yet implemented.");
  }

  private String parenthesize(String name, Expr... exprs) {
    var joiner = new StringJoiner("", "(" + name, ")");
    for (Expr expr : exprs) {
      joiner.add(" ").add(expr.accept(this));
    }
    return joiner.toString();
  }
}
