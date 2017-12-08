①运行PengBaoBao关联PbbReader包说明：

1.PbbReader和QlkUtil都使用release状态。

2.修改PbbReader下build.gradle文件：
             a. 改为apply plugin: 'com.android.library'
             b. 注释掉applicationId
             c. 关闭混淆minifyEnabled

3.注释PbbReader清单文件Manifest.xml中启动intent:
             <intent-filter>
                   <action android:name="android.intent.action.MAIN"/>
                   <category android:name="android.intent.category.LAUNCHER"/>
             </intent-filter>

4.修改settings.gradle中include，添加'PengBaoBao' module.




②运行PbbReader时候，修改settings.gradle中include，去掉'PengBaoBao' module.

