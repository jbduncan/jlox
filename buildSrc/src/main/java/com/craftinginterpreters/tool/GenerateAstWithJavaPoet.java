package com.craftinginterpreters.tool;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.writeString;
import static java.util.Objects.requireNonNull;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.SEALED;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.common.base.Ascii;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.processing.Generated;

final class GenerateAstWithJavaPoet {

  private static final String PACKAGE_NAME = "com.craftinginterpreters.lox";
  private static final ClassName NULLABLE = ClassName.get(Nullable.class);
  private static final AnnotationSpec NULLABLE_ANNOTATION =
      AnnotationSpec.builder(ClassName.get(Nullable.class)).build();
  private static final AnnotationSpec GENERATED_ANNOTATION =
      AnnotationSpec.builder(Generated.class)
          .addMember("value", '"' + GenerateAstWithJavaPoet.class.getCanonicalName() + '"')
          .build();

  private static final ClassName EXPR = ClassName.get(PACKAGE_NAME, "Expr");
  private static final ClassName NULLABLE_EXPR = EXPR.annotated(List.of(NULLABLE_ANNOTATION));
  private static final ParameterizedTypeName EXPR_LIST =
      ParameterizedTypeName.get(ClassName.get(List.class), EXPR);
  private static final ClassName NULLABLE_VARIABLE_EXPR = NULLABLE_EXPR.nestedClass("Variable");
  private static final ClassName STMT = ClassName.get(PACKAGE_NAME, "Stmt");
  private static final ClassName NULLABLE_STMT = STMT.annotated(List.of(NULLABLE_ANNOTATION));
  private static final ParameterizedTypeName STMT_LIST =
      ParameterizedTypeName.get(ClassName.get(List.class), STMT);
  private static final ClassName FUNCTION_STMT = STMT.nestedClass("Function");
  private static final ParameterizedTypeName FUNCTION_STMT_LIST =
      ParameterizedTypeName.get(ClassName.get(List.class), FUNCTION_STMT);
  private static final ClassName TOKEN = ClassName.get(PACKAGE_NAME, "Token");
  private static final ParameterizedTypeName TOKEN_LIST =
      ParameterizedTypeName.get(ClassName.get(List.class), TOKEN);
  private static final ClassName OBJECT = ClassName.get(Object.class);
  private static final ClassName NULLABLE_OBJECT = OBJECT.annotated(List.of(NULLABLE_ANNOTATION));

  public static void main(String... args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: GenerateAstWithJavaPoet <output directory>");
      System.exit(64);
    }
    var outputDir = args[0];

