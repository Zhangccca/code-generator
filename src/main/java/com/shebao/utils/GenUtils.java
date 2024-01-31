package com.shebao.utils;

import com.shebao.entity.ColumnEntity;
import com.shebao.entity.TableEntity;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成器 工具类
 *
 * @author zhangyaoyao
 * @email yoyo_jang@qq.com
 * @date 2021年04月07日
 */
public class GenUtils {
    // 表的公共列属性
    public final static String[] COMMON_ATTR = {"create_time", "create_user_id", "update_time",
            "update_user_id", "remark", "version", "is_delete"};

    public static List<String> getTemplates() {
        List<String> templates = new ArrayList<String>();
        templates.add("template/Entity.java.vm");
        templates.add("template/InsertDTO.java.vm");
        templates.add("template/QueryDTO.java.vm");
        templates.add("template/UpdateDTO.java.vm");
        templates.add("template/VO.java.vm");
        templates.add("template/Mapper.java.vm");
        templates.add("template/Mapper.xml.vm");
        templates.add("template/Service.java.vm");
        templates.add("template/ServiceImpl.java.vm");
        templates.add("template/Controller.java.vm");
        templates.add("template/AddRequest.java.vm");
        templates.add("template/BatchDeleteRequest.java.vm");
        templates.add("template/UpdateRequest.java.vm");
        templates.add("template/HandleService.java.vm");
        return templates;
    }

