package cn.com.pyc.pbbonline.db;

import java.util.List;

public class SharedDBManager
{

	private static SharedDaoImpl daoImpl;

	public SharedDBManager()
	{
		if (daoImpl == null)
		{
			daoImpl = new SharedDaoImpl();
		}
	}

	/**
	 * 保存数据
	 * 
	 * @param shared
	 */
	public boolean saveShared(Shared shared)
	{
		return daoImpl.saveShared(shared);
	}

	/**
	 * 保存数据
	 * 
	 * @param shareds
	 */
	public boolean saveSharedAll(List<Shared> shareds)
	{
		return daoImpl.saveSharedAll(shareds);
	}

	/**
	 * 查询所有
	 * 
	 * @return
	 */
	public List<Shared> findAll()
	{
		return daoImpl.findAll();
	}

	/**
	 * 删除所有
	 * 
	 * @return
	 */
	public boolean deleteAll()
	{
		return daoImpl.deleteAll();
	}

	/**
	 * 根据shareId查询
	 * 
	 * @param shareId
	 * @return
	 */
	public Shared findByShareId(String shareId)
	{
		return daoImpl.findByShareId(shareId);
	}

	/**
	 * 根据账户名查询
	 * 
	 * @param accountName
	 * @return
	 */
	public List<Shared> findByAccount(String accountName)
	{
		return daoImpl.findByAccount(accountName);
	}

	/**
	 * 更新此条数据
	 * 
	 * @param s
	 * @return
	 */
	public boolean updateShared(Shared s)
	{
		return daoImpl.updateShared(s);
	}

	/**
	 * 表中是否存在数据
	 * 
	 * @return
	 */
	public boolean existData()
	{
		return daoImpl.isExistData();
	}

	/**
	 * 按设备分享方式查询所有记录
	 * 
	 * @return
	 */
	public List<Shared> findByDevice()
	{
		return daoImpl.findByDevice();
	}

	/**
	 * 修改新分享的状态
	 * 
	 * @param shared
	 * @return
	 */
	public boolean modifyNewSharedState(Shared shared)
	{
		return daoImpl.modifyNewShared(shared);
	}

	/**
	 * 更新被收回的分享
	 * 
	 * @param shared
	 * @return
	 */
	public boolean updateRevokeShared(Shared shared)
	{
		return daoImpl.updateRevokeShared(shared);
	}

	/**
	 * 更新删除标记
	 * 
	 * @param shared
	 * @return
	 */
	public boolean updateDeleteFlag(Shared shared)
	{
		return daoImpl.updateDeleteFlag(shared);
	}

	/**
	 * 通过isDelete和isRevoke标记删除非按设备的分享
	 * 
	 * @return
	 */
	public boolean deleteUserByFlag(boolean isDelete, boolean isRevoke)
	{
		return daoImpl.deleteUserByFlag(isDelete, isRevoke);
	}
}
