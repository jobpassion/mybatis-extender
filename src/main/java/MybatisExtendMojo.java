package org.jeffery.mybatis.extender;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeffery on 15-8-6.
 */
@Mojo( name = "extend", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class MybatisExtendMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    private ClassLoader loader;

    public static void main(String[] args) throws IOException, MojoFailureException, MojoExecutionException {
    }

    public void execute() throws MojoExecutionException, MojoFailureException {


        try {
            List<String> eles = project.getRuntimeClasspathElements();

            URL[] urls = new URL[eles.size()];
            int i=0;
            for(String s:eles){
                urls[i++] = new File(s).toURL();
            }
            URLClassLoader cl = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            this.loader = cl;
        }catch (DependencyResolutionRequiredException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        processResourcesDir(project.getFile().getParent() + "/src/main/resources");
    }
    private void processResourcesDir(String filename){
        File file = new File(filename);
        if(file.isDirectory()){
            for(File f:file.listFiles()){
                processResourcesDir(f.getAbsolutePath());
            }
        }else{
            processFile(file.getAbsolutePath());
        }
    }
    private boolean processFile(String filename) {
        if(!filename.endsWith(".xml")){
            return false;
        }

        String superFilename = null;
        try {
            String origin = FileUtils.readFileToString(new File(filename));
            Pattern pattern = Pattern.compile("<mapper[^>]*extend=\"([^\"]*)\">");
            Matcher m = pattern.matcher(origin);
            String header = null;
            if(m.find()){
                superFilename = m.group(1).replaceAll("\\.", "/") + ".xml";
                header = m.group(0);
            }else{
                return false;
            }
            System.out.println("mybatis extender: processing:" + filename);
            InputStream stream = loader.getResourceAsStream(superFilename);
            if(null == stream){
                System.out.println("mybatis extender: error process file:" + filename + "\n ,can not find super resource " + superFilename);
                return false;
            }
            String superFile = IOUtils.toString(stream);
            Pattern pattern2 = Pattern.compile("<mapper[^>]*>([\\d\\D]*)</mapper>");
            Matcher m2 = pattern2.matcher(superFile);
            String superContent = null;
            if(m2.find()){
                superContent = m2.group(1);
            }else{
                return false;
            }
            String parsed = origin.replaceAll("extend=\"([^\"]*)\"","");
            header = header.replaceAll("extend=\"([^\"]*)\"","");
            parsed = parsed.replace(header, header + "\n" + superContent);
            FileUtils.writeStringToFile(new File(project.getFile().getParent() + "/target/classes" + filename.substring(filename.indexOf("resources") + 9)), parsed);
            System.out.println("mybatis extender: succeed:" + filename);
        } catch (Exception e) {
            System.out.println("mybatis extender: error process file:" + filename);
//            e.printStackTrace();
            return false;
        }

        return false;
    }
}

