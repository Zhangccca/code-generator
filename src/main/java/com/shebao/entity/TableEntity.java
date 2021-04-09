package com.shebao.entity;

import lombok.Data;

import java.util.List;

/**
 * 表数据
 * 
 * @author chutong
 * @email chutong@51shebao.com
 * @date 2021年04月07日
 */
@Data
public class TableEntity {
    // 表的名称
    private String tableName;
    // 表的备注
    private String comments;
    // 表的主键
    private ColumnEntity pk;
    // 表的列名
    private List<ColumnEntity> columns;
    // 表的列名(不包含主键)
    private List<ColumnEntity> notPkColumns;
    // 表的列名(不包含共有的属性)
    private List<ColumnEntity> notCommonColumns;

    // 类名(第一个字母大写)，如：sys_user => SysUser
    private String className;
    // 类名(第一个字母小写)，如：sys_user => sysUser
    private String classname;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

}