    /**
     * 生成代码
     */
    public static void generatorCode(Map<String, String> table, List<Map<String, String>> columns,
                                     ZipOutputStream zip) {
        // 配置信息
        Configuration config = getConfig();
        boolean hasBigDecimal = false;
        boolean hasDate = false;
        // 表信息
        TableEntity tableEntity = new TableEntity();
        tableEntity.setTableName(table.get("tableName"));
        tableEntity.setComments(table.get("tableComment"));
        // 表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getString("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setClassname(StringUtils.uncapitalize(className));

        // 列信息
        List<ColumnEntity> columsList = new ArrayList<>();
        List<ColumnEntity> notPkColumnList = new ArrayList<>();
        List<ColumnEntity> notCommonColumnList = new ArrayList<>();
        for (Map<String, String> column : columns) {
            ColumnEntity columnEntity = new ColumnEntity();
            columnEntity.setColumnName(column.get("columnName"));
            columnEntity.setDataType(column.get("dataType"));
            columnEntity.setComments(column.get("columnComment"));
            columnEntity.setExtra(column.get("extra"));

            // 列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setAttrname(StringUtils.uncapitalize(attrName));

            // 列的数据类型，转换成Java类型
            String attrType = config.getString(columnEntity.getDataType(), "unknowType");
            columnEntity.setAttrType(attrType);
            if (!hasBigDecimal && attrType.equals("BigDecimal")) {
                hasBigDecimal = true;
            }
            if (!hasDate && attrType.equals("Date")) {
                hasDate = true;
            }
            // 是否主键
            if ("PRI".equalsIgnoreCase(column.get("columnKey")) && tableEntity.getPk() == null) {
                tableEntity.setPk(columnEntity);
            }
            // 不包含主键的列集合
            if (!"PRI".equalsIgnoreCase(column.get("columnKey"))) {
                notPkColumnList.add(columnEntity);
            }
            // 不包含公共属性的列集合
            if (StringUtils.indexOfAny(columnEntity.getColumnName(), COMMON_ATTR) < 0) {
                if (!hasDate && attrType.equals("Date")) {
                    hasDate = true;
                }
                notCommonColumnList.add(columnEntity);
            }
            columsList.add(columnEntity);
        }
        tableEntity.setColumns(columsList);
        tableEntity.setNotPkColumns(notPkColumnList);
        tableEntity.setNotCommonColumns(notCommonColumnList);

        // 没主键，则第一个字段为主键
        if (tableEntity.getPk() == null) {
            tableEntity.setPk(tableEntity.getColumns().get(0));
        }

        // 设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);
        String mainPath = config.getString("mainPath");
        mainPath = StringUtils.isBlank(mainPath) ? "com.shebao" : mainPath;
        // 封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        map.put("comments", tableEntity.getComments());
        map.put("pk", tableEntity.getPk());
        map.put("className", tableEntity.getClassName());
        map.put("classname", tableEntity.getClassname());
        map.put("pathName", tableEntity.getClassname().toLowerCase());
        map.put("pathPrefix", config.getString("pathPrefix"));
        map.put("columns", tableEntity.getColumns());
        map.put("notPkColumns", tableEntity.getNotPkColumns());
        map.put("notCommonColumns", tableEntity.getNotCommonColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("hasDate", hasDate);
        map.put("mainPath", mainPath);
        map.put("package", config.getString("package"));
        map.put("moduleName", config.getString("moduleName"));
        map.put("author", config.getString("author"));
        map.put("email", config.getString("email"));
        map.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        VelocityContext context = new VelocityContext(map);

        // 获取模板列表
        List<String> templates = getTemplates();
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                System.out.println(getFileName(template, tableEntity.getClassName(),
                        config.getString("package"), config.getString("moduleName")));
                // 添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(),
                        config.getString("package"), config.getString("moduleName"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new RRException("渲染模板失败，表名：" + tableEntity.getTableName(), e);
            }
        }
    }


    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if (StringUtils.isNotBlank(tablePrefix)) {
            if (tableName.startsWith(tablePrefix)) {
                int length = tablePrefix.length();
                tableName = tableName.substring(length);
            }
            //tableName = tableName.replace(tablePrefix, "");
        }
        return columnToJava(tableName);
    }

    /**
     * 获取配置信息
     */
    public static Configuration getConfig() {
        try {
            return new PropertiesConfiguration("generator.properties");
        } catch (ConfigurationException e) {
            throw new RRException("获取配置文件失败，", e);
        }
    }

    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName,
                                     String moduleName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName
                    + File.separator;
        }
        if (template.contains("AddRequest.java.vm")) {
            return packagePath + "request" + File.separator + className + "AddRequest.java";
        }
        if (template.contains("BatchDeleteRequest.java.vm")) {
            return packagePath + "request" + File.separator + className + "BatchDeleteRequest.java";
        }
        if (template.contains("UpdateRequest.java.vm")) {
            return packagePath + "request" + File.separator + className + "UpdateRequest.java";
        }
        if (template.contains("Entity.java.vm")) {
            return packagePath + "entity" + File.separator + className + ".java";
        }
        if (template.contains("QueryDTO.java.vm")) {
            return packagePath + "dto" + File.separator + className + "QueryDTO.java";
        }
        if (template.contains("InsertDTO.java.vm")) {
            return packagePath + "dto" + File.separator + className + "InsertDTO.java";
        }
        if (template.contains("UpdateDTO.java.vm")) {
            return packagePath + "dto" + File.separator + className + "UpdateDTO.java";
        }
        if (template.contains("VO.java.vm")) {
            return packagePath + "vo" + File.separator + className + "VO.java";
        }

        if (template.contains("Mapper.java.vm")) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }

        if (template.contains("Service.java.vm") && !template.contains("HandleService.java.vm")) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("HandleService.java.vm")) {
            System.out.println(packagePath + "service" + File.separator + "handle" + File.separator + "Handle" + className + "Service.java");
            return packagePath + "service" + File.separator + "handle" + File.separator + "Handle" + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm")) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className
                    + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm")) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Mapper.xml.vm")) {
            return "main" + File.separator + "resources" + File.separator + "mapper"
                    + File.separator + moduleName + File.separator + className + "Mapper.xml";
        }

        if (template.contains("menu.sql.vm")) {
            return className.toLowerCase() + "_menu.sql";
        }

        if (template.contains("index.vue.vm")) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator
                    + "views" + File.separator + "modules" + File.separator + moduleName
                    + File.separator + className.toLowerCase() + ".vue";
        }

        if (template.contains("add-or-update.vue.vm")) {
            return "main" + File.separator + "resources" + File.separator + "src" + File.separator
                    + "views" + File.separator + "modules" + File.separator + moduleName
                    + File.separator + className.toLowerCase() + "-add-or-update.vue";
        }

        return null;
    }
}
