package cn.com.pyc.suizhi.manager;

import android.util.Log;
import android.util.Xml;

import com.sz.mobilesdk.common.Constant;
import com.sz.mobilesdk.models.xml.OEX_Agreement;
import com.sz.mobilesdk.models.xml.OEX_Rights;
import com.sz.mobilesdk.models.xml.XML2JSON_Album;
import com.sz.mobilesdk.util.AESUtil;
import com.sz.mobilesdk.util.FileUtil;
import com.sz.mobilesdk.util.SZLog;
import com.sz.mobilesdk.util.SecurityUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * drm文件构成 drm分为有头文件和资源文件两部分构成，中间通过32byte的md5做分割 <br/>
 * 前8byte通过一个long类型变量，记录了整个头文件的长度 <br/>
 * 每个头文件又分为3部分: <br/>
 * 40个byte记录文件名 <br/>
 * 8个byte记录一个long类型变量，表示文件的起点 <br/>
 * 8个byte记录一个long类型变量，表示文件的终点 <br/>
 * <p>
 * 前8byte的long类型变量 /(40+8+8) = 头文件的个数，每个头文件都对应着一个资源文件，所以也就得到了资源文件的个数 <br/>
 * <br/>
 * <p>
 * 解析drm包类
 */
public class ParserEngine {
    private static final String TAG = "PEngine";

