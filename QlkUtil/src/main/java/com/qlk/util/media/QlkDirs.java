package com.qlk.util.media;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

public class QlkDirs {
    private static final String DEFALUT_BOOT_NAME = "内部存储";
    private static final String EXTRA_BOOT_NAME = "扩展存储卡";

    private static String BootDefault;
    private static String BootExtra;

    private static final Vector<String> CardsPaths = new Vector<String>();

    public synchronized static ArrayList<String> getCardsPaths() {
        return new ArrayList<>(CardsPaths);
    }

    /**
     * 获取手机上容量大于0的存储器路径
     *
     * @param context
     * @return
     */
    public synchronized static ArrayList<String> reGetCardsPaths(Context context) {
        HashSet<String> sets = new HashSet<String>();
        try {
            // Api abover 10
            StorageManager mStorageManager = (StorageManager) context
                    .getSystemService(Activity.STORAGE_SERVICE);
            String[] paths = (String[]) mStorageManager.getClass().getMethod("getVolumePaths")
                    .invoke(mStorageManager);

            for (String path : paths) {
                if(path != null) {
                    File file = new File(path);
                    if (file.getTotalSpace() > 0 && file.canRead()) {
                        sets.add(path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        sets.add(getDefaultBoot());
        String extPath = getExtPath();
        if (extPath != null && new File(extPath).getTotalSpace() > 0) {
            sets.add(extPath);
        }

        CardsPaths.clear();
        CardsPaths.addAll(sets);

        return new ArrayList<String>(CardsPaths);
    }

    // 方法二
    private static String getExtPath() {
        String sdcard_path = null;
        String sd_default = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (sd_default.endsWith("/")) {
            sd_default = sd_default.substring(0, sd_default.length() - 1);
        }
        // 得到路径
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                if (line.contains("secure"))
                    continue;
                if (line.contains("asec"))
                    continue;
                if (line.contains("fat") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                } else if (line.contains("fuse") && line.contains("/mnt/")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        if (sd_default.trim().equals(columns[1].trim())) {
                            continue;
                        }
                        sdcard_path = columns[1];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sdcard_path;
    }

    /**
     * @return 内置硬盘路径
     */
    public static String getDefaultBoot() {
        if (BootDefault == null) {
            BootDefault = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return BootDefault;
    }

    /**
     * @return 扩展存储卡路径，没有则返回内置硬盘路径
     */
    public static String getExtraBoot() {
        if (BootExtra == null) {
            for (String boot : CardsPaths) {
                if (boot != null && !boot.startsWith(getDefaultBoot())) {
                    BootExtra = boot;
                }
            }
        }

        return BootExtra == null ? getDefaultBoot() : BootExtra;
    }

    /**
     * 根据bootPath返回其中文名称：“内部存储”、“扩展存储卡”
     *
     * @param bootPath
     * @return
     */
    public static String getBootName(String bootPath) {
        if (bootPath.equals(getDefaultBoot())) {
            return DEFALUT_BOOT_NAME;
        } else if (bootPath.equals(getExtraBoot())) {
            return EXTRA_BOOT_NAME;
        } else {
            return "未知类型卡";
        }
    }

    /**
     * @param bootName 值必须是DEFALUT_BOOT_NAME或者EXTRA_BOOT_NAME，否则返回null
     * @return
     */
    public static String getBootPath(String bootName) {
        if (bootName.equals(DEFALUT_BOOT_NAME)) {
            return getDefaultBoot();
        } else if (bootName.equals(EXTRA_BOOT_NAME)) {
            return getExtraBoot();
        } else {
            return null;
        }
    }

    /**
     * 判断该路径是在哪个存储器上
     *
     * @param path
     * @return
     */
    public static String getBootDir(String path) {
        if (path.startsWith(getDefaultBoot())) {
            return getDefaultBoot();
        } else if (path.startsWith(getExtraBoot())) {
            return getExtraBoot();
        } else {
            return getExtraBoot();
        }
    }

    /**
     * SD卡是否挂载
     *
     * @return
     */
    public static boolean isStorageInUse() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 是否在内置硬盘或者外置SD卡上（排除系统目录）
     *
     * @param path
     * @return
     */
    public static boolean isOnDisk(String path) {
        for (String boot : CardsPaths) {
            if (path.startsWith(boot)) {
                return true;
            }
        }
        return false;
    }

}
