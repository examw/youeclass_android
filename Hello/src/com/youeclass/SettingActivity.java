package com.youeclass;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.util.Constant;

public class SettingActivity extends Activity implements OnCheckedChangeListener,OnClickListener{
	private ImageButton returnBtn,clearCache;
	private TextView availableSpace,downFilePath;
	private CheckBox isUse3GDown,isUse3GPlay;
	private String username;
	private SharedPreferences setting;
	private Spinner spinner;
	private Button checkBtn,aboutusBtn;
	private AlertDialog alertDialog = null;
	private ProgressDialog progressDialog,o;
	private boolean isCanceled=false;
	private AsyncTask<String,Integer,String> downloader = null;
	private static String[] data = new String[]{"ÿ������","һ��һ��","һ����һ��","һ����һ��"};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_setting);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.returnBtn.setOnClickListener(this);
		this.availableSpace = (TextView) this.findViewById(R.id.set_down_spaceavailable);
		this.downFilePath = (TextView) this.findViewById(R.id.down_filepathTxt);
		this.username = getIntent().getStringExtra("username");
		String path = getString(R.string.Downfilepath);
		this.downFilePath.setText(path+this.username+"/");
		this.setting = this.getSharedPreferences("settingfile", 0);
		this.isUse3GDown = (CheckBox) this.findViewById(R.id.set_IsUser3G_check);
		this.isUse3GPlay = (CheckBox) this.findViewById(R.id.set_IsUser3G_check2);
		this.clearCache = (ImageButton) this.findViewById(R.id.set_clearPicBtn);
		this.clearCache.setOnClickListener(this);
		this.checkBtn = (Button) this.findViewById(R.id.check);
		this.checkBtn.setOnClickListener(this);
		this.aboutusBtn = (Button) this.findViewById(R.id.aboutus);
		this.aboutusBtn.setOnClickListener(this);
		this.spinner = (Spinner) this.findViewById(R.id.checkupdateSpinner);
		this.spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,data));
		this.spinner.setPrompt("��ѡ�����������");
		this.spinner.setSelection(this.setting.getInt("setCheckUpdateMode", 0));//����Ĭ��ֵ
		this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor editor = setting.edit();
				editor.remove("setCheckUpdateMode");
				editor.putInt("setCheckUpdateMode", arg2);
				editor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		initAvailableSpaceTextView();
		initCheckBox();
		this.isUse3GDown.setOnCheckedChangeListener(this);
		this.isUse3GPlay.setOnCheckedChangeListener(this);
	}
	private void initCheckBox() {
		// TODO Auto-generated method stub
		this.isUse3GDown.setChecked(this.setting.getBoolean("setDownIsUse3G", true));
		this.isUse3GPlay.setChecked(this.setting.getBoolean("setPlayIsUse3G", true));
		if(!this.setting.contains("IsFirst"))
		{
			SharedPreferences.Editor editor = this.setting.edit();
			editor.putString("Isfirst", "No");
			editor.putBoolean("setDownIsUse3G", true);
			editor.putString("setDownfilepath", getString(R.string.Downfilepath));
			editor.putBoolean("setDownfiletype", true);
			editor.putBoolean("setPlayIsUse3G", true);
			editor.putBoolean("setPlayfiletype", true);
			editor.putInt("setCheckUpdateMode",0);
			editor.putLong("lastCheckUpdateTime", 0);
			editor.commit();
		}
	}
	private void initAvailableSpaceTextView() {
		// TODO Auto-generated method stub
		//�ж�SD���Ƿ����,����SD���Ŀ��ÿռ�
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			//���� sd���ÿռ�Ĵ�С
			//ȡ��SD�ļ���·��
			File path = Environment.getExternalStorageDirectory(); 
			StatFs statfs = new StatFs(path.getPath());
			//���block���Ĵ�С
			long blockSize = statfs.getBlockSize();
			//��ÿ���block������ 
			long availableBlocks = statfs.getAvailableBlocks();
			long size = availableBlocks * blockSize/1024/1024;	//MB
			this.availableSpace.setText(" "+size+" MB");
			return;
		}
		this.availableSpace.setTextColor(getResources().getColor(R.color.grey));
		this.availableSpace.setText(" SD��������");
		this.downFilePath.setTextColor(getResources().getColor(R.color.grey));
		this.downFilePath.setText(this.downFilePath.getText()+" ·��������");
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		int id = buttonView.getId();
		SharedPreferences.Editor editor = this.setting.edit();
		switch(id)
		{
		case R.id.set_IsUser3G_check:
			editor.remove("setDownIsUse3G");
			editor.putBoolean("setDownIsUse3G", this.isUse3GDown.isChecked());
			editor.commit();
		case R.id.set_IsUser3G_check2:
			editor.remove("setPlayIsUse3G");
			editor.putBoolean("setPlayIsUse3G", this.isUse3GPlay.isChecked());
			editor.commit();
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.set_clearPicBtn:
			Toast.makeText(this, "����ɹ�", Toast.LENGTH_SHORT).show();
			break;
		case R.id.returnbtn:
			this.finish();
			break;
		case R.id.aboutus:
			startActivity(new Intent(this,AboutusActivity.class));
			break;
		case R.id.check:
			check();
			break;
		}
	}
	private void check()
	{
		o = ProgressDialog.show(SettingActivity.this, null, "��������Ժ�",
				true, false);
		o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		CheckUpdateTask checkup = new CheckUpdateTask();
		try {
			String oldVersion = getVersionName();	//��ȡ�ɵİ汾��
			System.out.println(oldVersion);
			checkup.execute(Constant.DOMAIN_URL+"mobile/checkup?appType=1&oldVersion="+oldVersion);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//��ȡ��ǰӦ�õİ汾��
		private String getVersionName() throws Exception
		   {
		           // ��ȡpackagemanager��ʵ��
		           PackageManager packageManager = getPackageManager();
		           // getPackageName()���㵱ǰ��İ�����0�����ǻ�ȡ�汾��Ϣ
		           PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
		           String version = packInfo.versionName;
		           return version;
		   }
	private class CheckUpdateTask extends AsyncTask<String,Integer,String>
	{
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			o.show();
			SharedPreferences.Editor editor = setting.edit();
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
				URL url = new URL(params[0]);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);//���ӳ�ʱ
				conn.setRequestMethod("GET");//����ʽ
				conn.connect();// ����
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.d(this.toString(),
							"getResponseCode() not HttpURLConnection.HTTP_OK");
					return null;
				}
				InputStream in = conn.getInputStream();
				//����һ�������ֽ���			//�ֽڲ���ʱ��ô��
				byte[] buffer = new byte[in.available()];
				//���������ж�ȡ���ݲ���ŵ������ֽ�������
				in.read(buffer);
				//���ֽ�ת�����ַ���
				result = new String(buffer);
				System.out.println(result);
			}catch(Exception e)
			{
				//���ִ���
				Log.d(this.toString(),
						e.getMessage());
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
			if(o!=null)
				o.dismiss();
			if(result==null)
			{
				//��ת����¼����
				Toast.makeText(SettingActivity.this, "�޷����,�Ժ�����", Toast.LENGTH_SHORT).show();
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
			        alertDialog = new AlertDialog.Builder(SettingActivity.this)
					.setTitle("���¼��")
					.setMessage("��⵽���°汾��" + version + "\n" + "�������ݣ�" + "\n" + content)
					.setPositiveButton("����", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
							//�������ط����Լ����ȶԻ��򣬽�������
							//to do something 
							progressDialog = new ProgressDialog(SettingActivity.this);
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
						    progressDialog.setButton("ȡ��", new DialogInterface.OnClickListener() {	//ȡ������
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									//ȡ��
									dialog.cancel();
									isCanceled =true;
									downloader.cancel(true);
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
					.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
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
				}else{
					Toast.makeText(SettingActivity.this, "�Ѿ������°汾", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
	        	SettingActivity.this.startActivity(localIntent);
	    	}
	    }
	    @Override
	    protected void onCancelled() {
	    	// TODO Auto-generated method stub
	    	super.onCancelled();
	    	
	    }
	}
	public static void createFile(String paramString)
	  {
		File a = null;
		File b = null;
	    if ("mounted".equals(Environment.getExternalStorageState()))
	    {
	      a = new File(Environment.getExternalStorageDirectory() + "/" + "eschool/");
	      if (!a.exists())
		        a.mkdirs();
	      b = new File(Environment.getExternalStorageDirectory() + "/eschool/" + paramString + ".apk");
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
}
