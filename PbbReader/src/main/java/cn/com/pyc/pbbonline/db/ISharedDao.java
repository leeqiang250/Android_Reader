package cn.com.pyc.pbbonline.db;

import java.util.List;

//增删改查
public interface ISharedDao
{

	//保存数据，数据模型为List
	boolean saveSharedAll(List<Shared> shareds);

	//保存数据，单个逐一保存
	boolean saveShared(Shared shared);

	//通过shareId删除
	boolean deleteByShareId(String shareId);

	//通过账户删除
	boolean deleteByAccount(String accountName);

	//通过删除标记删除按身份和人数的。回收的不删除
	boolean deleteUserByFlag(boolean isDelete, boolean isRevoke);

	//删除所有数据
	boolean deleteAll();

	//更新分享
	boolean updateShared(Shared shared);

	//更新 新的分享，主要修改用户是否点击打开状态
	boolean modifyNewShared(Shared shared);

	//更新已被收回的分享。
	boolean updateRevokeShared(Shared shared);

	//更新删除标记
	boolean updateDeleteFlag(Shared shared);

	//查询所有
	List<Shared> findAll();

	//通过shareId查询
	Shared findByShareId(String shareId);

	//通过设备分享方式查询
	List<Shared> findByDevice();

	//通过账户查询
	List<Shared> findByAccount(String accountName);

	//查询该记录是否标记删除
	List<Shared> findByDelete(boolean isDelete);

	//查询数据表中是否存在数据，无论多少条
	boolean isExistData();

}
