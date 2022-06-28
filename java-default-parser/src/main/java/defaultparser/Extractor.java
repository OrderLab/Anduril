package defaultparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.visitor.*;

/**
 * Some code that uses JavaSymbolSolver.
 */
public class Extractor {
  private static HashSet<String> set = new HashSet<>();
  private static void print(final String name) {
    if (set.contains(name)) {
      return;
    }
    set.add(name);
    System.out.println(name);
  }

  private static void processNode(Node node, String scope, int level) {
    if (node instanceof CompilationUnit || node instanceof ClassOrInterfaceDeclaration) {
      if (node instanceof ClassOrInterfaceDeclaration) {
        ClassOrInterfaceDeclaration cid = (ClassOrInterfaceDeclaration) node;
        if (level > 1) {
          scope = scope + '$';
        }
        scope = scope + cid.getName().asString();
      }
      for (Node child : node.getChildNodes()){
        processNode(child,scope,level + 1);
      }
    } else if (node instanceof MethodDeclaration) {
      MethodDeclaration me = (MethodDeclaration) node;
      if (me.getModifiers().toString().contains("default")) {
        print(scope + ".class");
      }
    }
  }

  private static String getScope(String path) {
    int i = path.length() - 1;
    i -= ".java".length();
    while (i >= 0 && path.charAt(i) != '/') i--;
    return path.substring(0, i + 1);
  }

  private static ArrayList<File> errors = new ArrayList<>();

  public static void listf(String path, File dir) throws FileNotFoundException {
    for (File file : dir.listFiles()) {
      if (file.isFile()) {
        if (file.getName().endsWith(".java")) {
          try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            processNode(cu, path + "/", 0);
          } catch (com.github.javaparser.ParseProblemException e) {
            errors.add(file);
          }
        }
      } else if (file.isDirectory()) {
        listf(path + "/" + file.getName(), file);
      }
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    File path = new File(args[0]);
    listf(".", path);
    for (File file : errors) {
      System.out.println("    error in " + file.getAbsolutePath());
    }
    //File sourceFile = new File(path);
    //CompilationUnit cu = StaticJavaParser.parse(sourceFile);
    //String scope = getScope(path);
    //processNode(cu, scope);

    //VoidVisitor<Void> methodNameVisitor=new MethodNamePrinter();
    //methodNameVisitor.visit(cu,null);
  }
}
