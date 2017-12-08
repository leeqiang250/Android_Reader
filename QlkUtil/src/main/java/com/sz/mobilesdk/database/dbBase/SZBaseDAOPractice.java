package com.sz.mobilesdk.database.dbBase;

import java.util.Collection;
import java.util.List;


public interface SZBaseDAOPractice<T> {
	/*数据表创建*/
	
	public void create(Class<T> entityClass);
	
	/*数据实体保存*/
	public boolean save(T entity);
	public int[] batchSave(Collection<T> entityCollection);
	public boolean cascadedSave(T entity);
	public boolean cascadedBatchSave(Collection<T> entityCollection);
	
	/*数据实体删除*/
	public int delete(T entity);
	public int[] batchDelete(Collection<T> entityCollection);
	public boolean cascadedDeleteById(String id);
	public boolean cascadedBatchDelete(Collection<T> entityCollection);
	
	/*数据实体更新*/
	public int update(T entity);
	public int[] batchUpdate(Collection<T> entityCollection);
	public boolean cascadedUpdate(T entity);
	public boolean cascadedBatchUpdate(Collection<T> entityCollection);

	/*数据实体查询*/
	public T findById(String id);
	public List<T> findAll(Class<T> entityClass, String order);
	public List<?> findByQuery(String sql, Class<?> entityClass);
	public List<?> findByQuery(String[] elements,String[] args, Class<?> entityClass);
	public T findObjectByQuery(String[] elements,String[] args, Class<?> entityClass);
	
	/*自定义SQL操作*/
	public void executeBySql(String sql);
}
