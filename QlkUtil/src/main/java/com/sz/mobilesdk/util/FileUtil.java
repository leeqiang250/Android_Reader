package com.sz.mobilesdk.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileUtil {

    /**
     * 根据文件的编码获取文件缓冲流
     *
     * @param file
     * @return
     */
    public static Reader getFileBufferByEncode(File file) throws IOException {
        BufferedReader reader = null;
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.mark(4);
        byte[] first3bytes = new byte[3];
        //找到文档的前三个字节并自动判断文档类型。
        bis.read(first3bytes);
        bis.reset();
        if (first3bytes[0] == (byte) 0xEF
                && first3bytes[1] == (byte) 0xBB
                && first3bytes[2] == (byte) 0xBF) {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
            SZLog.e("", "utf-8");
        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFE) {
            reader = new BufferedReader(new InputStreamReader(bis, "unicode"));
        } else if (first3bytes[0] == (byte) 0xFE
                && first3bytes[1] == (byte) 0xFF) {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-16be"));
        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFF) {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-16le"));
        } else {
            reader = new BufferedReader(new InputStreamReader(bis, "gbk"));
            SZLog.e("", "gbk");
        }
        return reader;
    }

    /**
     * 传入文件名以及字符串, 将字符串信息保存到文件中
     *
     * @param fileName
     * @param text
     */
    public static File writeTextToFile(final String fileName, final String text) {
        FileWriter fileWriter = null;
        File file = null;
        try {
            // 创建文件对象
            file = new File(fileName);
            // 向文件写入对象写入信息
            fileWriter = new FileWriter(file);
            // 写文件
            fileWriter.write(text);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 根据文件绝对路径生成文件
     *
     * @param filePath 文件绝对路径
     * @return
     */
    public static File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    public static File createFile(String filePath, String fileName) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(filePath, fileName);
        if (file2.isDirectory()) {
            file2.delete();
        } else if (file2.exists()) {
            return file2;
        }
        file2.createNewFile();
        return file2;
    }


    /**
     * 检查路径是否存在
     *
     * @param path
     * @return
     */
    public static boolean checkFilePathExists(String path) {
        return new File(path).exists();
    }

    /**
     * 新建目录
     *
     * @param directoryName 目录全路径名
     * @return
     */
    public static boolean createDirectory(String directoryName) {
        if (checkFilePathExists(directoryName)) {
            return true;
        }
        SZLog.i("create directory: " + directoryName);
        return "".equals(directoryName) ? false : new File(directoryName)
                .mkdirs();
    }

    /***
     * 获取文件名（不带扩展名）
     * <p>
     * eg：abcdef.jpg; return "abcdef"
     *
     * @param filename
     * @return
     */
    public static String getNameFromFileName(String filename) {
        if (filename == null) {
            return "";
        }

        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }

    /**
     * 获取文件扩展名
     * <p>
     * eg：abcdef.jpg; return "jpg"
     *
     * @param filename
     * @return
     */
    public static String getExtFromFileName(String filename) {
        if (filename == null) {
            return "";
        }
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1);
        }
        return "";
    }

    /**
     * 从文件路径获取文件名
     * <p>
     * eg：/sz/file/abcdef.mp4
     * <p>
     * 得到abcdef.mp4
     *
     * @param filePath
     * @return
     */
    public static String getNameFromFilePath(String filePath) {
        if (filePath == null) {
            return "";
        }

        int pos = filePath.lastIndexOf('/');
        if (pos != -1) {
            return filePath.substring(pos + 1);
        }
        return "";
    }

    /**
     * 删除指定文件夹下所有文件
     *
     * @param path 文件夹完整绝对路径
     */
    @Deprecated
    public static void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) return;
        if (!file.isDirectory()) return;

        String[] tempList = file.list();
        if (tempList == null) return;
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
                SZLog.d("delAllFile", temp.getAbsolutePath());
            }
            if (temp.isDirectory()) {
                delAllFile(path + File.separator + tempList[i]);// 先删除文件夹里面的文件
            }
        }
        file.delete();
    }

    /**
     * 递归删除文件和文件夹
     */
    public static void deleteAllFile(String path) {
        if (path == null) return;
        deleteAllFile(new File(path));
    }

    /**
     * 递归删除文件和文件夹
     */
    public static void deleteAllFile(File file) {
        if (file == null) return;
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFile = file.listFiles();
            if (childFile == null || childFile.length == 0) {
                file.delete();
                return;
            }
            for (File f : childFile) {
                deleteAllFile(f);
            }
            file.delete();
        }
    }

    /**
     * 删除文件夹及所有文件
     *
     * @param folderPath
     *            文件夹完整绝对路径
     */
    // public static void delFolder(String folderPath)
    // {
    // try
    // {
    // delAllFile(folderPath); // 删除完里面所有内容
    // // String filePath = folderPath;
    // // filePath = filePath.toString();
    // java.io.File myFile = new java.io.File(folderPath);
    // myFile.delete(); // 删除空文件夹
    // } catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    // }

    /**
     * 删除指定路径的文件
     *
     * @param filePath
     */
    public static boolean deleteFileWithPath(String filePath) {
        if (!checkFilePathExists(filePath)) return false;

        SecurityManager checker = new SecurityManager();
        File f = new File(filePath);
        checker.checkDelete(filePath);
        if (f.isFile()) {
            SZLog.i("deleteFileWithPath", filePath);
            return f.delete();
        }
        return false;
    }

    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////

    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////////

    /**
     * 获取文件大小
     *
     * @param filePath
     * @return
     */
    public static long getFileSize(String filePath) {
        long size = 0;
        File file = new File(filePath);
        if (file != null && file.exists()) {
            size = file.length();
        }
        return size;
    }

    /**
     * 获取目录文件大小
     *
     * @param dir
     * @return
     */
    public static long getDirSize(File dir) {
        if (dir == null) return 0;
        if (!dir.isDirectory()) return 0;

        long dirSize = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                dirSize += file.length();
            } else if (file.isDirectory()) {
                dirSize += file.length();
                dirSize += getDirSize(file); // 递归调用继续统计
            }
        }
        return dirSize;
    }

}
