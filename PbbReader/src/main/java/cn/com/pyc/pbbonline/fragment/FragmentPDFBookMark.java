package cn.com.pyc.pbbonline.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.sz.mobilesdk.database.bean.Bookmark;
import com.sz.mobilesdk.database.practice.BookmarkDAOImpl;
import com.sz.mobilesdk.util.ConvertToUtil;
import com.sz.mobilesdk.util.DeviceUtil;
import com.sz.mobilesdk.util.UIHelper;

import java.util.List;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.MuPdf_Mark_Adapter;
import cn.com.pyc.pbbonline.bean.event.MuPDFBookMarkDelEvent;
import cn.com.pyc.pbbonline.util.ViewHelp;
import de.greenrobot.event.EventBus;

/**
 * 书签
 */
public class FragmentPDFBookMark extends BaseMupdfFragment {

    public static final String KEY_CONTENT_ID = "key_content_id";
    private ListView label_listview;
    private View emptyView;
    private List<Bookmark> bookmarkList;
    private MuPdf_Mark_Adapter mlaAdapter;
    private Dialog dialog;
    private String[] fieldValues;

    private String contentId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null)
            contentId = bundle.getString(FragmentPDFBookMark.KEY_CONTENT_ID);
    }

    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.pbbonline_fragment_pdf_bookmark);
        List<Bookmark> bookmarks = getData();
        label_listview = (ListView) findViewById(R.id.label_listview);
        emptyView = findViewById(R.id.empty_include);
        initAdapter(getActivity().getLayoutInflater(), bookmarks);
        label_listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bookmark bookmark = bookmarkList.get(position);
                String pagefew = bookmark.getPagefew();
                int pageInt = ConvertToUtil.toInt(pagefew);
                Intent mIntent = new Intent();
                mIntent.putExtra("page", pageInt);
                getActivity().setResult(Activity.RESULT_OK, mIntent);
                getActivity().finish();
            }
        });

        label_listview.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
                    id) {
                final Bookmark bookmark = bookmarkList.get(position);
                return showEditSelectDialog(bookmark);
            }
        });
    }

    /**
     * @param bookmark 编辑的数据源对象
     * @return
     */
    private boolean showEditSelectDialog(final Bookmark bookmark) {
        View dialogView = getActivity().getLayoutInflater().inflate(R.layout
                .pbbonline_dialog_pdf_lable, null);
        View delete_bookmark = dialogView.findViewById(R.id.delete_bookmark);
        View empty_bookmark = dialogView.findViewById(R.id.clear_bookmark);
        View edit_bookmark = dialogView.findViewById(R.id.edit_bookmark);
        // 编辑书签
        edit_bookmark.setOnClickListener(new OnClickListener() {
            private AlertDialog dlg;

            public void onClick(View v) {
                dismiss();
                // 自定义内容的对话框
                AlertDialog.Builder builder = new Builder(getActivity());
                View view = getActivity().getLayoutInflater().inflate(
                        R.layout.pbbonline_dialog_pdf_lable_edit, null);
                final EditText edit_content = (EditText) view.findViewById(R.id.edit_content);
                Button edit_btn_positive = (Button) view
                        .findViewById(R.id.dialog_edit_btn_positive);
                Button edit_btn_negative = (Button) view
                        .findViewById(R.id.dialog_edit_btn_negative);
                edit_content.setText(bookmark.getContent());
                edit_content.setSelection(bookmark.getContent().length());

                edit_btn_positive.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        String edit_text = edit_content.getText().toString();
                        bookmark.setContent(edit_text);
                        BookmarkDAOImpl.getInstance().update(bookmark);

                        mlaAdapter.notifyDataSetChanged();
                        if (dlg != null) {
                            dlg.dismiss();
                        }
                    }
                });
                edit_btn_negative.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (dlg != null) {
                            dlg.dismiss();
                        }
                    }
                });
                dlg = builder.create();
                dlg.setView(view, 0, 0, 0, 0);
                dlg.setCanceledOnTouchOutside(false);
                dlg.setCancelable(false);
                dlg.show();
            }
        });
        // 删除书签
        delete_bookmark.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                dismiss();

                //Bookmark
                BookmarkDAOImpl.getInstance().deleteEntityByIdAndName(bookmark.getId(),
                        Bookmark.class.getSimpleName());
                bookmarkList.remove(bookmark);
                mlaAdapter.notifyDataSetChanged();

                // 改变当前页的添加书签按钮的状态
                EventBus.getDefault().post(new MuPDFBookMarkDelEvent());

                UIHelper.showToast(getApplicationContext(), "删除成功");
            }
        });
        // 清空书签
        empty_bookmark.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dismiss();
                BookmarkDAOImpl.getInstance().DeleteBookMark(fieldValues[0]);
                bookmarkList.clear();
                mlaAdapter.notifyDataSetChanged();
                showEmptyView();
                // 改变当前页的添加书签按钮的状态
                EventBus.getDefault().post(new MuPDFBookMarkDelEvent());
                UIHelper.showToast(getApplicationContext(), "已清空全部书签");
            }
        });
        dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
        dialog.setContentView(dialogView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        window.setWindowAnimations(R.style.bottom_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = DeviceUtil.getScreenSize(getActivity()).y;
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        dialog.onWindowAttributesChanged(wl);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return true;
    }

    private void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void initAdapter(LayoutInflater mInflater, List<Bookmark> bookmarkList) {
        if (bookmarkList == null || bookmarkList.isEmpty()) {
            showEmptyView();
            return;
        }
        if (mlaAdapter == null) {
            mlaAdapter = new MuPdf_Mark_Adapter(mInflater, bookmarkList);
            label_listview.setAdapter(mlaAdapter);
        } else {
            mlaAdapter.setData(bookmarkList);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Bookmark> getData() {
        String[] fields = new String[]{"content_ids"};
        fieldValues = new String[]{contentId};
        return bookmarkList = (List<Bookmark>) BookmarkDAOImpl.getInstance().findByQuery(fields,
                fieldValues, Bookmark.class);
    }

    private void showEmptyView() {
        ViewHelp.setEmptyViews(label_listview, emptyView,
                getString(R.string.have_read_add_bookmark));
    }

}
