package cn.com.pyc.media;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFActivity;
import com.qlk.util.base.BaseFragment;
import com.qlk.util.global.GlobalToast;
import com.qlk.util.media.ISelection.ISelectListener;
import com.qlk.util.widget.PullRefreshView;
import com.qlk.util.widget.PullRefreshView.OnRefreshListener;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cn.com.pyc.pbb.R;
import cn.com.pyc.global.GlobalData;
import cn.com.pyc.global.GlobalIntentKeys;
import cn.com.pyc.reader.ExtraImageReaderActivity;
import cn.com.pyc.reader.ExtraVideoPlayerActivity;
import cn.com.pyc.reader.music.MusicPlayerActivity;
import cn.com.pyc.sm.PayLimitConditionActivity;
import cn.com.pyc.user.Pbb_Fields;
import cn.com.pyc.xcoder.XCodeView;
import cn.com.pyc.xcoder.XCodeView.XCodeType;

/**
 * It works for "TAG_CIPHER_IMAGE", "TAG_CIPHER_FILE" and "TAG_CIPHER_VIDEO". <br>
 * It's similar to "CipherBaseFragment".
 *
 * @author QiLiKing 2015-8-18 上午11:32:45
 */

/**
 * @author 李巷阳
 * @version V1.0
 * @Description: (明文文件选择界面包括图片, 视频, 音频, 文档)
 * @date 2016/11/30 11:26
 * 其中图片和视频采用相同的布局,音频和文档采用相同的布局。
 * 通过AbsListView mAbsView 适配。
 * 通过ImageSort.getSort().get(imageFolder)获取图片时获取文件夹的子图片的方法。imageFolder：为图片上界面发送过来的文件夹id，也是文件夹显示的图片path.
 * 通过mType.instance(getActivity()).getCopyPaths(false);获取其他类型的所有path路径。type相当于GlobalData.Music。
 * goToReadView(int position)方法：多媒体文件的跳转。
 * sendSm();方法：发送文件。每次只能发送一个文件。
 *
 */
public class PlainBaseFragment extends BaseFragment implements OnClickListener {
    private GlobalData mType;
    private String imageFolder;    //used by image type
    private final ArrayList<String> mPaths = new ArrayList<>();
    private HashMap<String, String> mFileTimes = new HashMap<>();
    private MediaBaseAdapter mAdapter;
    private PullRefreshView mPullRefreshLayout;
    private AbsListView mAbsView;
    private View mEmptyLayout;
    private ViewStub mEmptyStub;
    private View mDateChosenLayout;
    private TextView mDateView;
    private TextView mChosenView;
    private View mBottomLayout;
    private ImageButton mEncryptView;
    private Button mSmView;

    private boolean isOverflowOccured = false;

