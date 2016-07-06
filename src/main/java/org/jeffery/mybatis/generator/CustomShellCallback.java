package org.jeffery.mybatis.generator;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffery on 16/1/22.
 * 实现DefaultShellCallback
 * 增加java files merge
 */
public class CustomShellCallback extends DefaultShellCallback {
    /**
     * @param overwrite
     */
    public CustomShellCallback(boolean overwrite) {
        super(overwrite);
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

    @Override
    public String mergeJavaFile(String newFileSource, String existingFileFullPath, String[] javadocTags, String fileEncoding) throws ShellException {
//        return super.mergeJavaFile(newFileSource, existingFileFullPath, javadocTags, fileEncoding);
        InputStream newIn = null;
        InputStream existIn = null;
        try {
            newIn = new StringBufferInputStream(newFileSource);
            existIn = new FileInputStream(existingFileFullPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        CompilationUnit newJava = null;
        CompilationUnit existingJava = null;
        try {
            // parse the file
            newJava = JavaParser.parse(newIn);
            existingJava = JavaParser.parse(existIn);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                newIn.close();
                existIn.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // prints the resulting compilation unit to default system output
//        System.out.println(cu.toString());
        List<TypeDeclaration> newJavaTypes = newJava.getTypes();
        if (newJavaTypes.size() > 1) {
            return newFileSource;
        }
        if(null != existingJava.getImports()){
            oldImport:for(ImportDeclaration oldImport:existingJava.getImports()){
                if(null != newJava.getImports()){
                    for(ImportDeclaration newImport:newJava.getImports()){
                        if(newImport.equals(oldImport)){
                            continue oldImport;
                        }
                    }
                }else{
                    newJava.setImports(new ArrayList<ImportDeclaration>());
                }
                newJava.getImports().add(oldImport);
            }
        }
        TypeDeclaration newJavaType = newJavaTypes.get(0);
        for (TypeDeclaration type : existingJava.getTypes()) {
            List<BodyDeclaration> members = type.getMembers();
            member:
            for (BodyDeclaration member : members) {
//                if (member instanceof MethodDeclaration || member instanceof FieldDeclaration) {
//                    member.getComment();
                for (String tag : javadocTags) {
                    if (null != member.getComment() && member.getComment().toString().indexOf(tag) > -1) {
                        continue member;
                    }
                }
                ASTHelper.addMember(newJavaType, member);
//                }
            }
        }
        newFileSource = newJava.toString();
        return newFileSource;
    }
}
