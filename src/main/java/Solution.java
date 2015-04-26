import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.TypeParameter;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.ArrayCreationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.*;


public class Solution  {
    private Map<String, List<String>> result = new HashMap<String, List<String>>();
    public final File start;
    private Set<String> curEdges = new HashSet<String>();
    private Set<String> visited = new HashSet<String>();
    private List<String> answer = new ArrayList<String>();
    private Map<String, File> allFiles = new HashMap<String, File>();
    private Map<String, String> packageFile = new HashMap<String, String>();
    public Solution(File s) {
        start = s;
    }
    public Map<File, String> getResult(String targetFile) {

        createGraph(targetFile);
        visited.clear();
        dfs(targetFile);

        Map<File, String> files = new HashMap<File, String>();
        for (String name : answer) {
            files.put(allFiles.get(name),packageFile.get(name));
        }
        return files;
    }

    private void dfs(String u) {
        visited.add(u);
        List<String> edges = result.get(u);
        if (edges != null) {
            for (String to : result.get(u)) {
                if (!visited.contains(to)) {
                    dfs(to);
                }
            }
        }

        answer.add(u);


    }

    public void createGraph (String targetFile) {
        try {
            visited.add(targetFile);
            File f = new File(getPathToTarget(start, targetFile));

            CompilationUnit cu = JavaParser.parse(f);
            packageFile.put(targetFile, cu.getPackage().getName().toString());
            Map<String, File> filesInPackage = getFilesInPackage(f.getParentFile().getName());
            allFiles.putAll(filesInPackage);
            List<ImportDeclaration> imports = cu.getImports();
            CurrentData cur = new CurrentData(targetFile, filesInPackage, curEdges);
            if (imports != null) {
                for (ImportDeclaration importDeclaration : imports) {
                    Map<String, File> files = getFilesInPackage(importDeclaration.getName().getName());
                    filesInPackage.putAll(files);
                    for (String name : files.keySet()){
                        new MethodVisitor().checkType(name.substring(0, name.length() -5), cur);

                    }

                }
            }
            allFiles.putAll(filesInPackage);

            new MethodVisitor().visit(cu, cur);


        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            Type type = n.getType();
            List<Parameter> args = n.getParameters();
            if (args != null) {
                for (Parameter p : args) {
                    List<Type> ar = new ArrayList<Type>();
                    ar.add(p.getType());
                    dfsType(ar, arg);

                }
            }
            List<Type> ar = new ArrayList<Type>();
            ar.add(type);
            dfsType(ar, arg);
            List<NameExpr> throwsName = n.getThrows();
            if (throwsName != null) {
                for (NameExpr name : throwsName) {
                    checkType(name.getName(), arg);
                }
            }
            super.visit(n, arg);
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Object arg) {
            List<ClassOrInterfaceType> extend = n.getExtends();
            if (extend != null) {
                for (ClassOrInterfaceType t : extend) {
                    List<Type> res = t.getTypeArgs();
                    if (res == null){
                        res = addOne(t);
                    }
                    dfsType(res, arg);
                }
            }

            List<ClassOrInterfaceType> implement = n.getImplements();
            if (implement != null) {
                for (ClassOrInterfaceType t : implement) {
                    List<Type> res = t.getTypeArgs();
                    if (res == null){
                        res = addOne(t);
                    }
                    dfsType(res, arg);
                }
            }
            super.visit(n, arg);
        }

       @Override
       public void visit (VariableDeclarationExpr n, Object arg) {
           dfsType(addOne(n.getType()), arg);
           super.visit(n, arg);
       }

        private List<Type> addOne(Type p) {
            List<Type> ar = new ArrayList<Type>();
            ar.add(p);
            return ar;
        }

       @Override
       public void visit(FieldDeclaration n, Object arg) {
           List<Type> ar = new ArrayList<Type>();
           ar.add(n.getType());
           dfsType(ar, arg);

           super.visit(n, arg);
       }

       private void  dfsType(List<Type> p, Object arg) {
           if (p != null) {
               for (Type i : p) {
                   if (i instanceof ReferenceType) {
                       ReferenceType ref = (ReferenceType) i;
                       i = ref.getType();
                   }
                   if (i instanceof ClassOrInterfaceType) {
                       ClassOrInterfaceType type = (ClassOrInterfaceType) i;
                       checkType(type.getName(), arg);
                       dfsType(type.getTypeArgs(), arg);
                   }

               }
           }

       }


       private  void checkType(String p, Object arg) {
            CurrentData cur = (CurrentData) arg;
            Map<String, File> allClasses = cur.allClasses;
            String target = cur.target;
            Set<String> curEdge = cur.curEdges;

            p+=".java";

            if (allClasses.containsKey(p) && !curEdge.contains(target + p)) {
                addNewEdge(target, p);
                curEdge.add(target + p);
                if (!visited.contains(allClasses.get(p).getName())) {
                    createGraph(allClasses.get(p).getName());
                }
            }
        }
    }

    private Map<String, File> getFilesInPackage(String name) {
        File onePackage = new File(getPathToTarget(start, name));

        Map<String,File> result = new HashMap<String,File>();
        if (!onePackage.toString().equals("")) {
            if (onePackage.isFile()) {
                result.put(onePackage.getName(), onePackage);
            } else {
                File[] files = onePackage.listFiles();

                for (File f : files) {
                    if (f.isFile() && f.getName().matches(".+\\.java")) {
                        result.put(f.getName(), f);
                    }
                }
            }
        }
        return result;
    }
    private String getPathToTarget(File f, String name) {
        File[] files = f.listFiles();
        for (File i : files) {
            if (i.getName().equals(name)|| i.getName().equals(name + ".java")  ) {
                return i.getPath();
            }
            else {
                if (i.isDirectory()) {
                    String cur = getPathToTarget(i, name);
                    if (!cur.equals("")) {
                        return cur;
                    }
                }
            }
        }
        return "";
    }


    private class CurrentData {
        public String target;
        public Map<String, File> allClasses;
        public Set<String> curEdges;

        public CurrentData(String target, Map<String, File> allClasses, Set<String> curEdges) {
            this.target = target;
            this.allClasses = allClasses;
            this.curEdges = curEdges;

        }

    }

    private void addNewEdge(String from, String to) {
        if (result.containsKey(from)) {
            List<String> cur = result.get(from);
            cur.add(to);
        } else {
            List<String> cur = new ArrayList<String>();
            cur.add(to);
            result.put(from, cur);
        }
    }

}