    @Override
    protected boolean isObserverEnabled() {
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init_value();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_plain_base, container, false);
        init_view(v);
        init_listener(v);
        initUI();
        refreshUI();
        return v;
    }

    /**
     * @author 李巷阳
     * @date 2016/11/30 11:44
     */
    private void init_view(View v) {
        mDateChosenLayout = v.findViewById(R.id.fpb_lyt_date_chosen);// 日期与已选择的title栏。
        mDateView = (TextView) mDateChosenLayout.findViewById(R.id.idc_txt_date);// 日期
        mChosenView = (TextView) mDateChosenLayout.findViewById(R.id.idc_txt_chosen);// 已经选择的数量
        mPullRefreshLayout = (PullRefreshView) v.findViewById(R.id.pull_down_refresh);// 下拉刷新
        // 判断
        // 如果是图片或者是视频则调用R.id.fpb_grv_data的布局。
        // 如果是音频或者是文档则调用R.id.fpb_lsv_data的布局。
        int id = mType.equals(GlobalData.Image) || mType.equals(GlobalData.Video) ? R.id.fpb_grv_data : R.id.fpb_lsv_data;
        // AbsListView是Listview和GridView的父类。
        mAbsView = (AbsListView) v.findViewById(id);
        // 空空如也的图片。
        mEmptyStub = (ViewStub) v.findViewById(R.id.fpb_lyt_empty);
        // 点击选择出现发送按钮。
        mBottomLayout = v.findViewById(R.id.fpb_lyt_bottom);
        // 发送或者加锁的两个图片。
        // 如明文则显示发送图片。
        // 如密文则显示加锁图片。
        mEncryptView = (ImageButton) mBottomLayout.findViewById(R.id.fpb_imb_encrypt);
        mSmView = (Button) mBottomLayout.findViewById(R.id.fpb_imb_sm);
    }

    /**
     * @author 李巷阳
     * @date 2016/11/30 11:44
     */
    private void init_listener(View v) {
        //刷新按钮
        v.findViewById(R.id.ipt_imb_refresh).setOnClickListener(this);
        mEncryptView.setOnClickListener(this);
        mSmView.setOnClickListener(this);
        // 上下拉刷新
        mPullRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mType.instance(getActivity()).search(false);
            }
        });
        // 子item的点击事件
        mAbsView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 如果是图片,则选中状态。
                // 如果是其他文件类型,则跳转播放器界面。
                if (mType.equals(GlobalData.Image)) {
                    mAdapter.setItemSelected(position);
                } else {
                // 点击多媒体文件的跳转不同的播放器。
                    goToReadView(position);
                }
            }
        });
        // 滑动事件,用于刷新日期的动态显示refreshFileTime();
        mAbsView.setOnScrollListener(new OnScrollListener() {
            private int scrollState = SCROLL_STATE_IDLE;
            private boolean isFirstScroll = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.scrollState = scrollState;
                if (scrollState == SCROLL_STATE_IDLE) {
                    refreshFileTime();
                }
                // The adapter will refresh thumbs when user press and scroll.
                mAdapter.changeScrollState(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    refreshFileTime();
                }
                if (isFirstScroll && visibleItemCount > 0) {
                    isFirstScroll = false;
                    mAdapter.refresh(view);
                }
            }
        });
    }


    /**
     * @author 李巷阳
     * @date 2016/11/30 11:34
     */
    private void init_value() {
        // 获取文件的类型,如果是图片,则可从图片的文件界面,获取图片的文件夹显示的图片path.
        String type = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_TYPE);
        switch (type) {
            case Pbb_Fields.TAG_PLAIN_IMAGE:
                mType = GlobalData.Image;
                imageFolder = getArguments().getString(GlobalIntentKeys.BUNDLE_DATA_FOLDER);
                break;
            case Pbb_Fields.TAG_PLAIN_FILE:
                mType = GlobalData.Pdf;
                break;
            case Pbb_Fields.TAG_PLAIN_VIDEO:
                mType = GlobalData.Video;
                break;
            case Pbb_Fields.TAG_PLAIN_MUSIC:
                mType = GlobalData.Music;
                break;
            default:
                break;
        }
    }

    @Override
    protected void initUI() {
        // 获取是明文还是密文。isFromSm~true:为隐私文件,false为加密文件过来。
        boolean isFromSm = getActivity().getIntent().getBooleanExtra(GlobalIntentKeys.BUNDLE_FLAG_FORM_SM, false);
        // 锁图片是否隐藏。
        mEncryptView.setVisibility(isFromSm ? View.GONE : View.VISIBLE);
        // 发送图片是否显示。
        mSmView.setVisibility(isFromSm ? View.VISIBLE : View.GONE);
        // 适配器显示。
        mAbsView.setVisibility(View.VISIBLE);
        // 获取适配器
        mAdapter = getAdapter();
        // 刷新
        mAdapter.setSelectable(true);    //Plain fragments don't hide the "CheckBox".
        // 设置
        mAbsView.setAdapter(mAdapter);
        // 适配器选中状态改变。
        mAdapter.setSelectListener(new ISelectListener() {
            @Override
            public void onSelcetChanged(boolean overflow, int total, boolean allSelected) {
                // 判断是否超出可用的空间,从adapter适配器中回调。
                if (overflow) {
                    if (!isOverflowOccured) {
                        isOverflowOccured = true;
                        GlobalToast.toastShort(getActivity(), "所选文件超出可用空间");
                        changeEncryptBtnClickable(false);
                        changeSmBtnClickable(false);
                    }
                } else {
                    if (isOverflowOccured) {
                        isOverflowOccured = false;
                        changeEncryptBtnClickable(true);
                        changeSmBtnClickable(true);
                    }
                }
                // 选中,动态显示已经选择的个数。
                if (total > 0) {
                    // 显示发送的布局和已经选择的数量。
                    mChosenView.setVisibility(View.VISIBLE);
                    mChosenView.setText("已选择" + total + "个");
                    mBottomLayout.setVisibility(View.VISIBLE);
                } else {
                    // 隐藏发送的布局和已经选择的数量。
                    mChosenView.setVisibility(View.GONE);
                    mBottomLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * @Description: (获取所有文件并进行更新的方法。)
     * @author 李巷阳
     * @date 2016/11/30 12:02
     */
    @Override
    public void refreshUI() {
        mPullRefreshLayout.setRefreshComplete();
        mPaths.clear();
        // 获取类型对应的文件。
        mPaths.addAll(getPaths());
        // 如果paths为空，则日期与已选择的title栏隐藏，空空如也显示。
        // 负责相反。
        if (mPaths.isEmpty()) {
            mDateChosenLayout.setVisibility(View.INVISIBLE);
            if (mEmptyLayout == null) {
                mEmptyLayout = mEmptyStub.inflate();
            }
            mEmptyLayout.setVisibility(View.VISIBLE);
        } else {
            mDateChosenLayout.setVisibility(View.VISIBLE);
            if (mEmptyLayout != null) {
                mEmptyLayout.setVisibility(View.GONE);
            }
            refreshFileTime();
        }
        // 初始化后,发送栏隐藏。
        mBottomLayout.setVisibility(View.GONE);
        // 所有选中的文件都初始化为未选中。
        mAdapter.clearSelected();
        // 加载缩略图。
        mAdapter.refresh(mAbsView);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 刷新
            case R.id.ipt_imb_refresh:
                mType.instance(getActivity()).search(false);
                break;
            // 隐私文件加密
            case R.id.fpb_imb_encrypt:
                new XCodeView(getActivity()).startXCode(XCodeType.Encrypt, null, true, getCheckedPaths().toArray(new String[]{}));
                break;
            // 发送
            case R.id.fpb_imb_sm:
                sendSm();
                break;

            default:
                break;
        }
    }

    /**
     * @Description: (如果为图片, 则通过文件夹的path，获取文件夹的所有子图片的路径path.)
     * @author 李巷阳
     * @date 2016/11/30 12:20
     */
    private ArrayList<String> getPaths() {
        if (mType.equals(GlobalData.Image) && imageFolder != null) {
            ArrayList<String> paths = ImageSort.getSort().get(imageFolder);
            //If the sort-task is not finished, paths may be null.
            if (paths == null) {
                paths = new ArrayList<>();
            }
            return paths;
        } else {
            return mType.instance(getActivity()).getCopyPaths(false);
        }
    }
    /**   
    *
    * @Description: (发送,每次只能发送一个安全文件)
    * @author 李巷阳
    * @date 2016/11/30 12:22 
    */
    private void sendSm() {
        ArrayList<String> pathSend = getCheckedPaths();
        if (pathSend.size() > 1) {
            GlobalToast.toastShort(getActivity(), "每次只能发送一个安全文件");
            return;
        }
//        Intent intent = new Intent(getActivity(), ChooseSMwayActivity.class);
        Intent intent = new Intent(getActivity(), PayLimitConditionActivity.class);
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, pathSend.get(0));// 发送需要传播的文件路径
        startActivity(intent);
    }
    // 获取所有选中的path
    private ArrayList<String> getCheckedPaths() {
        return new ArrayList<>(mAdapter.getSelected());
    }
    // 判断如果超出文件可用空间，就显示不可加密图片。
    private void changeEncryptBtnClickable(boolean clickable) {
        mEncryptView.setClickable(clickable);
        mEncryptView.setBackgroundResource(clickable ? R.drawable.xml_encrypt : R.drawable.imb_encrypt_disabled);
    }
    // 判断如果超出文件可用空间，就显示不可发送图片。
    private void changeSmBtnClickable(boolean clickable) {
        mSmView.setClickable(clickable);
        mSmView.setBackgroundResource(clickable ? R.drawable.xml_makesm : R.drawable.sm_disabled);
    }

    // 不同的文件类型,对应着不同的适配器。
    // 音频和文档对应着相同的适配器。
    private MediaBaseAdapter getAdapter() {
        MediaBaseAdapter adapter;
        switch (mType) {
            case Image:
                adapter = new PlainImageAdapter(getActivity(), mPaths);
                break;

            case Video:
                adapter = new PlainVideoAdapter(getActivity(), mPaths);
                break;

            default:
                adapter = new MediaListAdapter(getActivity(), mPaths);
                break;
        }
        return adapter;
    }
    /**   
    * @Description: (刷新文件的时间)
    * @author 李巷阳
    * @date 2016/11/30 12:20 
    */
    private void refreshFileTime() {
        if (mPaths.size() > 0) {
            String firstFilePath = mPaths.get(mAbsView.getFirstVisiblePosition());
            String firstFileTime = mFileTimes.get(firstFilePath);
            if (firstFileTime == null) {
                firstFileTime = DateFormat.getDateInstance().format(new Date(new File(firstFilePath).lastModified()));
                mFileTimes.put(firstFilePath, firstFileTime);
            }

            mDateView.setText(firstFileTime);
        }
    }
    /**   
    * @Description: (点击文件进行跳转.图片不进行跳转)
    * @author 李巷阳
    * @date 2016/11/30 12:20 
    */
    private void goToReadView(int position) {
        Class<?> clas = null;
        switch (mType) {
            case Image:
                clas = ExtraImageReaderActivity.class;        //never use
                break;
            case Video:
                clas = ExtraVideoPlayerActivity.class;
                break;
            case Pdf:
                clas = MuPDFActivity.class;
                break;
            case Music:
                clas = MusicPlayerActivity.class;
                break;

            default:
                break;
        }
        Intent intent = new Intent(getActivity(), clas);
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATH, mPaths.get(position));
        intent.putExtra(GlobalIntentKeys.BUNDLE_DATA_PATHS, new ArrayList<>(mPaths));
        intent.putExtra(GlobalIntentKeys.BUNDLE_FLAG_CIPHER, false);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter.exit();    //Create in "onCreateView" and exit in "onDestroyView".
    }

}
