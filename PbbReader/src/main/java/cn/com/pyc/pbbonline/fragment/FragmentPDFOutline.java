package cn.com.pyc.pbbonline.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.artifex.mupdfdemo.OutlineItem;

import java.util.List;

import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.pbbonline.adapter.MuPdf_Outline_Adapter;
import cn.com.pyc.pbbonline.common.K;
import cn.com.pyc.pbbonline.util.ViewHelp;

/**
 * 目录
 */
public class FragmentPDFOutline extends BaseMupdfFragment {

    public static final String KEY_OUTLINES = "key_outlines";

    private List<OutlineItem> outlineList;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null)
            this.outlineList = (List<OutlineItem>) bundle.getSerializable(KEY_OUTLINES);
    }

    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.pbbonline_fragment_pdf_outline);
        ListView lv = (ListView) findViewById(R.id.outline_listview);
        View emptyView = findViewById(R.id.empty_include);
        if (null == outlineList || outlineList.isEmpty()) {
            ViewHelp.setEmptyViews(lv, emptyView, getString(R.string.have_not_outline));
            return;
        }
        final MuPdf_Outline_Adapter lineAdapter = new MuPdf_Outline_Adapter(getActivity(),
                outlineList);
        lv.setAdapter(lineAdapter);
        lv.setSelection(K.OUTLINE_POSITION != 0 ? K.OUTLINE_POSITION - 1 : K.OUTLINE_POSITION);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mIntent = new Intent();
                mIntent.putExtra("page", lineAdapter.getItem(position).page);
                getActivity().setResult(Activity.RESULT_OK, mIntent);
                getActivity().finish();
                //UIHelper.finishOutAnim(getActivity());
            }
        });
    }

    public void onDestroyView() {
        super.onDestroyView();
    }
}
