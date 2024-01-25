package com.shebao.dao;

import java.util.List;
import java.util.Map;

/**
 * 代码生成器
 * 
 * @author zhangyaoyao
 * @email yoyo_jang@qq.com
 * @date 2021年04月07日
 */
public interface SysGeneratorDao {
	
	List<Map<String, Object>> queryList(Map<String, Object> map);
	
	int queryTotal(Map<String, Object> map);
	
	Map<String, String> queryTable(String tableName);
	
	List<Map<String, String>> queryColumns(String tableName);
}
