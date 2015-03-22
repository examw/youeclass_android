package com.youeclass;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.adapter.MyExpandableAdapter2;
import com.youeclass.entity.UserClass;
import com.youeclass.util.Constant;
import com.youeclass.util.HttpConnectUtil;

public class QuestionFromCourseActivity extends Activity{
	private SharedPreferences userinfo;
	private ProgressDialog dialog;
	private static String username;
	private Handler handler;
	private ExpandableListView expandList;
	private LinearLayout nodata;
	private ImageButton returnBtn;
	private String[] group;
	private String[][] child;
	private int[][] gids;
	private int id;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_mycourselist2);
		userinfo = getSharedPreferences("userinfo", 0);
		id = userinfo.getInt("id", 0);
		username = getIntent().getStringExtra("username");
		dialog = ProgressDialog.show(QuestionFromCourseActivity.this,null,"Ŭ�����������Ժ�",true,false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		expandList = (ExpandableListView) this.findViewById(R.id.explist2);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		expandList.setGroupIndicator(null); 
		expandList.setOnChildClickListener(new ChildClickListener());
		expandList.setOnGroupClickListener(new GroupClickListener());
		nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		new GetMyLessonThread().start();
		handler = new MyHandler(this);
		
	}
	private class GetMyLessonThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				String result = HttpConnectUtil.httpGetRequest(QuestionFromCourseActivity.this, Constant.DOMAIN_URL+"mobile/myLessons?stuId="+id);
				if(result!=null&&!result.equals("null"))
            	{
            		//����json�ַ���,����expandableListView��adapter
            		try
            		{
            			JSONObject json = new JSONObject(result);
            			JSONArray packages = json.optJSONArray("classPackages");
            			JSONArray grades = json.getJSONArray("grade");
            			int plength = packages.length();
            			int glength = grades.length();
            			List<UserClass> list = new ArrayList<UserClass>();
            			//����,�ײͻ򵥰༶
            			group = new String[plength+glength];
            			//����,�ײ��µİ༶,���༶û������
            			//ע������ĳ��ȱȴ���Ķ�
            			child = new String[plength][];
            			//�γ���ϸ,����༶ʱ��ת
            			gids = new int[plength+glength][];
            			//ѭ���ײ�( classid,classname,username,fatherid,classtype)
            			for(int i=0;i<plength;i++)
            			{
            				JSONObject p = packages.getJSONObject(i);
            				group[i]=p.optString("pkgName");
            				JSONArray p_grades = p.getJSONArray("grade");
            				child[i] = new String[p_grades.length()];
            				gids[i] = new int[p_grades.length()];
            				UserClass c = new UserClass(p.optInt("pkgId")+"",p.optString("pkgName"),username,0+"",1+"");
            				list.add(c);
            				//ѭ���ײ��µİ༶
            				for(int k=0;k<p_grades.length();k++)
            				{
            					p = p_grades.getJSONObject(k);
            					child[i][k]=p.getString("name");
            					int gid = p.optInt("gradeId",0);
            					gids[i][k] = gid;
            				}
            			}
            			//ѭ���༶
            			for(int j=0;j<glength;j++)
            			{
            				JSONObject p = grades.getJSONObject(j);
            				group[j+plength]=p.optString("name");
            				int gid = p.optInt("gradeId",0);
            				gids[plength+j] = new int[1];
        					gids[plength+j][0] = gid;
            			}
            			handler.sendEmptyMessage(1);
            		}catch(Exception e)
            		{
            			e.printStackTrace();
            			handler.sendEmptyMessage(-2);
            		}
            	}else
            	{
            		handler.sendEmptyMessage(0);
            	}
			}catch(Exception e)
			{
				Message msg = handler.obtainMessage();
				msg.what = -1;
				handler.sendMessage(msg);
			}
		}
	}
	static class MyHandler extends Handler {
        WeakReference<QuestionFromCourseActivity> mActivity;
        MyHandler(QuestionFromCourseActivity activity) {
                mActivity = new WeakReference<QuestionFromCourseActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	QuestionFromCourseActivity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
        			theActivity.expandList.setAdapter(new MyExpandableAdapter2(theActivity, theActivity.group, theActivity.child));
                	break;
                case 0:
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//��������ʾ
            		Toast.makeText(theActivity, "��û�й���γ�", Toast.LENGTH_SHORT).show();//��ʾ
                	break;
                case -1:
                	//������,
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//��������ʾ
            		Toast.makeText(theActivity, "��ʱ�����Ϸ�����,���Ժ�", Toast.LENGTH_SHORT).show();//��ʾ
            		break;
                case -2:
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//��������ʾ
            		Toast.makeText(theActivity, "�������ݳ���", Toast.LENGTH_SHORT).show();//��ʾ
            		break;
                }
        }
	}
	private class ChildClickListener implements OnChildClickListener
	{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(QuestionFromCourseActivity.this,QuestionPaperListActivity.class);
			intent.putExtra("name",((TextView)v.findViewById(R.id.text3)).getText().toString());
			intent.putExtra("gid", QuestionFromCourseActivity.this.gids[groupPosition][childPosition]);
			intent.putExtra("username", username);
			QuestionFromCourseActivity.this.startActivity(intent);
			return true;
		}
	}
	private class GroupClickListener implements OnGroupClickListener
	{
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			// TODO Auto-generated method stub
			//���û��������,��ʾ�ǵ����Ŀγ�
			if(parent.getExpandableListAdapter().getChildrenCount(groupPosition)==0)
			{
				Intent intent = new Intent(QuestionFromCourseActivity.this,QuestionPaperListActivity.class);
				intent.putExtra("name",((TextView)v.findViewById(R.id.text3)).getText().toString());
				intent.putExtra("gid", QuestionFromCourseActivity.this.gids[groupPosition][0]);
				QuestionFromCourseActivity.this.startActivity(intent);
				return true;
			}
			return false;
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(dialog!=null)
		{
			dialog.dismiss();	
		}
		super.onDestroy();
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
