package com.devchao.orm.page;

/**
 * MySQL分页策略
 */
public class SqlPageBuilder4MySQL implements SqlPageBuilder {

    @Override
    public String buildPageSql(String sql, Integer pageNo, Integer pageSize) {
    	if (pageNo == null || pageSize == null) {
    		return sql;
    	}
        return sql + " LIMIT " + (pageNo - 1) * pageSize + "," + pageSize;
    }

}
