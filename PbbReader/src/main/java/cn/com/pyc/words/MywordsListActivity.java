package cn.com.pyc.words;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.com.pyc.bean.PhoneInfo;
import cn.com.pyc.pbb.reader.R;
import cn.com.pyc.utils.ExampleUtil;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.qlk.util.global.GlobalTask;

public class MywordsListActivity extends Activity implements OnItemClickListener
{
	public static final String NEW_NUMS = "nums";

	private List listTitles = new ArrayList();
	private List listTimes = new ArrayList();
	private List listUrls = new ArrayList();
	private List<Map<String, Object>> listItems;
	private Handler mHandler;
	private TextView tv_title;
	private PullToRefreshListView mPullRefreshListView;
	private SimpleAdapter simpleAdapter;
	// private int y = 1;
	private int n = 10;
	private int num;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mywords);
		tv_title = (TextView) findViewById(R.id.am_txt_title);
		tv_title.setText("精品博文");

		num = getIntent().getIntExtra(NEW_NUMS, 0);

		listItems = new ArrayList<Map<String, Object>>();
		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.am_listview);
		mPullRefreshListView.setMode(Mode.BOTH);

		GlobalTask.executeBackground(new Runnable()
		{

			@Override
			public void run()
			{
				getDataFromNet(n);
			}
		});

		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				super.handleMessage(msg);

				if (msg.what == 1)
				{
					mPullRefreshListView.onRefreshComplete();
					return;
				}

				MyJSONObj obj1 = (MyJSONObj) msg.obj;
				for (int i = 0; i < listTitles.size(); i++)
				{
					Map<String, Object> listItem = new HashMap<String, Object>();
					listItem.put("Title", obj1.title.get(i));
					listItem.put("Time", obj1.time.get(i));
					listItem.put("Url", obj1.url.get(i));
					listItems.add(listItem);
				}

				simpleAdapter = new SimpleAdapter(MywordsListActivity.this, listItems,
						R.layout.adapter_mywords, new String[]
						{ "Title", "Time" }, new int[]
						{ R.id.tv_mywords_title, R.id.tv_mywords_time });

				mPullRefreshListView.setAdapter(simpleAdapter);
			}

		};

		mPullRefreshListView.setOnItemClickListener(this);

		mPullRefreshListView.setOnRefreshListener(new OnRefreshListener()
		{

			@Override
			public void onRefresh(PullToRefreshBase refreshView)
			{
				// 下拉刷新
				if (mPullRefreshListView.isHeaderShown())
				{
					mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("下拉刷新");
					mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel(
							"正在刷新...");
					mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("松开刷新");
					GlobalTask.executeBackground(new Runnable()
					{

						@Override
						public void run()
						{
							getDataFromNet(n);
							mHandler.sendEmptyMessage(1);
						}
					});

					System.out.println("下拉刷新-------------------------------------------");

				}
				// 上拉加载
				else if (mPullRefreshListView.isFooterShown())
				{
					mPullRefreshListView.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载");
					mPullRefreshListView.getLoadingLayoutProxy(false, true).setRefreshingLabel(
							"正在加载...");
					mPullRefreshListView.getLoadingLayoutProxy(false, true).setReleaseLabel("松开加载");
					GlobalTask.executeBackground(new Runnable()
					{

						@Override
						public void run()
						{
							if (num >= n + 10)
							{

								getDataFromNet(n + 10);
								n = n + 10;
							}
							else
							{
								getDataFromNet(n + (num - n));
								n = n + (num - n);
							}

							/*
							 * if (num > 0)
							 * {
							 * y++;
							 * getDataFromNet(y);
							 * num = num - 10;
							 * }
							 * else
							 * {
							 * getDataFromNet(1);
							 * num = getIntent().getExtras().getInt(
							 * "NUMS");
							 * y = 1;
							 * }
							 */
							mHandler.sendEmptyMessage(1);
						}
					});
					System.out.println("上拉加载更多。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。");
				}
			}
		});

	}

	private void getDataFromNet(int n)
	{
		String url = "http://api.pyc.com.cn/api/v1/article?pageindex=1" + "&pagesize=" + n;
		try
		{
			// 第一步，创建HttpGet对象
			HttpGet httpGet = new HttpGet(url);
			// 第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 200)
			{
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());
				System.out.println(result);

				try
				{
					JSONArray jarray = new JSONArray(result);
					listItems.clear();
					listTimes.clear();
					listTitles.clear();
					listUrls.clear();
					for (int i = 0; i < jarray.length(); i++)
					{
						JSONObject jobj = jarray.getJSONObject(i);
						listTitles.add(jobj.get("Title"));
						listTimes.add(jobj.get("Addtime"));
						listUrls.add("http://www.pyc.com.cn" + jobj.get("Url") + "&mode="
								+ PhoneInfo.getDeviceID(getApplicationContext()));

					}
					MyJSONObj mjson = new MyJSONObj();
					mjson.title = listTitles;
					mjson.time = listTimes;
					mjson.url = listUrls;

					Message msg = new Message();
					msg.obj = mjson;
					mHandler.sendMessage(msg);

				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		startActivity(new Intent(MywordsListActivity.this, WebWordsActivity.class).putExtra(
				"wordsUrl", listItems.get(arg2-1).get("Url").toString()));

	}

	public class MyJSONObj
	{
		private List title;

		public List getTitle()
		{
			return title;
		}

		public void setTitle(List title)
		{
			this.title = title;
		}

		public List getTime()
		{
			return time;
		}

		public void setTime(List time)
		{
			this.time = time;
		}

		private List time;
		private List url;

		public List getUrl()
		{
			return url;
		}

		public void setUrl(List url)
		{
			this.url = url;
		}
	}

	public void onBackButtonClick(View v)
	{
		finish();
	}

}
