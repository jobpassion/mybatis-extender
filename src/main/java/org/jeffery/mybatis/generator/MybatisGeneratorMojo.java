package org.jeffery.mybatis.generator;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jeffery on 15-8-6.
 */
@Mojo( name = "generate", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MybatisGeneratorMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    private ClassLoader loader;
    @Parameter( property = "xmlConfiguration", defaultValue = "Hello World!" )
    private File xmlConfiguration;

    public static void main(String[] args) throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {

    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> warnings = new ArrayList<String>();
        boolean overwrite = false;
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = null;
        try {
//            config = cp.parseConfiguration(MybatisGeneratorMojo.class.getResourceAsStream("/Generator.xml"));
            config = cp.parseConfiguration(xmlConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLParserException e) {
            e.printStackTrace();
        }
//        String path = MybatisGeneratorMojo.class.getProtectionDomain().getCodeSource().getLocation().getFile();
//        path = path.substring(0, path.lastIndexOf("goblin-dal") + "goblin-dal".length());
        String path = this.project.getBasedir().getPath();
        String javaPath = path + "/src/main/java";
        String xmlPath = path + "/src/main/resources";
        config.getContext("DB2Tables").getJavaClientGeneratorConfiguration().setTargetProject(javaPath);
        config.getContext("DB2Tables").getJavaModelGeneratorConfiguration().setTargetProject(javaPath);
        config.getContext("DB2Tables").getSqlMapGeneratorConfiguration().setTargetProject(xmlPath);
//        DefaultShellCallback callback = new DefaultShellCallback(overwrite);
        //默认Generator针对java files并不会自动merge, 采用的是直接覆盖或者生成到另一个备份文件的策略
        //这里调用自己扩展的CustomShellCallback, 实现了针对java files的自动merge.
        DefaultShellCallback callback = new CustomShellCallback(overwrite);
        MyBatisGenerator myBatisGenerator = null;
        try {
            myBatisGenerator = new MyBatisGenerator(config, callback, warnings);
            myBatisGenerator.generate(null);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

    }
}

