package com.devchao.orm.page;


/**
 * 分页规范
 */
public interface SqlPageBuilder {
    public String buildPageSql(String sql, Integer pageNo, Integer pageSize);
}