    defineAst(
        EXPR,
        outputDir,
        List.of(
            new AstSubType(
                "Assign", //
                new Field(TOKEN, "name"),
                new Field(EXPR, "value")),
            new AstSubType(
                "Binary", //
                new Field(EXPR, "left"),
                new Field(TOKEN, "operator"),
                new Field(EXPR, "right")),
            new AstSubType(
                "Call", //
                new Field(EXPR, "callee"),
                new Field(TOKEN, "paren"),
                new Field(EXPR_LIST, "arguments")),
            new AstSubType(
                "Get", //
                new Field(EXPR, "object"),
                new Field(TOKEN, "name")),
            new AstSubType(
                "Grouping", //
                new Field(EXPR, "expression")),
            new AstSubType(
                "Literal", //
                new Field(NULLABLE_OBJECT, "value")),
            new AstSubType(
                "Logical", //
                new Field(EXPR, "left"),
                new Field(TOKEN, "operator"),
                new Field(EXPR, "right")),
            new AstSubType(
                "Set", //
                new Field(EXPR, "object"),
                new Field(TOKEN, "name"),
                new Field(EXPR, "value")),
            new AstSubType(
                "Super", //
                new Field(TOKEN, "keyword"),
                new Field(TOKEN, "method")),
            new AstSubType(
                "This", //
                new Field(TOKEN, "keyword")),
            new AstSubType(
                "Unary", //
                new Field(TOKEN, "operator"),
                new Field(EXPR, "right")),
            new AstSubType(
                "Variable", //
                new Field(TOKEN, "name"))));
    defineAst(
        STMT,
        outputDir,
        List.of(
            new AstSubType(
                "Block", //
                new Field(STMT_LIST, "statements")),
            new AstSubType(
                "Class", //
                new Field(TOKEN, "name"),
                new Field(NULLABLE_VARIABLE_EXPR, "superclass"),
                new Field(FUNCTION_STMT_LIST, "methods")),
            new AstSubType(
                "Expression", //
                new Field(EXPR, "expression")),
            new AstSubType(
                "Function", //
                new Field(TOKEN, "name"),
                new Field(TOKEN_LIST, "params"),
                new Field(STMT_LIST, "body")),
            new AstSubType(
                "If", //
                new Field(EXPR, "condition"),
                new Field(STMT, "thenBranch"),
                new Field(NULLABLE_STMT, "elseBranch")),
            new AstSubType(
                "Print", //
                new Field(EXPR, "expression")),
            new AstSubType(
                "Return", //
                new Field(TOKEN, "keyword"),
                new Field(NULLABLE_EXPR, "value")),
            new AstSubType(
                "Var", //
                new Field(TOKEN, "name"),
                new Field(NULLABLE_EXPR, "initializer")),
            new AstSubType(
                "While", //
                new Field(EXPR, "condition"),
                new Field(STMT, "body"))));
  }

  private static void defineAst(ClassName astBaseName, String outputDir, List<AstSubType> types)
      throws IOException {

    var typeSpecBuilder =
        TypeSpec.interfaceBuilder(astBaseName)
            .addModifiers(SEALED)
            .addAnnotation(GENERATED_ANNOTATION);

    var visitorInterfaceTypeSpec = defineVisitor(astBaseName, types);
    typeSpecBuilder.addType(visitorInterfaceTypeSpec);

    // The base accept() method.
    var acceptMethodSpecBuilder =
        MethodSpec.methodBuilder("accept")
            .addTypeVariable(TypeVariableName.get("R"))
            .returns(TypeVariableName.get("R"))
            .addParameter(
                ParameterSpec.builder(
                        ParameterizedTypeName.get(
                            ClassName.get(
                                astBaseName.packageName(),
                                astBaseName.simpleName(),
                                visitorInterfaceTypeSpec.name),
                            TypeVariableName.get("R")),
                        "visitor")
                    .build());
    typeSpecBuilder.addMethod(acceptMethodSpecBuilder.addModifiers(PUBLIC, ABSTRACT).build());

    // The AST classes.
    for (var type : types) {
      typeSpecBuilder.addType(
          defineSubType(astBaseName.packageName(), typeSpecBuilder.build(), type.subType, type.fields));
    }

    var astSourceCode =
        JavaFile.builder(astBaseName.packageName(), typeSpecBuilder.build()).build().toString();
    writeString(
        Path.of(outputDir, astBaseName.simpleName() + ".java"), astSourceCode, UTF_8);
  }

  private static TypeSpec defineVisitor(ClassName astBaseName, List<AstSubType> types) {
    var visitorInterface =
        TypeSpec.interfaceBuilder(ClassName.get(astBaseName.packageName(), "Visitor"))
            .addTypeVariable(TypeVariableName.get("R"))
            .addModifiers(PUBLIC, STATIC)
            .addAnnotation(GENERATED_ANNOTATION);

    for (var type : types) {
      visitorInterface.addMethod(
          MethodSpec.methodBuilder("visit" + type.subType + astBaseName.simpleName())
              .addModifiers(PUBLIC, ABSTRACT)
              .returns(TypeVariableName.get("R"))
              .addParameter(
                  ClassName.get(astBaseName.packageName(), astBaseName.simpleName(), type.subType),
                  Ascii.toLowerCase(astBaseName.simpleName()))
              .build());
    }

    return visitorInterface.build();
  }

  private static TypeSpec defineSubType(
      String packageName, TypeSpec outerBaseType, String innerSubType, List<Field> fields) {
    var fieldSpecs =
        fields.stream()
            .map(field -> FieldSpec.builder(field.typeName, field.name, FINAL).build())
            .toList();

    var parameterSpecs =
        fields.stream()
            .map(f -> ParameterSpec.builder(f.typeName, f.name).build())
            .toList();

    var constructorBuilder = MethodSpec.constructorBuilder().addParameters(parameterSpecs);

    // Store parameters in fields.
    for (var field : fields) {
      if (field.isNullable()) {
        constructorBuilder.addStatement("this.$N = $N", field.name, field.name);
      } else {
        constructorBuilder.addStatement(
            "this.$N = $T.requireNonNull($N, \"$N\")",
            field.name,
            Objects.class,
            field.name,
            field.name);
      }
    }

    // Visitor pattern.
    var acceptMethod =
        MethodSpec.methodBuilder("accept")
            .addAnnotation(Override.class)
            .addTypeVariable(TypeVariableName.get("R"))
            .addModifiers(PUBLIC)
            .returns(TypeVariableName.get("R"))
            .addParameter(
                ParameterSpec.builder(
                        ParameterizedTypeName.get(
                            ClassName.get(packageName, outerBaseType.name, "Visitor"),
                            TypeVariableName.get("R")),
                        "visitor")
                    .build())
            .addStatement("return visitor.$N(this)", "visit" + innerSubType + outerBaseType.name)
            .build();

    // TODO: Generate as a record when https://github.com/square/javapoet/pull/840 is resolved.
    return TypeSpec.classBuilder(innerSubType)
        .addModifiers(PUBLIC, STATIC, FINAL)
        .addSuperinterface(ClassName.get(packageName, outerBaseType.name))
        .addFields(fieldSpecs)
        .addMethod(constructorBuilder.build())
        .addMethod(acceptMethod)
        .addAnnotation(GENERATED_ANNOTATION)
        .build();
  }

  private record Field(TypeName typeName, String name) {
    Field {
      requireNonNull(typeName);
      requireNonNull(name);
    }

    boolean isNullable() {
      if (typeName instanceof ClassName className) {
        return className.topLevelClassName().annotations.stream()
            .anyMatch(a -> a.type.equals(NULLABLE));
      }
      return typeName.annotations.stream().anyMatch(a -> a.type.equals(NULLABLE));
    }
  }

  private record AstSubType(String subType, ImmutableList<Field> fields) {
    AstSubType {
      requireNonNull(subType);
    }

    AstSubType(String subType, Field... fields) {
      this(subType, ImmutableList.copyOf(fields));
    }
  }
}
