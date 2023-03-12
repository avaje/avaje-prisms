package io.avaje.prism.internal;

import java.io.PrintWriter;

import javax.lang.model.type.DeclaredType;

public class GenerateContext {

  private final String indent;
  private final PrintWriter out;
  private final String outerName;
  private final String name;
  private final DeclaredType typeMirror;
  private final String access;
  private String annName;

  public GenerateContext(
      String indent,
      PrintWriter out,
      String outerName,
      String name,
      DeclaredType typeMirror,
      String access) {
    this.indent = indent;
    this.out = out;
    this.outerName = outerName;
    this.name = name;
    this.typeMirror = typeMirror;
    this.access = access;
  }

  public String indent() {
    return indent;
  }

  public PrintWriter out() {
    return out;
  }

  public String outerName() {
    return outerName;
  }

  public String name() {
    return name;
  }

  public String annName() {
    return annName;
  }

  public DeclaredType typeMirror() {
    return typeMirror;
  }

  public String access() {
    return access;
  }

  public void setAnnName(String annName) {
    this.annName = annName;
  }
}
