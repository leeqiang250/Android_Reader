package com.qlk.util.media;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.qlk.util.base.BaseActivity;
import com.qlk.util.event.PathsEvent;
import com.qlk.util.global.GlobalTask;
import com.qlk.util.tool.Util.FileUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public abstract class QlkMedia {
    // 隐藏文件夹或者“.nomedia”
    private static final HashSet<String> UnExpectedDirs = new HashSet<>();

    // 搜索到的文件路径（按时间排序）
    protected final Vector<String> SortedPaths = new Vector<>();

    /**
     * 搜索系统数据库时的Uri
     */
    protected abstract Uri getMediaUri();

    /**
     * 要搜索的文件后缀（例如“.png”、“.pdf”），必须小写
     */
    protected abstract String[] getSupportTypes();

    /**
     * 该方法在UI线程中调用
     */
    protected abstract void onSearchFinished(boolean autoToast);

    /**
     * 返回一个所搜文件集合的复制品
     */
    public ArrayList<String> getQlkPaths() {
        return new ArrayList<String>(SortedPaths);
    }

    protected Context mContext;

    public QlkMedia(Context context) {
        this.mContext = context;
    }

    /**
     * path是否是以types的某一个type结尾的
     *
     * @param path
     * @param types
     * @return
     */
    public static boolean isSameType2(String path, String... types) {
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex != -1) {
            String suffix = path.substring(dotIndex).toLowerCase(Locale.US); // 类似“.pdf”
            for (String type : types) {
                if (suffix.equals(type)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * path是否是以本类所支持的某一个type结尾的（判断依据来自getSupportTypes()）
     *
     * @param path
     * @return
     */
    public boolean isSameType2(String path) {
        return isSameType2(path, getSupportTypes());
    }

    /**
     * 忽略隐藏文件、忽略“.nomedia”文件
     */
    public boolean isFromUnExpectedDir(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }

        synchronized (UnExpectedDirs) {
            for (String dir : UnExpectedDirs) {
                if (path.startsWith(dir)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 忽略隐藏文件、忽略“.nomedia”文件、忽略系统目录
     */
    private boolean isExpected(String path, boolean checkFile) {
        if (isFromUnExpectedDir(path) || TextUtils.isEmpty(path)) {
            return false;
        }

        File file = new File(path);
        /* 忽略隐藏文件夹 */
        String unExpectedDir = null;
        if (path.matches(".*/\\..*/.*")) {
            // 类似 .../.pyc/...
            unExpectedDir = FileUtil.getParentDir(path);
        }
        if (unExpectedDir == null && path.matches(".*/\\..*") && file.isDirectory()) {
            // 类似 .../.pyc同时pyc是个文件夹
            unExpectedDir = path;
        }
        // 忽略系统目录
        if (unExpectedDir == null && !QlkDirs.isOnDisk(path)) {
            unExpectedDir = file.isDirectory() ? path : FileUtil.getParentDir(path);
        }
        if (unExpectedDir == null && file.isDirectory() && !checkFile) {
            String[] names = file.list();
            if (names != null) {
                for (String name : names) {
                    if (name.equals(".nomedia")) {
                        unExpectedDir = path;
                        break;
                    }
                }
            }
        }
        if (unExpectedDir != null) {
            synchronized (UnExpectedDirs) {
                UnExpectedDirs.add(unExpectedDir);
            }
        }
        return unExpectedDir == null;
    }

	/*-**********************************
     * TODO 系统数据库相关
	 ***********************************/

    /**
     * 从数据库中搜索
     * <p>
     * (新版：揉合pbb文件和随知文件 的版本用这个方法)
     */
    public void searchFromSysDBNew() {
        GlobalTask.executeBackground(new Runnable() {
            @Override
            public void run() {
                String[] supports = getSupportTypes();
                int length = supports.length;
                String selection = "";
                String[] args = new String[length];
                for (int i = 0; i < length; i++) {
                    // 这里就只搜索小写结尾的，全搜太慢了
                    selection += (i == 0 ? "" : " or ") + MediaColumns.DATA
                            + " like ?";
                    args[i] = "%" + supports[i];
                }
                Cursor c = mContext.getContentResolver().query(getMediaUri(),
                        new String[]{MediaColumns.DATA}, selection, args,
                        MediaColumns.DATE_MODIFIED + " desc");
                List<String> paths = new ArrayList<>();
                if (c != null && c.moveToFirst()) {
                    do {
                        String path = c.getString(0);
                        if (isSameType2(path) && isExpected(path, true)) {
                            // 有些文件管理器删除文件后不会管系统数据库
                            File file = new File(path);
                            if (file.exists()) {
                                paths.add(path);
                            }
                        }
                    } while (c.moveToNext());
                    c.close();
                }
                afterSearchNew(paths);
            }
        });

    }

    /**
     * 从数据库中搜索
     */
    @Deprecated
    public void searchFromSysDB() {
        GlobalTask.executeBackground(new Runnable() {
            @Override
            public void run() {
                String[] supports = getSupportTypes();
                int length = supports.length;
                String selection = "";
                String[] args = new String[length];
                for (int i = 0; i < length; i++) {
                    // 这里就只搜索小写结尾的，全搜太慢了
                    selection += (i == 0 ? "" : " or ") + MediaColumns.DATA
                            + " like ?";
                    args[i] = "%" + supports[i];
                }
                Cursor c = mContext.getContentResolver().query(getMediaUri(),
                        new String[]{MediaColumns.DATA}, selection, args,
                        MediaColumns.DATE_MODIFIED + " desc");
                ArrayList<String> paths = new ArrayList<>();
                if (c != null && c.moveToFirst()) {
                    do {
                        String path = c.getString(0);
                        if (isSameType2(path) && isExpected(path, true)) {
                            // 有些文件管理器删除文件后不会管系统数据库
                            File file = new File(path);
                            if (file.exists()) {
                                paths.add(path);
                            }
                        }
                    } while (c.moveToNext());
                    c.close();
                }
                afterSearch(paths, false);
            }
        });

    }

    /**
     * 插入系统数据库
     */
    public void insertToSysDBAsync(final String... paths) {
        GlobalTask.executeBackground(new Runnable() {
            @Override
            public void run() {
                ContentResolver resolver = mContext.getContentResolver();
                ContentValues values = new ContentValues();
                for (String path : paths) {
                    Cursor c = resolver.query(getMediaUri(), null,
                            MediaColumns.DATA + "=?", new String[]{path},
                            null);
                    if (c == null || !c.moveToFirst()) {
                        values.put(MediaColumns.DATA, path);
                        resolver.insert(getMediaUri(), values);
                        //add 20171020
                        //resolver.notifyChange(getMediaUri(), null);
                    }

                    if (c != null) {
                        c.close();
                    }
                }
            }
        });
    }

	/*-*****************************************
     * TODO 搜索
	 ******************************************/

    private final ArrayList<SortPair> mSearchContainer = new ArrayList<>();
    private final ArrayList<String> mDeletePaths = new ArrayList<>(); // Deleted
    // paths
    // while
    // searching.
    //private Toast searchToast;
    private boolean isSearching = false;
    private final LinkedBlockingQueue<String> mWaitDirs = new LinkedBlockingQueue<>();
    //private ExecHandler searchHandler = new ExecHandler(this);
    private boolean findWorking = false;
    //private boolean showToast = false;
    //private boolean isDirFindingFinished = true;
    //private FindFileThread mThread1, mThread2, mThread3;

    /**
     * 搜索手机上的符合文件
     *
     * @return true 正在搜索中
     */
    public boolean search() {
        if (isSearching) {
            return true;
        }

//        initToast();

        mDeletePaths.clear();
        mSearchContainer.clear();

        startFindThread();

        return false;
    }

//    private void initToast() {
//        if (searchToast == null) {
//            searchToast = new Toast(mContext);
//            TextView txt = new TextView(mContext);
//            txt.setEllipsize(TruncateAt.MIDDLE);
//            txt.setSingleLine();
//            txt.setWidth(ScreenUtil.getScreenWidth(mContext));
//            txt.setTextColor(mContext.getResources().getColor(R.color.black));
//            searchToast.setView(txt);
//            searchToast.setGravity(Gravity.BOTTOM, 0, 0);
//        }
//    }

    private void startFindThread() {

        isSearching = true;

        GlobalTask.executeBackground(new FindThread());

        //new FindDirThread().start();

        //mThread1 = new FindFileThread();
        //mThread1.start();

        //mThread2 = new FindFileThread();
        //mThread2.start();

        //mThread3 = new FindFileThread();
        //mThread3.start();
    }

    private boolean finishFind() {
//        if ((mThread1 != null && mThread1.isAlive())
//              || (mThread2 != null && mThread2.isAlive())) {
//            return false;
//        }

        List<SortPair> container = mSearchContainer;
        ArrayList<String> paths = new ArrayList<>();
        if (!container.isEmpty()) {
            try {
                /* 排序 */
                Collections.sort(container);    //如果数据不合法，sort可能出错
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                for (SortPair pair : container) {
                    paths.add(pair.first);
                }

                insertToSysDBAsync(paths.toArray(new String[]{}));

                paths.removeAll(mDeletePaths);

//                afterSearch(paths, true); //20170908修改
                afterSearchNew(paths);

                mDeletePaths.clear();
                mSearchContainer.clear();
                mWaitDirs.clear();
                isSearching = false;
//                showToast = false;
            }
        } else {
            afterSearchNew(paths); //数据为空时候。

            isSearching = false;
        }
        //扫描到的路径为空
//        if (paths.isEmpty()) {
//            EventBus.getDefault().post(new PathsEvent(PathsEvent.P_NO_DATA));
//        }
//        paths.removeAll(mDeletePaths);
//        afterSearch(paths, true);
//        mDeletePaths.clear();
//        mSearchContainer.clear();
//        mWaitDirs.clear();
//        isSearching = false;
//        showToast = false;
        return true;
    }

    class SortPair extends Pair<String, Long> implements Comparable<SortPair> {

        SortPair(String first, Long second) {
            super(first, second);
        }

        @Override
        public int compareTo(SortPair another) {
            return (int) (another.second - this.second); // From big number to small.
        }

        @Override
        public String toString() {
            return "SortPair [first=" + first + ", second=" + second + "]";
        }
    }

    //新版揉合pbb文件和随之文件 用这个方法
    private void afterSearchNew(List<String> paths) {
        SortedPaths.clear();
        SortedPaths.addAll(paths);
        EventBus.getDefault().post(new PathsEvent(PathsEvent.P_PATH));
    }

    @Deprecated
    private void afterSearch(ArrayList<String> paths, final boolean autoToast) {
        SortedPaths.clear();
        SortedPaths.addAll(paths);
        BaseActivity.UIHandler.post(new Runnable() {
            @Override
            public void run() {
                onSearchFinished(autoToast);
            }
        });
    }

//    private void sendSearchMsg(String msg) {
//        Message message = Message.obtain();
//        message.obj = msg;
//        searchHandler.sendMessage(message);
//    }

//    private static Handler searchHandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (searchToast != null) {
//                searchToast.cancel();
//                ((TextView) searchToast.getView()).setText((CharSequence) (msg.obj));
//                searchToast.show();
//            }
//        }
//    };

//    private static class ExecHandler extends Handler {
//        private WeakReference<QlkMedia> reference;
//
//        private ExecHandler(QlkMedia activity) {
//            reference = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            QlkMedia ref = reference.get();
//            if (ref == null) return;
//            if (ref.searchToast == null) ref.initToast();
//            ((TextView) ref.searchToast.getView()).setText((CharSequence) (msg.obj));
//            if (!ref.showToast) {
//                ref.searchToast.show();
//                ref.showToast = true;
//            }
//        }
//    }

    //搜目录，搜文件
    private class FindThread extends Thread {
        @Override
        public void run() {
            super.run();
            //目录 sdcard: eg: /storage/emulated/0下
            ArrayList<String> boots = QlkDirs.getCardsPaths();
            for (String boot : boots) {
                if (boot != null) {
                    mWaitDirs.offer(boot);
                    new File(boot).listFiles(dirFilter);
                }
            }
            //文件
            findWorking = true;
            ArrayList<SortPair> container = mSearchContainer;
            try {
                while (findWorking && !isInterrupted()) {
                    String dir = mWaitDirs.poll(50, TimeUnit.MILLISECONDS);
                    if (dir != null) {
                        // sendSearchMsg(dir); //update
                        Log.d("dir", "dir：" + dir);
                        File[] files = new File(dir).listFiles(fileFilter);
                        if (files != null && files.length > 0) {
                            for (File file : files) {
                                if (file != null && file.length() > 0) {
                                    container.add(new SortPair(file.getAbsolutePath(),
                                            file.lastModified()));
                                }
                            }
                        }
                    } else {
                        Log.w("FindThread", "poll value maybe null");
                        findWorking = false;
                        interrupt();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                finishFind();
            }
            isSearching = false;
        }
    }

    /*
     * 搜索目录
     */
//    class FindDirThread extends Thread {
//
//        @Override
//        public void run() {
//            super.run();
//            isDirFindingFinished = false;
//
//            ArrayList<String> boots = QlkDirs.getCardsPaths();
//            for (String boot : boots) {
//                if (boot != null) {
//                    mWaitDirs.offer(boot);
//                    new File(boot).listFiles(dirFilter);
//                }
//            }
//
//            isDirFindingFinished = true;
//
//            // 查找完后监督搜索进度
//            while (true) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                if (finishFind()) {
//                    break;
//                }
//            }
//            isSearching = false;
//        }
//    }

    /*
     * 搜索文件的线程
     *
     * @author QiLiKing 2015-7-30 下午2:40:13
     */
//    class FindFileThread extends Thread {
//
//        @Override
//        public void run() {
//            super.run();
//            ArrayList<SortPair> container = mSearchContainer;
//            while (true) {
//                try {
//                    String dir = mWaitDirs.poll(1, TimeUnit.SECONDS);
//                    if (dir != null) {
//                        sendSearchMsg(dir);
//                        File[] files = new File(dir).listFiles(fileFilter);
//                        if (files != null && files.length > 0) {
//                            for (File file : files) {
//                                if (file != null && file.length() > 0) {
//                                    container.add(new SortPair(file.getAbsolutePath(), file
//                                            .lastModified()));
//                                }
//                            }
//                        }
//                    } else {
//                        if (isDirFindingFinished) {
//                            break;
//                        }
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * 文件夹过滤器
     */
    private final FileFilter dirFilter = new FileFilter() {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                String dir = file.getAbsolutePath();
                if (isExpected(dir, false)) {
                    mWaitDirs.offer(dir);
                    file.listFiles(dirFilter);
                }
            }
            return false;
        }
    };

    /**
     * 符合条件的文件过滤器
     */
    private final FileFilter fileFilter = new FileFilter() {
        @Override
        public boolean accept(File file) {
            if (file.isFile()) {
                String path = file.getAbsolutePath();
                if (isSameType2(path)) {
                    return true;
                }
            }
            return false;
        }
    };

	/*-***************************************
     * TODO 删除
	 ****************************************/

    /**
     * @param delPaths
     * @return Real deleted path
     */
    public ArrayList<String> delete(String... delPaths) {
        final ContentResolver resolver = mContext.getContentResolver();
        final ArrayList<String> del = new ArrayList<>();
        ArrayList<String> mDel = mDeletePaths;
        for (String path : delPaths) {
            if (FileUtil.deleteFile(path)) {
                del.add(path);
                mDel.add(path);
            }
        }
        GlobalTask.executeBackground(new Runnable() {
            @Override
            public void run() {
                for (String path : del) {
                    resolver.delete(getMediaUri(), MediaColumns.DATA + "=?",
                            new String[]{path});
                }
            }
        });
        return del;
    }
}
