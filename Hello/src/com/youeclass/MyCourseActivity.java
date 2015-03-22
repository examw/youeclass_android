package com.youeclass;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.dao.UserClassDao;
import com.youeclass.entity.UserClass;
import com.youeclass.util.Constant;
import com.youeclass.util.HttpConnectUtil;

public class MyCourseActivity extends BaseActivity implements OnClickListener {
	private SharedPreferences userinfo;
	private ProgressDialog dialog;
	private Handler handler;
	private ExpandableListView expandList;
	private LinearLayout nodata;
	private ImageButton returnBtn;
	private String[] group;
	private String[][] child;
	private String[][] classDetail;
	private int id;
	private boolean isLocalLogin;
	private LinearLayout outlineCourse, playrecord;
	private UserClassDao dao;
	private String username,loginType;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_mycourselist);
		Intent intent = this.getIntent();
		this.loginType = intent.getStringExtra("loginType");
		this.isLocalLogin = "local".equals(loginType);
		this.username = intent.getStringExtra("username");
		userinfo = getSharedPreferences("userinfo", 0);
		id = userinfo.getInt("id", 0);
		expandList = (ExpandableListView) this.findViewById(R.id.explist2);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		outlineCourse = (LinearLayout) this
				.findViewById(R.id.MyfileDown_layout_btn);
		playrecord = (LinearLayout) this
				.findViewById(R.id.LearningRecord_layout_btn);
		returnBtn.setOnClickListener(this);
		outlineCourse.setOnClickListener(this);
		playrecord.setOnClickListener(this);
		expandList.setGroupIndicator(null);
		expandList.setOnChildClickListener(new ChildClickListener());
		expandList.setOnGroupClickListener(new GroupClickListener());
		nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		handler = new MyHandler(this);
		dao = new UserClassDao(this);
		dialog = ProgressDialog.show(MyCourseActivity.this, null, "Ŭ�����������Ժ�",
				true, true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		new GetMyLessonThread().start();
		
	}

	private class GetMyLessonThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if (!isLocalLogin) {
					String result = HttpConnectUtil.httpGetRequest(
							MyCourseActivity.this,
							Constant.DOMAIN_URL+"mobile/myLessons?stuId="
									+ id);
					//�����ַ���
					if (result != null && !result.equals("null")) {
						// ����json�ַ���,����expandableListView��adapter
						try {
							JSONObject json = new JSONObject(result);
							JSONArray packages = json.optJSONArray("classPackages");
							JSONArray grades = json.getJSONArray("grade");
							int plength = packages.length();
							int glength = grades.length();
							List<UserClass> list = new ArrayList<UserClass>();
							// ����,�ײͻ򵥰༶
							group = new String[plength + glength];
							// ����,�ײ��µİ༶,���༶û������
							// ע������ĳ��ȱȴ���Ķ�
							child = new String[plength][];
							// �γ���ϸ,����༶ʱ��ת
							classDetail = new String[plength + glength][];
							// ѭ���ײ�( classid,classname,username,fatherid,classtype)
							for (int i = 0; i < plength; i++) {
								JSONObject p = packages.getJSONObject(i);
								group[i] = p.optString("pkgName");
								JSONArray p_grades = p.getJSONArray("grade");
								child[i] = new String[p_grades.length()];
								classDetail[i] = new String[p_grades
										.length()];
								int pkgId = p.optInt("pkgId");
								UserClass c = new UserClass( pkgId + "",
										p.optString("pkgName"), username, 0 + "",
										1 + "");
								list.add(c);
								// ѭ���ײ��µİ༶
								for (int k = 0; k < p_grades.length(); k++) {
									p = p_grades.getJSONObject(k);
									child[i][k] = p.getString("name");
									JSONArray cd = p.optJSONArray("classDetails");
									classDetail[i][k] = cd.toString();
									//
									UserClass c1 = new UserClass(
											p.optInt("gradeId") + "",
											p.optString("name"), username,
											pkgId + "", 0 + "");
									list.add(c1);
								}
							}
							// ѭ���༶
							for (int j = 0; j < glength; j++) {
								JSONObject p = grades.getJSONObject(j);
								group[j + plength] = p
										.optString("name");
								JSONArray cd = p.optJSONArray("classDetails");
								classDetail[plength + j] = new String[1];
								classDetail[plength + j][0] = cd == null ? "[]"
										: cd.toString();
								UserClass c1 = new UserClass(p.optInt("gradeId")
										+ "", p.optString("name"), username,
										p.optInt("pkgId") + "", 0 + "");
								list.add(c1);
							}
							// ���ҵĿγ̱��浽���ݿ�
							dao.deleteAll(username);
							dao.addClasses(list);
							Message msg = handler.obtainMessage();
							msg.what = 1;
							handler.sendMessage(msg);
						} catch (Exception e) {
								e.printStackTrace();
								handler.sendEmptyMessage(-1);
							}
						} else {
							//����Ϣ
							Message msg = handler.obtainMessage();
							msg.what = -2;
							handler.sendMessage(msg);
						}
				}else
				{
					//ͨ�����ݿ��� [group,child]
					group = dao.findBigClassName(username);
					child = dao.findChildrenClass(username);
					classDetail = dao.findChildrenClassid(username);
					Message msg = handler.obtainMessage();
					if(group==null)
					{
						msg.what = -3;
					}else
					{
						msg.what = 2;
					}
					handler.sendMessage(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
				Message msg = handler.obtainMessage();
				msg.what = -1;
				handler.sendMessage(msg);
			}
		}
	}

	static class MyHandler extends Handler {
		WeakReference<MyCourseActivity> mActivity;

		MyHandler(MyCourseActivity activity) {
			mActivity = new WeakReference<MyCourseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			MyCourseActivity theActivity = mActivity.get();
			switch (msg.what) {
			case 1:
				theActivity.dialog.dismiss();
				theActivity.expandList.setAdapter(new MyExpandableAdapter(
										theActivity, theActivity.group,
										theActivity.child));
						// ����adapter
				break;
			case -1:
				// ������,
				theActivity.dialog.dismiss();
				theActivity.nodata.setVisibility(View.VISIBLE);// ��������ʾ
				Toast.makeText(theActivity, "��ʱ�����Ϸ�����,���Ժ�", Toast.LENGTH_SHORT)
						.show();// ��ʾ
				break;
			case -2:
				//û������
				theActivity.dialog.dismiss();
				theActivity.nodata.setVisibility(View.VISIBLE);// ��������ʾ
				Toast.makeText(theActivity, "��û�й���γ�", Toast.LENGTH_SHORT)
						.show();// ��ʾ
				break;
			case -3:
				//���ݿ���û������
				theActivity.dialog.dismiss();
				theActivity.nodata.setVisibility(View.VISIBLE);// ��������ʾ
				Toast.makeText(theActivity, "�������ݿ���û������,�����ߵ�¼������ҵĿγ�", Toast.LENGTH_SHORT)
						.show();// ��ʾ
				break;
			case 2:
				//ͨ�����ݿ����
				theActivity.dialog.dismiss();
				theActivity.expandList
						.setAdapter(new MyExpandableAdapter(
								theActivity, theActivity.group,
								theActivity.child));
				break;
			}
		}
	}

	private class ChildClickListener implements OnChildClickListener {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MyCourseActivity.this,
					MyCourseDetailActivity.class);
			intent.putExtra("name", ((TextView) v.findViewById(R.id.text3))
					.getText().toString());
			if(isLocalLogin)
			{
				intent.putExtra("classid", MyCourseActivity.this.classDetail[groupPosition][childPosition]);
			}else{
				intent.putExtra(
						"classDetails",
						MyCourseActivity.this.classDetail[groupPosition][childPosition]);
			}
			intent.putExtra("username", username);
			intent.putExtra("loginType", loginType);
			MyCourseActivity.this.startActivity(intent);
			return true;
		}
	}

	private class GroupClickListener implements OnGroupClickListener {
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			// TODO Auto-generated method stub
			// ���û��������,��ʾ�ǵ����Ŀγ�
			if (parent.getExpandableListAdapter().getChildrenCount(
					groupPosition) == 0) {
				Intent intent = new Intent(MyCourseActivity.this,
						MyCourseDetailActivity.class);
				intent.putExtra("name", ((TextView) v.findViewById(R.id.text2))
						.getText().toString());
				if(isLocalLogin)
				{
					intent.putExtra("classid", MyCourseActivity.this.classDetail[groupPosition][0]);
				}else{
				intent.putExtra("classDetails",
						MyCourseActivity.this.classDetail[groupPosition][0]);}
				intent.putExtra("username", username);
				intent.putExtra("loginType", loginType);
				MyCourseActivity.this.startActivity(intent);
				return true;
			}
			return false;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.LearningRecord_layout_btn: // ���ż�¼
			Intent mIntent = new Intent(this, PlayrecordActivity.class);
			mIntent.putExtra("username", username);
			mIntent.putExtra("loginType", loginType);
			this.startActivity(mIntent);
			break;
		case R.id.MyfileDown_layout_btn:
			Intent intent = new Intent(this, DownloadActivity.class);
			intent.putExtra("actionName", "outline");
			intent.putExtra("username", username);
			this.startActivity(intent);
			break;
		case R.id.returnbtn:
			this.finish();
			return;
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
