package com.youeclass;

import java.lang.ref.WeakReference;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.app.AppContext;
import com.youeclass.dao.UserDao;
import com.youeclass.db.MyDBHelper;
import com.youeclass.entity.User;
import com.youeclass.util.Constant;
import com.youeclass.util.HttpConnectUtil;

public class LoginActivity extends Activity implements TextWatcher {
	private AutoCompleteTextView usernameText;
	private String[] items;// ����autoCompleteTextView������
	private EditText pwdText;
	private Button goRegisterBtn;
	private ImageButton loginBtn1,loginBtn2;
	private ProgressDialog o;
	private Handler handler;
	private CheckBox rememeberCheck;
	private String password;
	private SharedPreferences share;
	private SharedPreferences share2;
	private SharedPreferences userinfo;
	private UserDao userdao;
	private AppContext appContext;
	/** �����½ʧ��,������Ը��û�ȷ�е���Ϣ��ʾ,true����������ʧ��,false���û������������ */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		usernameText = (AutoCompleteTextView) this
				.findViewById(R.id.usernameText);// �û���
		pwdText = (EditText) this.findViewById(R.id.pwdText);// ����
		loginBtn1 = (ImageButton) this.findViewById(R.id.login1Btn);// ���ߵ�¼
		loginBtn2 = (ImageButton) this.findViewById(R.id.login2Btn);// ���ص�¼
		rememeberCheck = (CheckBox) this.findViewById(R.id.rememeberCheck);// ��ס����
		goRegisterBtn = (Button) this.findViewById(R.id.goRegisterBtn);// ע��
		goRegisterBtn.setText(Html.fromHtml("<u>���ע��</u>"));
		userdao = new UserDao(new MyDBHelper(this)); 	//�������ݿ�
		share = getSharedPreferences("passwordfile", 0);
		share2 = getSharedPreferences("abfile", 0);
		userinfo = getSharedPreferences("userinfo", 0);
		items = share.getAll().keySet().toArray(new String[0]);
		appContext = (AppContext) getApplication();
		usernameText.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, items));
		usernameText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				usernameText.requestFocus();
				return true;
			}
		});
		usernameText.addTextChangedListener(this);
		loginBtn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//��¼��¼�¼�
				MobclickAgent.onEvent(LoginActivity.this,"LoginIn_online");
				
				final String name = usernameText.getText().toString();
				password = pwdText.getText().toString();
				if (checkInput()) {
					if(checkNetWork())
					{
						// ��ʾ���ڵ�¼
						if(o==null)
						{
							o = ProgressDialog.show(LoginActivity.this, null, "��¼�����Ժ�",
									true, true	);
							o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						}else
						{
							o.show();
						}
						if(appContext.getLoginState() == AppContext.LOGINING)
						{
							return;
						}
						// ����һ���߳�������¼
						new Thread() {
								public void run() {
									String url = Constant.DOMAIN_URL+"mobile/login?username="+name+"&password="+password;
									String result = null;
									appContext.setLoginState(AppContext.LOGINING);
									try {
										result = HttpConnectUtil.httpGetRequest(
												LoginActivity.this, url);
										if (!"".equals(result)) {
											try {
												// �����ַ���
												JSONObject json = new JSONObject(result);
												int ok = json.optInt("OK", 0);
												int id = json.optInt("uid", 0);
												if (ok == 1) { // ��¼�ɹ�
													if (isRememberMe()) {
														saveSharePreferences(true,true);
													}
													userinfo.edit().putInt("id", id)
															.commit();
													userinfo.edit()
															.putString("name", name)
															.commit();
													password = new String(Base64.encode(Base64.encode(password.getBytes(), 0), 0));
													User user = new User(String.valueOf(id),name,password);
													saveToLocaleDB(user);
													appContext.saveLoginInfo(user);
													Message msg = handler.obtainMessage();
													msg.what = 1;
													Bundle data = new Bundle();
													data.putString("username", name);
													data.putInt("uid", id);
													msg.setData(data);
													handler.sendMessage(msg);//��¼�ɹ�
												}else
												{
													handler.sendEmptyMessage(-3); //�û����������
													appContext.setLoginState(AppContext.LOGIN_FAIL);
												}
											} catch (Exception e) {
												handler.sendEmptyMessage(-2);
												appContext.setLoginState(AppContext.LOGIN_FAIL);
											}
										} else {
											handler.sendEmptyMessage(-2);
											appContext.setLoginState(AppContext.LOGIN_FAIL);
										}
									} catch (Exception e) {
										e.printStackTrace();
										handler.sendEmptyMessage(-1); // ��������
										appContext.setLoginState(AppContext.LOGIN_FAIL);
									}
						};
						}.start();
					}
				}
			}
		});
		loginBtn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(checkInput()){
				String name = usernameText.getText().toString();
				User user = userdao.findByUsername(name);
				System.out.println(user);
				if(user!=null)
				{
					String password = pwdText.getText().toString();
					if(password.equals(new String(Base64.decode(Base64.decode(user.getPassword(), 0), 0))))
					{
						showToast("��¼�ɹ�");
						appContext.saveLocalLoginInfo(name);
						Intent intent = new Intent();
						intent.setClass(LoginActivity.this, MainActivity.class);
						Bundle bundle = new Bundle();
						bundle.putString("MAP_USERNAME", name);
						bundle.putString("loginType", "local");
						intent.putExtras(bundle);
						// ת���½���ҳ��
						BaseActivity.username = name;
						BaseActivity.loginType = "local";
						startActivity(intent);
						LoginActivity.this.finish();
					}else
					{
						showToast("�������ߵ�¼");
					}
				}else
				{
					showToast("�������ߵ�¼");
				}
				}
			}
		});
		goRegisterBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(LoginActivity.this,
						RegisterActivity.class));
			}
		});
		findViewById(R.id.findPwd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				viewWebSite();
			}
		});
		handler = new MyHandler(this);

	}
	private void viewWebSite()
	{
		 String url = getResources().getString(R.string.findPwdUrl);
		 Uri uri = Uri.parse(url);          
	     Intent it = new Intent(Intent.ACTION_VIEW, uri);
	     startActivity(it);
	}
	private static class MyHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		MyHandler(LoginActivity activity) {
			mActivity = new WeakReference<LoginActivity>(activity);
		}

		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			LoginActivity login = mActivity.get();
			if (login.o != null) {
				login.o.dismiss();
			 }
			switch(msg.what)
			{
			case 1:
				//��¼�ɹ�
				Bundle data = msg.getData();
				Intent intent = new Intent(login,MainActivity.class);
				intent.putExtra("MAP_USERNAME",data.getString("username"));
				intent.putExtra("uid",data.getInt("uid"));
				intent.putExtra("loginType", "online");
				login.startActivity(intent);
				login.finish();
				break;
			case -1:
				Toast.makeText(login, "���Ӳ���������", Toast.LENGTH_SHORT).show();
				break;
			case -2:
				Toast.makeText(login, "���Ӵ���", Toast.LENGTH_SHORT).show();
				break;
			case -3:
				Toast.makeText(login, "�û������������", Toast.LENGTH_SHORT).show();
				break;
			}
		}

	}

	/**
	 * �����¼�ɹ���,�򽫵�½�û����������¼��SharePreferences
	 * 
	 * @param saveUserName
	 *            �Ƿ��û������浽SharePreferences
	 * @param savePassword
	 *            �Ƿ����뱣�浽SharePreferences
	 * */
	private void saveSharePreferences(boolean saveUserName, boolean savePassword) {
		if (saveUserName) {
			Log.d(this.toString(), "saveUserName="
					+ usernameText.getText().toString());
			share.edit()
					.putString(
							usernameText.getText().toString(),
							Base64.encodeToString(
									Base64.encode(password.getBytes(), 0), 0))
					.commit();
			share2.edit().putString("n", usernameText.getText().toString())
					.commit();
			share2.edit()
					.putString(
							"p",
							Base64.encodeToString(
									Base64.encode(password.getBytes(), 0), 0))
					.commit();
		}
		//share = null;
	}
	//
	public void saveToLocaleDB(User user){
		if(userdao==null)
		{
			userdao = new UserDao(new MyDBHelper(this));
		}
			try {
				userdao.saveOrUpdate(user);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	/** ��ס����ѡ���Ƿ�ѡ */
	private boolean isRememberMe() {
		if (rememeberCheck.isChecked()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	// �������
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

	// �������������
	private boolean checkInput() {
		String username = usernameText.getText().toString().trim();
		String password = pwdText.getText().toString().trim();
		if (username.equals("") || password.equals("")) {
			Toast.makeText(LoginActivity.this, "�û��������벻��Ϊ��", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	// ��ʼ�������
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.usernameText.setText(this.getSharedPreferences("abfile", 0)
				.getString("n", ""));
		String pwd = this.getSharedPreferences("abfile", 0).getString("p", "");
		this.pwdText
				.setText(new String(Base64.decode(Base64.decode(pwd, 0), 0)));
		MobclickAgent.onResume(this);
	}

	// �������˳�����
	long waitTime = 2000;// �ȴ�ʱ��2s
	long touchTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& KeyEvent.KEYCODE_BACK == keyCode) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - touchTime) >= waitTime) {
				Toast.makeText(this, "�ٰ�һ���˳�", Toast.LENGTH_SHORT).show();
				touchTime = currentTime;
			} else {
				this.finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		String name = usernameText.getText().toString();
		pwdText.setText(new String(Base64.decode(
				Base64.decode(share.getString(name, ""), 0), 0)));
		if (pwdText.getText().toString().length() > 0)
			pwdText.requestFocus();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}
	private void showToast(String content)
	{
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(userdao!=null)
		{
			userdao.closeDB();
		}
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if(userdao==null)
		{
			userdao = new UserDao(new MyDBHelper(this));
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(o!=null)
		{
			o.dismiss();
		}
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
}
