package org.jeffery.mybatis.generator;

import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.*;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameExamplePackagePlugin extends PluginAdapter {
    private String searchString;
    private String replaceString;
    private Pattern pattern;

    public RenameExamplePackagePlugin() {
    }

    private Object clone(Element textElement){
        if(textElement instanceof TextElement){
            return clone((TextElement)textElement);
        }else if(textElement instanceof XmlElement){
            return clone((XmlElement)textElement);
        }
        return null;
    }
    private Object clone(TextElement textElement){
        return new TextElement(textElement.getContent());
    }
    private Object clone(Attribute textElement){
        return new Attribute(textElement.getName(), textElement.getValue());
    }
    private Object clone(XmlElement textElement){
        XmlElement xmlElement = new XmlElement(textElement.getName());
        for(Attribute attr:textElement.getAttributes()){
            xmlElement.addAttribute((Attribute) clone(attr));
        }
        for(Element e:textElement.getElements()){
            xmlElement.addElement((Element) clone(e));
        }
        return xmlElement;
    }

    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for(InnerClass innerClass:topLevelClass.getInnerClasses()){
            if(innerClass.getType().toString().equals("GeneratedCriteria")){
                for(Method method:innerClass.getMethods()){
                    method.setVisibility(JavaVisibility.PUBLIC);
                }
            }
        }
        return super.modelExampleClassGenerated(topLevelClass, introspectedTable);
    }

    @Override
    public boolean sqlMapGenerated(GeneratedXmlFile sqlMap, IntrospectedTable introspectedTable) {
        try {
            Field field = sqlMap.getClass().getDeclaredField("document");
            field.setAccessible(true);
            Document document = (Document) field.get(sqlMap);
            List elements = document.getRootElement().getElements();
            for(int i=0; i<elements.size(); i++){
                XmlElement element = (XmlElement) elements.get(i);
                if(element.getAttributes().size() > 0 && "Base_Column_List".equals(element.getAttributes().get(0).getValue())
                        && "id".equals(element.getAttributes().get(0).getName())){
                    XmlElement newElement = new XmlElement("sql");
                    newElement.addAttribute(new Attribute("id", "commonSelect"));
                    for(int j=0,b=0; j<element.getElements().size(); j++){
                        TextElement e = (TextElement) element.getElements().get(j);
                        if(b==0){
                            newElement.addElement(e);
                        }else{
                            TextElement last = (TextElement) clone(e);
                            Pattern pattern = Pattern.compile("([^,^ ]+)");
                            Matcher matcher = pattern.matcher(last.getContent());
                            String text = matcher.replaceAll(introspectedTable.getTableConfiguration().getTableName() + ".$1 '"+introspectedTable.getTableConfiguration().getTableName()+".$1"+"'");
                            newElement.addElement(new TextElement(text));
                        }
                        if(e.getContent().contains("-->")){
                            b=1;
                        }
                    }
                    elements.add(newElement);

                    newElement = new XmlElement("sql");
                    newElement.addAttribute(new Attribute("id", "Self_Column_List"));
                    for(int j=0,b=0; j<element.getElements().size(); j++){
//                    for(Element e:element.getElements()){
                        TextElement e = (TextElement) element.getElements().get(j);
                        if(b==0){
                            newElement.addElement(e);
                        }else{
                            TextElement last = (TextElement) clone(e);
                            Pattern pattern = Pattern.compile("([^,^ ]+)");
                            Matcher matcher = pattern.matcher(last.getContent());
                            String text = matcher.replaceAll(introspectedTable.getTableConfiguration().getTableName() + ".$1 '$1'");
                            newElement.addElement(new TextElement(text));
                        }
                        if(e.getContent().contains("-->")){
                            b=1;
                        }
                    }
                    elements.add(newElement);

                    newElement = new XmlElement("sql");
                    newElement.addAttribute(new Attribute("id", "Main_Column_List"));
                    for(int j=0,b=0; j<element.getElements().size(); j++){
//                    for(Element e:element.getElements()){
                        TextElement e = (TextElement) element.getElements().get(j);
                        if(b==0){
                            newElement.addElement(e);
                        }else{
                            TextElement last = (TextElement) clone(e);
                            Pattern pattern = Pattern.compile("([^,^ ]+)");
                            Matcher matcher = pattern.matcher(last.getContent());
                            String text = matcher.replaceAll("Main.$1 '$1'");
                            newElement.addElement(new TextElement(text));
                        }
                        if(e.getContent().contains("-->")){
                            b=1;
                        }
                    }
                    elements.add(newElement);
                }
                if(element.getAttributes().size() > 0 && "BaseResultMap".equals(element.getAttributes().get(0).getValue())
                        && "id".equals(element.getAttributes().get(0).getName())){
                    XmlElement newElement = new XmlElement("resultMap");
                    newElement.addAttribute(new Attribute("id", "joinResultMap"));
                    newElement.addAttribute(new Attribute("type", element.getAttributes().get(1).getValue()));
                    element = (XmlElement) clone(element);
                    for(Element e:element.getElements()){
                        if(e instanceof XmlElement){
                            XmlElement xmlE = (XmlElement) e;
                            for(Attribute attr:xmlE.getAttributes()){
                                if(attr.getName().equals("column")){
                                    xmlE.getAttributes().remove(attr);
                                    xmlE.addAttribute(new Attribute("column", introspectedTable.getTableConfiguration().getTableName() + "." + attr.getValue()));
                                    break;
                                }
                            }
                            newElement.addElement(xmlE);
                        }else{
                            newElement.addElement(e);
                        }
                    }
                    elements.add(newElement);
                }
            }

            XmlElement newElement = new XmlElement("sql");
            newElement.addAttribute(new Attribute("id", "commonJoin"));

            String foreignKey = Introspector.decapitalize(introspectedTable.getTableConfiguration().getTableName());
            if (null != introspectedTable.getTableConfigurationProperty("foreignKey")){
                foreignKey = introspectedTable.getTableConfigurationProperty("foreignKey");
            }
            String text = "left join "+introspectedTable.getTableConfiguration().getTableName()+" on Main."+foreignKey+" = "+introspectedTable.getTableConfiguration().getTableName()+".id";
            newElement.addElement(new TextElement("<!--\n" +
                    "      WARNING - @mbggenerated\n" +
                    "      This element is automatically generated by MyBatis Generator, do not modify.\n" +
                    "    -->"));
            newElement.addElement(new TextElement(text));
            elements.add(newElement);


        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return super.sqlMapGenerated(sqlMap, introspectedTable);
    }

    public boolean validate(List<String> warnings) {
        return true;
    }

    public void initialized(IntrospectedTable introspectedTable) {
        String mapperPackage = introspectedTable.getMyBatis3JavaMapperType();
        mapperPackage = mapperPackage.substring(0, mapperPackage.lastIndexOf("."));
        String exampleType = introspectedTable.getExampleType();
        exampleType = exampleType.substring(exampleType.lastIndexOf(".") + 1);
        exampleType = exampleType.substring(0, exampleType.lastIndexOf("Example")) + "Query";
        introspectedTable.setExampleType(mapperPackage + "." + exampleType);

        introspectedTable.setMyBatis3JavaMapperType(introspectedTable.getMyBatis3JavaMapperType().substring(0, introspectedTable.getMyBatis3JavaMapperType().length() - 6) + "Dao");
        introspectedTable.setBaseRecordType(introspectedTable.getBaseRecordType() + "DO");

        introspectedTable.setCountByExampleStatementId("countByQuery");
        introspectedTable.setDeleteByExampleStatementId("deleteByQuery");
        introspectedTable.setSelectByExampleStatementId("selectByQuery");
        introspectedTable.setSelectByExampleWithBLOBsStatementId("selectByQueryWithBLOBs");
        introspectedTable.setUpdateByExampleSelectiveStatementId("updateByQuerySelective");
        introspectedTable.setUpdateByExampleStatementId("updateByQuery");
        introspectedTable.setUpdateByExampleWithBLOBsStatementId("updateByQueryWithBLOBs");
    }
}
