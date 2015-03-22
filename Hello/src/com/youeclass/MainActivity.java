package com.youeclass;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.app.AppContext;

public class MainActivity extends BaseActivity{
	 private GridView g;
	 private boolean isLocalLogin;
	 private String username;
	 private int uid;
	 private AppContext appContext;
	 protected void onCreate(Bundle paramBundle)
	  {
	    super.onCreate(paramBundle);
	    setContentView(R.layout.activity_mian);
	    Intent intent = this.getIntent();
	    isLocalLogin = "local".equals(intent.getStringExtra("loginType"));
	    this.username = intent.getStringExtra("MAP_USERNAME");
	    uid = intent.getIntExtra("uid", 0);
	    g = (GridView) this.findViewById(R.id.gridview1);
		g.setAdapter(new MyAdapter());
		appContext = (AppContext) getApplication();
		//g.setOnItemClickListener(new ItemClickListener());
	  }
	 //��ʼ����Ŀ
	 private class MyAdapter extends BaseAdapter
	 {
		private LayoutInflater inflater = LayoutInflater.from(MainActivity.this);;
		private int[] imagebtns ={R.drawable.mycourse_state,R.drawable.experience_center_state,R.drawable.question_bank_state,R.drawable.answer_sheet_state,R.drawable.palyrecord_state,R.drawable.ssuggestion_state};
		private int[] texts = {R.string.mycourse,R.string.experience_center,R.string.question_bank,R.string.answer_sheet,R.string.LearningRecord,R.string.suggestionStr};
		private Class[] classes = { MyCourseActivity.class, Class1Activity.class, QuestionMainActivity.class, AnswerMainActivity.class, PlayrecordActivity.class, SuggestionActivity.class };
		 @Override
		public int getCount() {
			// TODO Auto-generated method stub
			 return imagebtns.length;
		 }
		 @Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return Integer.valueOf(this.imagebtns[position]);
		}
		 @Override
		public View getView(int position, View paramView, ViewGroup parent) {
			// TODO Auto-generated method stub
			    if (paramView == null)
			       paramView = this.inflater.inflate(R.layout.gridviewlayout, null);
			      ImageButton localImageButton = (ImageButton)paramView.findViewById(R.id.ImgBtn);
			      TextView localTextView = (TextView)paramView.findViewById(R.id.Imglab);
			      localImageButton.setImageResource(this.imagebtns[position]);
			      localImageButton.setOnClickListener(new ItemClickListener(this.classes[position]));
			      localTextView.setText(this.texts[position]);
			      return paramView;
		}
		 @Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	 }
	 //Ϊÿ����Ŀ���ü�������
	 private class ItemClickListener implements OnClickListener
	{
		 private Class c;
		 public ItemClickListener(Class c) {
			// TODO Auto-generated constructor stub
			 this.c = c;
		}
		 @Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			 if(isLocalLogin)
			 {
				 if(c.equals(MyCourseActivity.class))
				 {
					 Intent intent = new Intent(MainActivity.this,c);
					 intent.putExtra("username", username);
					 intent.putExtra("loginType", "local");
					 MainActivity.this.startActivity(intent);
					 return;
				 }
				 if(c.equals(QuestionMainActivity.class))
				 {
					 Intent intent = new Intent(MainActivity.this,c);
					 intent.putExtra("username", username);
					 intent.putExtra("loginType", "local");
					 MainActivity.this.startActivity(intent);
					 return;
				 }
				 Toast.makeText(MainActivity.this, "�����ߵ�¼", Toast.LENGTH_SHORT).show();
				 return;
			 }
			 Log.v("debug", "����activity"+c.toString());
			 Intent intent = new Intent(MainActivity.this,c);
			 intent.putExtra("username", username);
			 intent.putExtra("uid", uid);
			 MainActivity.this.startActivity(intent);
		}
	}
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
	{
	    if ((paramKeyEvent.getKeyCode() == 4) && (paramKeyEvent.getRepeatCount() == 0))
	    {
	      showDialog ();
	      return true;
	    }
	    return super.onKeyDown(paramInt, paramKeyEvent);
	  }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		//���㲥,֪ͨ���ط���service�������е��߳�,ͬʱ�����Լ�
        Intent myIntent = new Intent();
        myIntent.setAction("commandFromActivity");  
        sendBroadcast(myIntent);//���͹㲥  
		super.onDestroy();
	}
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(Menu.NONE, Menu.FIRST+1, 1, "����").setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, Menu.FIRST+2, 2, "����").setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, Menu.FIRST+4, 4, "ע��").setIcon(android.R.drawable.ic_menu_set_as);
		return true;
	}
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		 switch(item.getItemId()){
		 case Menu.FIRST+1:
			 //����
			 startSettingActivity();
		 	 break;
		 case Menu.FIRST+2:
			 //����
			 this.startActivity(new Intent(this,HelpActivity.class));
			 break;
		 case Menu.FIRST+4:
			 showDialog();
		 		break;
		 }
		 return true;
		 
	 }
	 /**
		 * �Զ���һ����Ϣ��ʾ����
		 * @param msg
		 */
		protected void showDialog(){
			 AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			 localBuilder.setTitle("ע��").setMessage("�Ƿ�ע���û�").setCancelable(false).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO Auto-generated method stub
						//ֹͣ���ط���
						//Toast.makeText(this, "������ע��", Toast.LENGTH_LONG).show();
						MobclickAgent.onEvent(MainActivity.this,"LoginOut");
						appContext.cleanLoginInfo();
						MainActivity.this.startActivity(new Intent(MainActivity.this,LoginActivity.class));
						MainActivity.this.finish();
						
					}                      
				}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}                      
				});
			 localBuilder.create().show();
		}
		protected void startSettingActivity(){
			Intent intent = new Intent(this,SettingActivity.class);
			intent.putExtra("username", username);
			this.startActivity(intent);
		};
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