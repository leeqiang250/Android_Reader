package cn.com.pyc.user;

import android.os.Bundle;
import cn.com.pyc.base.ExtraBaseActivity;
import cn.com.pyc.bean.UserInfo;
import cn.com.pyc.db.UserDao;

public class UserBaseActivity extends ExtraBaseActivity {
	protected UserInfo userInfo;
	protected UserDao db;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		db = UserDao.getDB(this);
		userInfo = db.getUserInfo();
	}

}