    /*
     * 解析drm文件
     *
     * @param drmPath    被解析文件路径
     * @param decodePath 解析后保存文件路径
     */
    @Deprecated
    public static List<CommonFile> parserDRMFile(String drmPath, String decodePath) {

        List<CommonFile> files = new ArrayList<CommonFile>();

        File file = new File(drmPath);

        if (!FileUtil.checkFilePathExists(drmPath)) {
            Log.i(TAG, "文件不存在");
            return null;
        }
        FileOutputStream fos = null;
        FileInputStream fis = null;
        byte[] eightBytes = new byte[8];
        byte[] fileMd5Bytes = new byte[32];
        byte[] headFileNameBytes = new byte[40];
        int bufferLength = 1024 * 1024;
        try {
            // 头文件解析
            fis = new FileInputStream(file);

            fis.read(eightBytes);
            long len1 = SecurityUtil.bytes2Long(eightBytes);
            int count = (int) (len1 / (headFileNameBytes.length
                    + eightBytes.length + eightBytes.length));

            long[] fileStarts = new long[count];
            long[] fileEnds = new long[count];
            for (int i = 0; i < count; i++) {
                CommonFile f = new CommonFile();
                fis.read(headFileNameBytes);
                String filename = new String(headFileNameBytes).trim();
                f.filetype = getFileType(filename);
                f.filename = filename;
                f.filepath = decodePath + File.separator + filename;
                fis.read(eightBytes);
                fileStarts[i] = SecurityUtil.bytes2Long(eightBytes);
                fis.read(eightBytes);
                fileEnds[i] = SecurityUtil.bytes2Long(eightBytes);
                files.add(f);

                SZLog.d(TAG, "filename: " + f.filename);
            }

            fis.read(fileMd5Bytes);

            // 资源文件解析
            for (int i = 0; i < count; i++) {
                File f = new File(decodePath, files.get(i).filename);
                if (!f.exists()) {
                    SZLog.i("文件绝对路径：" + f.getAbsolutePath());
                    f.createNewFile();
                }
                fos = new FileOutputStream(f);
                int len = -1;
                byte[] buffer = null;
                if (fileEnds[i] > bufferLength) {
                    int sum = (int) ((fileEnds[i] - fileMd5Bytes.length) / bufferLength);
                    buffer = new byte[bufferLength];
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        // fos.flush();
                        sum--;
                        if (sum == 0) {
                            buffer = new byte[(int) ((fileEnds[i] - fileMd5Bytes.length) %
                                    bufferLength)];
                            fis.read(buffer);
                            fos.write(buffer);
                            // fos.flush();
                            fis.read(fileMd5Bytes);
                            break;
                        }
                    }

                } else {
                    buffer = new byte[(int) (fileEnds[i] - fileMd5Bytes.length)];
                    fis.read(buffer);
                    fos.write(buffer);
                    // fos.flush();
                    fis.read(fileMd5Bytes);
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            //} catch (FileNotFoundException e) {
            //    e.printStackTrace();
            //return null;
        } catch (IOException e) {
            e.printStackTrace();
            //return null;
        } finally {

            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return files;
    }

    /**
     * 新drm包解析(证书分离后)
     *
     * @param drmPath    被解析文件路径
     * @param decodePath 解析后保存文件路径
     */
    public static List<CommonFile> parserDRMFile2(String drmPath, String decodePath) {

        List<CommonFile> files = new ArrayList<CommonFile>();

        File drmFile = new File(drmPath);

        SZLog.i("parserDRM path: " + drmPath);

        if (!FileUtil.checkFilePathExists(drmPath)) {
            Log.i(TAG, "文件不存在");
            return null;
        }
        FileOutputStream fos = null;
        FileInputStream fis = null;
        byte[] eightBytes = new byte[8];
        byte[] fileMd5Bytes = new byte[32];
        byte[] headFileNameBytes = new byte[40];
        int bufferLength = 1024 * 1024;
        //文件包版本号
        byte[] drmVersionBytes = new byte[8];

        try {
            // 头文件解析
            fis = new FileInputStream(drmFile);

            fis.read(eightBytes);           //头偏移：0~7byte
            fis.read(drmVersionBytes);      //新包版本号: 8~15byte

            //DRM文件版本号，要入库
            String strDrmVersion = new String(drmVersionBytes).trim();
            SZLog.d(TAG, "DrmVersion: " + strDrmVersion);
            //TODO:文件版本号存储

            long headOffset = SecurityUtil.bytes2Long(eightBytes); //头偏移长度

            int fileCount = (int) ((headOffset - 16 - 32) / (headFileNameBytes.length
                    + eightBytes.length + eightBytes.length));

            for (int i = 0; i < fileCount; i++) {

                CommonFile commonFile = new CommonFile();
                fis.read(headFileNameBytes);
                String filename = new String(headFileNameBytes).trim();  //名称

                commonFile.filetype = getFileType(filename);
                commonFile.filename = filename;
                commonFile.filepath = decodePath + File.separator + filename;

                fis.read(eightBytes);
                commonFile.filestart = SecurityUtil.bytes2Long(eightBytes);    //文件起始
                fis.read(eightBytes);
                commonFile.filesize = SecurityUtil.bytes2Long(eightBytes);   //文件的长度

                files.add(commonFile);

                SZLog.d(TAG, "filename: " + commonFile.filename);
            }

            fis.read(fileMd5Bytes);

            // 资源文件解析
            for (int i = 0; i < fileCount; i++) {
                CommonFile file = files.get(i);
                if (file.filename.equalsIgnoreCase("Empty"))
                    continue;

                File f = new File(decodePath, file.filename);
                if (!f.exists()) {
                    SZLog.i("文件绝对路径：" + f.getAbsolutePath());
                    f.createNewFile();
                }
                fos = new FileOutputStream(f);
                int len = -1;
                byte[] buffer = null;
                if (file.filesize > bufferLength) {
                    int sum = (int) ((file.filesize - fileMd5Bytes.length) / bufferLength);
                    buffer = new byte[bufferLength];
                    while ((len = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                        // fos.flush();
                        sum--;
                        if (sum == 0) {
                            buffer = new byte[(int) ((file.filesize - fileMd5Bytes.length) %
                                    bufferLength)];
                            fis.read(buffer);
                            fos.write(buffer);
                            // fos.flush();
                            fis.read(fileMd5Bytes);
                            break;
                        }
                    }
                } else {
                    buffer = new byte[(int) (file.filesize - fileMd5Bytes.length)];
                    fis.read(buffer);
                    fos.write(buffer);
                    // fos.flush();
                    fis.read(fileMd5Bytes);
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            //} catch (FileNotFoundException e) {
            //    e.printStackTrace();
            //return null;
        } catch (IOException e) {
            e.printStackTrace();
            //return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //删除DRM包
            FileUtil.deleteFileWithPath(drmPath);
        }
        return files;
    }

    /*
     * 新drm包解析(证书分离后)
     *
     * <p>
     * 错误解析！
     *
     * @param drmPath    被解析文件路径
     * @param decodePath 解析后保存文件路径
     */
//    public static List<CommonFile> parserDRMFile2(String drmPath, String decodePath) {
//
//        List<CommonFile> files = new ArrayList<CommonFile>();
//
//        File drmFile = new File(drmPath);
//
//        if (!FileUtils.checkFilePathExists(drmPath)) {
//            Log.i(TAG, "文件不存在");
//            return null;
//        }
//        FileOutputStream fos = null;
//        FileInputStream fis = null;
//        byte[] eightBytes = new byte[8];
//        byte[] fileMd5Bytes = new byte[32];
//        byte[] headFileNameBytes = new byte[40];
//        int bufferLength = 1024 * 1024;
//
//        byte[] drmVersionBytes = new byte[8];
//
//        try {
//            // 头文件解析
//            fis = new FileInputStream(drmFile);
//
//            fis.read(eightBytes);           //头偏移：0~7byte
//            fis.read(drmVersionBytes);      //新包版本号: 8~15byte
//
//            //DRM文件版本号，要入库
//            String strDrmVersion = new String(drmVersionBytes).trim();
//            DRMLog.d(TAG, "DrmVersion:" + strDrmVersion);
//            //文件版本号存储
//
//            long headOffset = SecurityUtil.bytes2Long(eightBytes); //头偏移长度
//
//            int fileCount = (int) ((headOffset - 16 - 32) / (headFileNameBytes.length
//                    + eightBytes.length + eightBytes.length));
//
//            long[] fileStarts = new long[fileCount];
//            long[] fileOffsets = new long[fileCount];
//            for (int i = 0; i < fileCount; i++) {
//                CommonFile commonFile = new CommonFile();
//                fis.read(headFileNameBytes);
//                String filename = new String(headFileNameBytes).trim();  //名称
//
//                commonFile.filetype = getFileType(filename);
//                commonFile.filename = filename;
//                commonFile.filepath = decodePath + File.separator + filename;
//
//                fis.read(eightBytes);
//                fileStarts[i] = SecurityUtil.bytes2Long(eightBytes);    //文件起始
//                fis.read(eightBytes);
//                fileOffsets[i] = SecurityUtil.bytes2Long(eightBytes);   //文件的长度
//
//                if (filename.equalsIgnoreCase("Empty"))
//                    continue;
//
//                files.add(commonFile);
//
//                DRMLog.d(TAG, "filename: " + commonFile.filename);
//            }
//
//            fis.read(fileMd5Bytes);
//
//            // 资源文件解析
//            for (int i = 0; i < fileCount; i++) {
//
//                File f = new File(decodePath, files.get(i).filename);
//                if (!f.exists()) {
//                    DRMLog.i("文件绝对路径：" + f.getAbsolutePath());
//                    f.createNewFile();
//                }
//                fos = new FileOutputStream(f);
//                int len = -1;
//                byte[] buffer = null;
//                if (fileOffsets[i] > bufferLength) {
//                    int sum = (int) ((fileOffsets[i] - fileMd5Bytes.length) / bufferLength);
//                    buffer = new byte[bufferLength];
//                    while ((len = fis.read(buffer)) != -1) {
//                        fos.write(buffer, 0, len);
//                        // fos.flush();
//                        sum--;
//                        if (sum == 0) {
//                            buffer = new byte[(int) ((fileOffsets[i] - fileMd5Bytes.length) %
//                                    bufferLength)];
//                            fis.read(buffer);
//                            fos.write(buffer);
//                            // fos.flush();
//                            fis.read(fileMd5Bytes);
//                            break;
//                        }
//                    }
//
//                } else {
//                    buffer = new byte[(int) (fileOffsets[i] - fileMd5Bytes.length)];
//                    fis.read(buffer);
//                    fos.write(buffer);
//                    // fos.flush();
//                    fis.read(fileMd5Bytes);
//                }
//
//                if (fos != null) {
//                    try {
//                        fos.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            //return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//            //return null;
//        } finally {
//
//            if (fis != null) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return files;
//    }

    /*
     * 解析证书方法
     *
     * @param name 证书文件名
     * @return
     */
    public static OEX_Rights parserRight(File file) {
        String filePath = file.getPath();
        SZLog.i("parserRight path: " + filePath);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return readXML(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //删除Right.xml
            FileUtil.deleteFileWithPath(filePath);
        }
        return null;
    }

    /**
     * 新解析证书方法 （证书分离后）
     *
     * @param text 证书文件名
     * @return
     */
//    public static OEX_Rights parserRight2(String text) {
//        DRMLog.i("right: " + text);
//        //String filePath = PathUtil.getFilePrefixPath()+"/"+
//         //       File file = FileUtils.writeTextToFile(fileName, text);
//        FileInputStream fis = null;
//        try {
//            fis = new FileInputStream(file);
//            return readXML(fis);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            if (fis != null) {
//                try {
//                    fis.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }


    /**
     * 新解析albumInfo.xml (证书分离后)
     *
     * @param albumInfoFile albumInfo文件
     * @param list          CommonFile文件
     * @return
     */
    public static XML2JSON_Album parserAlbumInfo2(File albumInfoFile, List<CommonFile> list) {
        XML2JSON_Album albumInfo = null;
        FileInputStream reader = null;
        ByteArrayOutputStream baos = null;

        String tempPath = albumInfoFile.getPath();
        SZLog.i("parserAlbumInfo path: " + tempPath);
        int length = -1;
        try {
            albumInfo = new XML2JSON_Album();
            reader = new FileInputStream(albumInfoFile);
            baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while ((length = (reader.read(buffer))) != -1) {
                baos.write(buffer, 0, length);
            }
            //albumInfo中保存内容为加密过的byte[];
            String result = AESUtil.decrypt(baos.toByteArray(), Constant.ALBUMINFO_SECRET);
            // 将字符串转成JSON对象
            SZLog.d("albumInfo: " + result);
            JSONObject rootObj = new JSONObject(result);
            JSONObject contentNames = rootObj.getJSONObject("contentNames");

            ArrayList<String> al = new ArrayList<String>();
            for (int x = 0; x < list.size(); x++) {
                if (x > 1) {
                    //eg: 12566-ss5fgf2-fgd8fgd-522ghh.pdf
                    String fileName = list.get(x).filename.split("\\.")[0];
                    String contentName = contentNames.optString(fileName);
                    al.add("\"" + fileName + "\"");
                    al.add("\"" + contentName + "\"");
                }
            }
            // 对contentNames只能用字符串方式解析，因为key值不是固定的
            albumInfo.setContentList(al);
            albumInfo.setInfoList(parserJSONToArrayList(rootObj.getString("infos")));
            albumInfo.setInfoObj(rootObj.getJSONObject("infos"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //删除AlbumInfo.xml
            FileUtil.deleteFileWithPath(tempPath);
        }
        return albumInfo;
    }

    /*
     * 解析albumInfo.xml实际内容为JSON格式的字符
     *
     * @param albumInfoFile albumInfo文件
     * @param list          CommonFile文件
     * @return
     */
    @Deprecated
    public static XML2JSON_Album parserAlbumInfo(File albumInfoFile, List<CommonFile> list) {
        StringBuilder sb = new StringBuilder();
        XML2JSON_Album albumInfo = new XML2JSON_Album();
        FileInputStream reader = null;
        try {
            reader = new FileInputStream(albumInfoFile);
            byte[] buffer = new byte[1024];
            while ((reader.read(buffer)) != -1) {
                sb.append(new String(buffer));
            }

            // 将字符串转成JSON对象
            SZLog.d("albumInfo: " + sb.toString());
            JSONObject rootObj = new JSONObject(sb.toString());
            JSONObject contentNames = rootObj.getJSONObject("contentNames");

            ArrayList<String> al = new ArrayList<String>();
            for (int x = 0; x < list.size(); x++) {
                if (x > 1) {
                    String filename = list.get(x).filename.split("\\.")[0];
                    String contentname = contentNames.getString(filename);
                    al.add("\"" + filename + "\"");
                    al.add("\"" + contentname + "\"");
                }
            }
            // 对contentNames只能用字符串方式解析，因为key值不是固定的
            albumInfo.setContentList(al);
            albumInfo.setInfoList(parserJSONToArrayList(rootObj.getString("infos")));
            albumInfo.setInfoObj(rootObj.getJSONObject("infos"));

            // 将字符串转成JSON对象
            // JSONObject rootObj = new JSONObject(sb.toString());
            // 对contentNames只能用字符串方式解析，因为key值不是固定的
            // albumInfo.setContentList(DRMUtil.parserJSONToArrayList(rootObj.getString
            // ("contentNames")));
            // albumInfo.setInfoList(DRMUtil.parserJSONToArrayList(rootObj.getString("infos")));
            // albumInfo.setInfoObj(rootObj.getJSONObject("infos"));
            // System.out.println(albumInfo);

            //} catch (FileNotFoundException e) {
            //    e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return albumInfo;
    }

    /**
     * 获取文件类型
     *
     * @param fileName
     * @return
     */
    public static CommonFile.FILETYPE getFileType(String fileName) {
        String type = FileUtil.getExtFromFileName(fileName);
        if ("pdf".equalsIgnoreCase(type)) {
            return CommonFile.FILETYPE.PDF;
        } else if ("xml".equals(type)) {
            if (fileName.contains("albumInfo")) {
                return CommonFile.FILETYPE.ALBUMINFO;
            } else {
                return CommonFile.FILETYPE.RIGHT;
            }
        } else if ("mp3".equalsIgnoreCase(type)) {
            return CommonFile.FILETYPE.MP3;
        } else if ("mp4".equalsIgnoreCase(type)) {
            return CommonFile.FILETYPE.MP4;
        } else if ("drm".equals(type)) {
            return CommonFile.FILETYPE.DRM;
        }
        return CommonFile.FILETYPE.UNDEFINITION;
    }

    public static class CommonFile {
        public String filename;
        public String filepath;
        public FILETYPE filetype;
        public long filestart;
        public long filesize;

        @Override
        public String toString() {
            return "CommonFile{" +
                    "filename='" + filename + '\'' +
                    ", filepath='" + filepath + '\'' +
                    ", filetype=" + filetype +
                    ", filestart=" + filestart +
                    ", filesize=" + filesize +
                    '}';
        }

        public enum FILETYPE {
            DRM, RIGHT, PDF, MP3, MP4, TXT, ALBUMINFO, UNDEFINITION;// *
            // .drm文件，权限文件，各种资源文件，albumInfo文件
        }
    }

    /**
     * 解析contentNames 将contentNames对应的字符串中的所有双引号之内的内容添加到一个list中
     *
     * @param json
     */
    private static ArrayList<String> parserJSONToArrayList(String json) {

        json.replaceAll("\\{", "");
        json.replaceAll("\\}", "");
        json.replaceAll("\"", "");

        ArrayList<String> contents = new ArrayList<String>();
        Pattern p = Pattern.compile("\"(.*?)\"");
        Matcher m = p.matcher(json);
        while (m.find()) {
            contents.add(m.group().trim());
        }

        return contents;
    }

    /**
     * 解析证书xml的约束信息
     *
     * @param inputStream 文件流
     * @return
     */
    private static OEX_Rights readXML(InputStream inputStream) {
        boolean isFristUID = true;
        boolean isFristAssent = true;

        OEX_Rights right = null;
        OEX_Agreement agreement = null;
        OEX_Agreement.OEX_Asset asset = null;
        OEX_Agreement.OEX_Permission permission = null;
        Map<String, String> map = null;
        String value = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String startTag = parser.getName();
                if (eventType == XmlPullParser.START_TAG) {
                    switch (startTag) {
                        case "rights":
                            right = new OEX_Rights();
                            break;
                        case "version":
                            right.setContextMap("version", parser.nextText());
                            break;
                        case "uid":
                            if (isFristUID) {
                                right.setContextMap("uid", parser.nextText());
                                isFristUID = false;
                            } else {
                                asset.setOdd_uid(parser.nextText());
                            }
                            break;
                        case "agreement":
                            agreement = new OEX_Agreement();
                            break;
                        case "asset":
                            if (isFristAssent) {
                                asset = agreement.new OEX_Asset();
                            } else {
                                permission.setAssent_id(parser.getAttributeValue(0));
                            }
                            break;
                        case "DigestMethod":
                            asset.setDigest_algorithm_key(parser.getAttributeValue(0));
                            break;
                        case "DigestValue":
                            asset.setDigest_algorithm_value(parser.nextText());
                            break;
                        case "EncryptionMethod":
                            asset.setEnc_algorithm(parser.getAttributeValue(0));
                            break;
                        case "RetrievalMethod":
                            asset.setRetrieval_url(parser.getAttributeValue(0));
                            break;
                        case "CipherValue":
                            asset.setCipheralue(parser.nextText());
                            break;
                        case "permission":
                            permission = agreement.new OEX_Permission();
                            break;
                        case "display":
                        case "play":
                        case "print":
                        case "execute":
                        case "export":
                            permission.setType(getType(startTag));
                            break;
                        case "constraint":
                            map = new HashMap<String, String>();
                            break;
                        case "datetime":
                            // <o-dd:datetime o-dd:start="1448872142446"
                            // o-dd:end="1546531199000">1131</o-dd:datetime>
                            // String end = parser.getAttributeValue(0);
                            // String start = parser.getAttributeValue(1);
                            String startTime = parser.getAttributeValue(null, "start");
                            String endTime = parser.getAttributeValue(null, "end");
                            SZLog.d("PullXML", "startTime: " + startTime);
                            SZLog.d("PullXML", "endedTime: " + endTime);

                            permission.setStartTime(startTime);
                            permission.setEndTime(endTime);
                            value = parser.nextText();
                            permission.setDays(value);
                            map.put(startTag, value);
                            break;
                        case "individual":
                            value = parser.nextText();
                            permission.setIndividual(value);
                            map.put(startTag, value);
                            break;
                        case "system":
                            value = parser.nextText();
                            permission.setSystem(value);
                            map.put(startTag, value);
                            break;
                        case "accumulated":
                            value = parser.nextText();
                            permission.setAccumulated(value);
                            map.put(startTag, value);
                            break;
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    switch (startTag) {
                        case "asset":
                            if (isFristAssent && asset != null) {
                                agreement.setAssets(asset);
                                isFristAssent = false;
                            }
                            break;
                        case "constraint":
                            permission.setAttributes(map);
                            map = null;
                            break;
                        case "permission":
                            isFristAssent = true;
                            agreement.setPermission(permission);
                            break;
                        case "agreement":
                            right.setAgreement(agreement);
                            break;
                    }

                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return right;
    }


    /**
     * 权限类型
     */
    public static OEX_Agreement.PERMISSION_TYPE getType(String s)
    {
        switch (s) {
            case OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY:
                return OEX_Agreement.PERMISSION_TYPE.DISPLAY;
            case OEX_AGREEMENT_PERMISSION_TYPE_PLAY:
                return OEX_Agreement.PERMISSION_TYPE.PLAY;
            case OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE:
                return OEX_Agreement.PERMISSION_TYPE.EXECUTE;
            case OEX_AGREEMENT_PERMISSION_TYPE_EXPORT:
                return OEX_Agreement.PERMISSION_TYPE.EXPORT;
            case OEX_AGREEMENT_PERMISSION_TYPE_PRINT:
                return OEX_Agreement.PERMISSION_TYPE.PRINT;
        }
        return null;
    }

    /* 权限类型 */
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_DISPLAY = "display";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_PLAY = "play";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_PRINT = "print";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_EXECUTE = "execute";
    private static final String OEX_AGREEMENT_PERMISSION_TYPE_EXPORT = "export";

}
