package com.sz.mobilesdk.authentication;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.database.bean.Asset;
import com.sz.mobilesdk.database.bean.Permission;
import com.sz.mobilesdk.database.practice.AssetDAOImpl;
import com.sz.mobilesdk.util.AESUtil;

import java.util.List;

public class SZContent {

    private static final String TAG = "szc";

    static final String kConstraintDatetime = "datetime";

    static final String kConstraintAccumulated = "accumulated";

    static final String kConstraintIndividual = "individual";

    static final String kConstraintSystem = "system";

    private static final String kPermissionPlay = "PLAY";
    private static final String kPermissionDisplay = "DISPLAY";
    private static final String kPermissionPrint = "PRINT";

    private AssetDAOImpl assetDAO = new AssetDAOImpl();
    private SZCheckRight rightCheck;
    private SZPermissionDisplay permissionDisplay = null;
    private SZPermissionPlay permissionPlay = null;
    private SZPermissionPrint permissionPrint = null; //移动端没有打印权限，应该忽略
    private String cek_cipher_value = "";

    @SuppressWarnings("unchecked")
    public SZContent(String assetID) {
        Asset asset = assetDAO.findObjectByQuery(new String[]{"_id"},
                new String[]{assetID}, Asset.class);
        asset.setPermissions((List<Permission>) assetDAO.findByQuery(
                new String[]{"asset_id"}, new String[]{assetID},
                Permission.class));
        //cek_cipher_value = asset.getCek_cipher_value();
        String aesKey = asset.getCek_cipher_value();
        //SZLog.e(TAG, "a: " + aesKey);
        cek_cipher_value = (aesKey.length() == 32) ? aesKey : AESUtil.decrypt(aesKey,
                Constant.FILE_KEY_SECRET);
        //SZLog.e(TAG, "d: " + cek_cipher_value);

        for (int i = 0; i < asset.getPermissions().size(); i++) {
            Permission permission = asset.getPermissions().get(i);
            if (kPermissionPlay.equals(permission.getElement())) {
                permissionPlay = new SZPermissionPlay();
                permissionPlay.initWithPermission(permission);
            } else if (kPermissionDisplay.equals(permission.getElement())) {
                permissionDisplay = new SZPermissionDisplay();
                permissionDisplay.initWithPermission(permission);
            } else if (kPermissionPrint.equals(permission.getElement())) {
                //print权限
                permissionPrint = new SZPermissionPrint();
                permissionPrint.initWithPermission(permission);
            }
        }
        rightCheck = new SZCheckRight();
    }

    /**
     * 获取秘钥，初始化SZContent之后赋值
     *
     * @return
     */
    public String getCek_cipher_value() {
        return cek_cipher_value;
    }

    /**
     * 验权
     *
     * @return
     */
    public boolean checkOpen() {
        return rightCheck.checkWithPermission(permissionPlay)
                || rightCheck.checkWithPermission(permissionDisplay);
    }

    /**
     * 检查权限是否到开始生效的时间
     * <p>
     * 是：未生效 ； 否：已生效
     *
     * @return true:未生效  ；  否：已经生效
     */
    public boolean isInEffective() {
        return rightCheck.checkFileIneffective(permissionPlay)
                || rightCheck.checkFileIneffective(permissionDisplay);
    }

    public boolean checkPrint() {
        return permissionPrint != null;
    }

    public long getOdd_datetime_end() {
        if (permissionPlay != null) {
            return this.permissionPlay.odd_datetime_end;
        } else if (permissionDisplay != null) {
            return this.permissionDisplay.odd_datetime_end;
        }

        return 0L;
    }

    public long getOdd_datetime_start() {
        return permissionPlay.odd_datetime_start;
    }

    // 获取剩余时间,-1永久，0过期
    public long getAvailbaleTime() {
        long currentTime = System.currentTimeMillis();
        if (this.permissionDisplay != null) {
            if (this.permissionDisplay.isAllPermission())
                return -1;
            else {
                long availableTime = (this.permissionDisplay.odd_datetime_end - currentTime);
                // availableTime = availableTime > 0 ? availableTime : 0;
                return availableTime > 0 ? availableTime : 0;
            }
        } else if (this.permissionPlay != null) {
            if (this.permissionPlay.isAllPermission())
                return -1;
            else {
                if (this.permissionPlay.odd_datetime_end < currentTime)
                    return 0;

                else {
                    if (this.permissionPlay.odd_datetime_start > currentTime)
                        return 0;
                    return (this.permissionPlay.odd_datetime_end - currentTime);
                }
            }
        }
        return 0;
    }

}
