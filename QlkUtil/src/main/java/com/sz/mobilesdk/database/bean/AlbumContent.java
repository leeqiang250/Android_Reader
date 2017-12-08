package com.sz.mobilesdk.database.bean;

import java.io.Serializable;

public class AlbumContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9148974058794534252L;
	
	private String id = "";
	private String name;
	private String content_id;
	private String album_id;
	private String modify_time;
	private String asset_id;
	private String fileType;
	private String myProId;

	//新增
	private String currentItemId;//当前文件id（原oldItemId）
	private String latestItemId;//更新后的文件id（原newItemId）
	private String collectionId; //文件所属集合的id
	private String musicLrcId;
	private long contentSize;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContent_id() {
		return content_id;
	}

	public void setContent_id(String content_id) {
		this.content_id = content_id;
	}

	public String getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(String album_id) {
		this.album_id = album_id;
	}

	public String getModify_time() {
		return modify_time;
	}

	public void setAsset_id(String asset_id) {
		this.asset_id = asset_id;
	}

	public String getAsset_id() {
		return asset_id;
	}

	public void setModify_time(String modify_time) {
		this.modify_time = modify_time;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	public void setMyProId(String myProId)
	{
		this.myProId = myProId;
	}
	
	public String getMyProId()
	{
		return myProId;
	}

	public String getCurrentItemId() {
		return currentItemId;
	}

	public void setCurrentItemId(String currentItemId) {
		this.currentItemId = currentItemId;
	}

	public String getLatestItemId() {
		return latestItemId;
	}

	public void setLatestItemId(String latestItemId) {
		this.latestItemId = latestItemId;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public String getMusicLrcId() {
		return musicLrcId;
	}

	public void setMusicLrcId(String musicLrcId) {
		this.musicLrcId = musicLrcId;
	}

	public long getContentSize() {
		return contentSize;
	}

	public void setContentSize(long contentSize) {
		this.contentSize = contentSize;
	}
}
