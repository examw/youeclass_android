package com.youeclass;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.app.AppContext;
import com.youeclass.util.Constant;
import com.youeclass.util.HttpConnectUtil;

public class StartActivity extends Activity{
	private AlertDialog alertDialog = null;
	private ProgressDialog progressDialog = null;
	private boolean isCanceled=false;
	private AsyncTask<String,Integer,String> downloader = null;
	private SharedPreferences settingfile;
	private Handler handler;
	private AppContext appContext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		com.umeng.common.Log.LOG = true;
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_start);
		SharedPreferences guidefile = getSharedPreferences("guidefile", 0);
		this.settingfile = this.getSharedPreferences("settingfile", 0);
		appContext = (AppContext) getApplication();
		int versionCode = appContext.getVersionCode();
		initSetting();
		int isfirst = guidefile.getInt("isfirst"+versionCode, 0);
		handler = new MyHandler(this);
		if(isfirst==1)
		{
			//������
			//�Ȼ�ȡ�汾��Ϣ
			if(isNeedCheck())
			{
				if(checkNetWork())
				{
					CheckUpdateTask checkup = new CheckUpdateTask();
					try {
						int oldVersion = getVersionCode();	//��ȡ�ɵİ汾��
						checkup.execute(Constant.DOMAIN_URL+"mobile/checkup?appType=1&oldVersion="+oldVersion);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						handler.sendEmptyMessage(-1);
					}
				}else
				{
					handler.sendEmptyMessageDelayed(1, 2000);
				}
			}else
			{
				handler.sendEmptyMessageDelayed(1, 2000);
			}
		}else
		{
			
			handler.sendEmptyMessageDelayed(2, 2000);
		}
	}
	private void initSetting() {
		// TODO Auto-generated method stub
		if(this.settingfile.contains("IsFirst"))
		{
			return;
		}
		SharedPreferences.Editor editor = this.settingfile.edit();
		editor.putString("IsFirst", "No");
		editor.putBoolean("setDownIsUse3G", true);
		editor.putString("setDownfilepath", getString(R.string.Downfilepath));
		editor.putBoolean("setDownfiletype", true);
		editor.putBoolean("setPlayIsUse3G", true);
		editor.putBoolean("setPlayfiletype", true);
		editor.putInt("setCheckUpdateMode",0);
		editor.putLong("lastCheckUpdateTime", 0);
		editor.commit();
	}
	//��ȡ��ǰӦ�õİ汾��
	private int getVersionCode() throws Exception
	   {
	           // ��ȡpackagemanager��ʵ��
	           PackageManager packageManager = getPackageManager();
	           // getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
	           PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
	           int versionCode = packInfo.versionCode;
	           return versionCode;
	   }
	//�ж��Ƿ���Ҫ���и���
	private boolean isNeedCheck()
	{
		int checkMode = settingfile.getInt("setCheckUpdateMode", 0);
		long lastTime = settingfile.getLong("lastCheckUpdateTime", 0);
		long now = System.currentTimeMillis();
		if(checkMode==0)
		{
			return true;
		}else if(checkMode==1)
		{
			if(now - lastTime >(24*60*60*1000))
			{
				return true;
			}
			return false;
		}else if(checkMode==2)
		{
			if(now - lastTime >(7*24*60*60*1000))
			{
				return true;
			}
			return false;
		}else if(checkMode==3)
		{
			if(now - lastTime >(30*24*60*60*1000))
			{
				return true;
			}
			return false;
		}
		return false;
	}
	private class CheckUpdateTask extends AsyncTask<String,Integer,String>
	{
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			SharedPreferences.Editor editor = settingfile.edit();
			editor.remove("lastCheckUpdateTime");
			editor.putLong("lastCheckUpdateTime", System.currentTimeMillis());
			editor.commit();
			super.onPreExecute();
		}
		//��飬����ֵ���ǽ���ʱ�Ľ������
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String result = null;
			HttpURLConnection conn = null;
			try{
				Thread.sleep(1000);
				result = HttpConnectUtil.httpGetRequest(StartActivity.this, params[0]);
			}catch(Exception e)
			{
				//���ִ���
//				e.printStackTrace();
				return null;
			}finally
			{
				if(conn!=null)
				{
					conn.disconnect();
				}
			}
			return result;
		}
		//�������
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(result==null||"".equals(result))
			{
				//��ת����¼����
				Intent intent = new Intent();
				intent.setClass(StartActivity.this, LoginActivity.class);
				StartActivity.this.startActivity(intent);
				StartActivity.this.finish();
				return;
			}
			try {
				JSONObject json = new JSONObject(result);
				if(json.getInt("S")==1)//��ʾ�и���
				{
					final String version = json.optString("version");
			        final String url = URLDecoder.decode(json.optString("url"),"UTF-8");
			        System.out.println(url);
			        String content=null;
			        content = json.optString("Content");
			        alertDialog = new AlertDialog.Builder(StartActivity.this)
					.setTitle("���¼��")
					.setMessage("��⵽���°汾��" + version + "\n" + "�������ݣ�" + "\n" + content)
					.setPositiveButton("����", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
							//�������ط����Լ����ȶԻ��򣬽�������
							//to do something 
							progressDialog = new ProgressDialog(StartActivity.this);
						    progressDialog.setProgressStyle(1);
						    progressDialog.setTitle("�������");
						    progressDialog.setMessage("���°汾��" + version);
						    progressDialog.setIcon(R.drawable.down2);
						    progressDialog.setProgress(10);
						    progressDialog.setMax(100);
						    progressDialog.setIndeterminate(false);
						    progressDialog.setCancelable(false);
						    //����һ���첽�������app����
						    downloader = new DownLoaderTask();
						    progressDialog.setButton("ȡ��", new OnClickListener() {	//ȡ������
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									//ȡ��
									dialog.cancel();
									isCanceled =true;
									downloader.cancel(true);
								    Intent localIntent = new Intent(StartActivity.this, LoginActivity.class);
								    StartActivity.this.startActivity(localIntent);
								    StartActivity.this.finish();
								}
							});
						    downloader.execute(url);	//���ص�ַ
						    progressDialog.show();
						  //���������¼�,�����ȡ��ʲô������,�൱�ڶ�����Ի�������˷��ؼ�
						    progressDialog.setOnKeyListener(new OnKeyListener() {
								@Override
								public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
									// TODO Auto-generated method stub
									if(event.getKeyCode()==4)
									{
										return true;
									}
									return false;
								}
							});
						}
					})
					.setNegativeButton("ȡ��", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						    Intent localIntent = new Intent(StartActivity.this, LoginActivity.class);
						    StartActivity.this.startActivity(localIntent);
						    StartActivity.this.finish();
						}
					})
					.show();
			        //���������¼�,�����ȡ��ʲô������,�൱�ڶ�����Ի�������˷��ؼ�
			        alertDialog.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							// TODO Auto-generated method stub
							if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
							{
								return true;
							}
							return false;
						}
					});
				}else //û�и���
				{
					gotoLogin();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	//��������
				gotoLogin();
			}
		}
	}
	
	//���ظ���app
	private class DownLoaderTask extends AsyncTask<String, Integer, String> {
		//The method (doInBackground) runs always on a background thread. You shouldn't do any UI tasks there.
	    @Override
	    protected String doInBackground(String... sUrl) {
	        try {
	            URL url = new URL(sUrl[0]);
	            URLConnection connection = url.openConnection();
	            connection.connect();
	            // this will be useful so that you can show a typical 0-100% progress bar
	            int fileLength = connection.getContentLength();
	            // download the file
	            InputStream input = new BufferedInputStream(url.openStream());
	            //StartActivity.createFile("Hello");
	            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Hello.apk");
	            byte data[] = new byte[1024];
	            long total = 0;
	            int count;
	            while ((count = input.read(data)) != -1) {
	            	if(!isCanceled&&!this.isCancelled()){
	                total += count;
	                // publishing the progress....
	                publishProgress((int) (total * 100 / fileLength));
	                output.write(data, 0, count);
	            	}else
	            	{
	            		break;
	            	}
	            }
	            output.flush();
	            output.close();
	            input.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	        if(isCanceled||this.isCancelled()){
	        	System.out.println("ȡ��������");
	        	new File(Environment.getExternalStorageDirectory().getPath()+"/Hello.apk").delete();
	        }
	        return null;
	    }
	    //the onProgressUpdate and onPreExecute run on the UI thread, so there you can change the progress bar:
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        progressDialog.show();
	    }
	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        progressDialog.setProgress(progress[0]);
	    }
	    @Override
	    protected void onPostExecute(String result) {
	    	// TODO Auto-generated method stub
	    	super.onPostExecute(result);
	    	if(!isCanceled&&!this.isCancelled()){
	    		progressDialog.dismiss();
	    		//��ʾ���Ӧ�ã����û���װ
	    		Uri localUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+"/Hello.apk"));
	        	Intent localIntent = new Intent("android.intent.action.VIEW");
	        	localIntent.setDataAndType(localUri, "application/vnd.android.package-archive");
	        	StartActivity.this.startActivity(localIntent);
	    	}
	    }
	    @Override
	    protected void onCancelled() {
	    	// TODO Auto-generated method stub
	    	super.onCancelled();
	    	
	    }
	}
	//�������
	private boolean checkNetWork() {
		ConnectivityManager manager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			Toast.makeText(this,"��������", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	public static void createFile(String paramString)
	  {
		File a = null;
		File b = null;
	    if ("mounted".equals(Environment.getExternalStorageState()))
	    {
	      a = new File(Environment.getExternalStorageDirectory() + "/" + "Eschool/");
	      if (!a.exists())
		        a.mkdirs();
	      b = new File(Environment.getExternalStorageDirectory() + "/Eschool/" + paramString + ".apk");
	      if (!b.exists())
	      {
	    	  try
	  	    {
	  	      b.createNewFile();
	  	      return;
	  	    }
	  	    catch (IOException localIOException)
	  	    {
	  	      localIOException.printStackTrace();
	  	    }
	      }
	    }
	  }
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		gotoLogin();
	}
	private void gotoLogin()
	{
		Intent intent = new Intent();
		intent.setClass(StartActivity.this, LoginActivity.class);
		StartActivity.this.startActivity(intent);
		StartActivity.this.finish();
	}
	private void gotoGuide()
	{
		Intent intent = new Intent();
		intent.setClass(this, GuideActivity.class);
		this.startActivity(intent);
		this.finish();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(alertDialog!=null)
		{
			alertDialog.dismiss();
		}
		if(progressDialog!=null)
		{
			progressDialog.dismiss();
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		
	}
	static class MyHandler extends Handler
	{
		WeakReference<StartActivity> mActivity;
		public MyHandler(StartActivity activity) {
			mActivity = new WeakReference<StartActivity>(activity);
		 }
		@Override
		public void handleMessage(Message msg) {
			StartActivity login = mActivity.get();
			switch(msg.what)
			{
			case 1:
				login.gotoLogin();
				break;
			case -1:
				login.gotoLogin();
				break;
			case 2:
				login.gotoGuide();
				break;
			}
		}
	}
}
