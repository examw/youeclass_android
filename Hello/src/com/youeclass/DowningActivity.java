package com.youeclass;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.youeclass.adapter.DowningListAdapter;
import com.youeclass.dao.CourseDao;
import com.youeclass.downloader.SmartFileDownloader;
import com.youeclass.entity.DowningCourse;
import com.youeclass.service.DownloadService;
import com.youeclass.util.FileUtil;

public class DowningActivity extends BaseActivity{
	private ListView listView;
	private LinearLayout nodata;
	private List<DowningCourse> list;
	private BaseAdapter mAdapter;
	private CourseDao dao = new CourseDao(this);
	private Handler handler = new DowningHandler(this);
	private QuickActionPopupWindow actionbar;
	private ActionItem action_delete;
	private ActionBarClickListener listener;
	private String username;
	private DowningCourse theNewOne;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_downing);
		Intent intent = getIntent();
		String name=intent.getStringExtra("name");
		String url = intent.getStringExtra("url");
		this.username = intent.getStringExtra("username");
		this.listView = (ListView) this.findViewById(R.id.videoListView);
		this.nodata = (LinearLayout) this.findViewById(R.id.down_nodataLayout);
		//�ҳ����ݿ��������������صĿγ�
		list = initList();
		System.out.println("list size = "+list.size()+"!!!!!!!!!");
		//��ʼ���ӿγ��б��е����Ҫ������
		if(name!=null&&url!=null)
		{
			theNewOne = new DowningCourse();
			theNewOne.setCourseName(name);
			//���Ե�ַ
			theNewOne.setFileurl(url);
			theNewOne.setStatus(-1);
			theNewOne.setUsername(username);
			//�������������
			if(!list.contains(theNewOne))
				list.add(theNewOne);
			else
				theNewOne = null;
		}
		//��������Դ
		mAdapter = new DowningListAdapter(this,list,username);
		this.listView.setAdapter(mAdapter);
		//��һ���߳�ȥ���ӻ�ȡ�ռ������ص��ļ���С
		if(theNewOne!=null)	//ԭ��û�е�	,
		{
			new GetFileSizeThread().start();
		}
		if(list==null||list.size()==0)
		{
			//���ݿ���û�����������е�����
			this.nodata.setVisibility(View.VISIBLE);
		}
		//��������ȡ�����ص�PopupWindow
		this.listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				showWindow(arg1,list.get(arg2));
				return false;
			}
		});
	}
	private class GetFileSizeThread extends Thread
	{
		public void run() {
			try {
				String path = theNewOne.getFileurl();
				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				conn.connect();
				if (conn.getResponseCode() == 200) {
					int fileSize = conn.getContentLength();// ������Ӧ��ȡ�ļ���С
					if (fileSize <= 0)
					{
						Message errorMsg = handler.obtainMessage();
						errorMsg.what = -3;
						errorMsg.obj = "δ֪�ļ���С";
						handler.sendMessage(errorMsg);
						return;
						//throw new RuntimeException("Unkown file size ");
					}
					theNewOne.setFilesize(fileSize);
					File dir = Environment.getExternalStorageDirectory();
					String filePath = dir.getPath()+File.separator+"eschool"+File.separator+username+File.separator+"video";
					theNewOne.setFilePath(filePath);
					Message msg = handler.obtainMessage();
					Bundle data = new Bundle();
					data.putString("path", path);
					msg.what = 1;
					msg.arg1 = fileSize; 
					msg.setData(data);
					handler.sendMessage(msg);
				}else
				{
					System.out.println("�����Ϸ�����");
					Message errorMsg = handler.obtainMessage();
					errorMsg.what = -1;
					errorMsg.obj = "�����Ϸ�����";
					handler.sendMessage(errorMsg);
				}
			}catch(Exception e)
			{
				e.printStackTrace();
				Message errorMsg = handler.obtainMessage();
				errorMsg.what = -2;
				errorMsg.obj = "���ص�ַ���ӳ�ʱ";
				handler.sendMessage(errorMsg);
			}
		};
	}
	//��ȡ�������ص���������
	private int getDowningSize()
	{
		int count = 0;
		for(DowningCourse dc:list)
		{
			if(dc.getStatus()==1) //��ʾ��������
			{
				count++;
			}
		}
		return count;
	}
	private static class DowningHandler extends Handler
	{
		WeakReference<DowningActivity> mActivity;

		DowningHandler(DowningActivity activity) {
			mActivity = new WeakReference<DowningActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			DowningActivity theActivity = mActivity.get();
			switch(msg.what)
			{
			case 1:
				//dao�����ļ�����,�Լ�����״̬[û�л�ȡ���ļ����ȱ�ʾû������]
				Bundle data = msg.getData();
				String url = data.getString("path");
				if(!FileUtil.checkSDCard(msg.arg1))
				{
					Toast.makeText(theActivity, "û��SD�����߿ռ䲻��", Toast.LENGTH_SHORT).show();
					return;
				}
				File dir = Environment.getExternalStorageDirectory();
				String filePath = dir.getPath()+File.separator+"eschool"+File.separator+theActivity.username+File.separator+"video";
				theActivity.dao.updateFileSize(url, msg.arg1,1,filePath,theActivity.username);
				theActivity.mAdapter.notifyDataSetChanged();
				int count = theActivity.getDowningSize();
				if(count>=2)
				{
					theActivity.theNewOne.setStatus(4);
					theActivity.mAdapter.notifyDataSetChanged();
				}else{
					theActivity.theNewOne.setStatus(1);
					theActivity.mAdapter.notifyDataSetChanged();
					//��������,֪ͨ����
					SmartFileDownloader.flagMap.put(url, true);
					//!!!��ʱ����������
					Intent intent = new Intent(theActivity,DownloadService.class);
					intent.putExtra("url", url);
					intent.putExtra("dir",filePath);
					intent.putExtra("username", theActivity.username);
					theActivity.startService(intent);
				}
				break;
			case -1:
				Toast.makeText(theActivity, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case -2:
				Toast.makeText(theActivity, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
			case -3:
				Toast.makeText(theActivity, (String)msg.obj, Toast.LENGTH_SHORT).show();
				break;
				
			}
		}
	}
	private List<DowningCourse> initList()
	{
		return dao.findAllDowning(username);
	}
	//����㲥�����߽��չ㲥  ���ڸ����б�����
	private class DataReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String url = intent.getStringExtra("url");
    		int data = intent.getIntExtra("data", -1); //ʵʱ����������
    		updateListView(url,data);
		}
	}
	private void updateListView(String url,int data)
	{
		DowningCourse d = null;
		for(DowningCourse dc:list)
		{
			if(dc.getFileurl().equals(url))
			{
				d = dc;
				break;
			}
		}
		if(d!=null)
		{
			d.setFinishsize(data==-1?0:data);
			if(d.getFilesize()==data)
			{
				//��list��ɾ���������
				list.remove(d);	
				//��ʾ�������
				Toast.makeText(this, d.getCourseName()+"�������", Toast.LENGTH_LONG).show();
			}
		}
		mAdapter.notifyDataSetChanged();
		System.out.println("!!!!!! fresh listView!!!!!!");
	}
	private DataReceiver dataReceiver ;
	@Override
	protected void onStart() {
		// TODO Auto-generated method stu
		dataReceiver = new DataReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("updateUI");
		registerReceiver(dataReceiver, filter);
		//��ʼ������
		super.onStart();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
//		ע���㲥
		unregisterReceiver(dataReceiver);
//		//���㲥,֪ͨservice�������е��߳�,ͬʱ�����Լ�
//        Intent myIntent = new Intent();
//        myIntent.setAction("commandFromActivity");  
//        sendBroadcast(myIntent);//���͹㲥  
		super.onStop();
	}
	private void showWindow(View v,DowningCourse dc) {
		if(actionbar == null)
		{
			actionbar = new QuickActionPopupWindow(DowningActivity.this);
			action_delete = new ActionItem();
			action_delete.setTitle("ȡ��");
			action_delete.setIcon(getResources().getDrawable(
					R.drawable.action_delete));
			actionbar.addActionItem(action_delete);
			// ���ö������
			actionbar.setAnimStyle(QuickActionPopupWindow.ANIM_AUTO);
		}
		if(listener == null)
		{
			listener = new ActionBarClickListener(dc);
		}else
		{
			listener.setDc(dc);
		}
		action_delete.setClickListener(listener); 
		// ��ʾ
		actionbar.show(v);
		}
	private class ActionBarClickListener implements OnClickListener
	{
		private DowningCourse dc;
		public ActionBarClickListener(DowningCourse dc) {
			// TODO Auto-generated constructor stub
			this.dc = dc;
		}
		public void setDc(DowningCourse dc) {
			this.dc = dc;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//�����Ƿ�ȷ��ɾ��
			AlertDialog dialog = new AlertDialog.Builder(DowningActivity.this)
			.setTitle("ɾ���ļ�")
			.setMessage("�Ƿ�ȷ��ȡ�����ز�ɾ���ļ�")
			.setPositiveButton("ȷ��", new  DialogButtonClick(dc))
			.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			}).create();
			dialog.show();
		}
	}
	private class DialogButtonClick implements DialogInterface.OnClickListener
	{
		private DowningCourse dc;
		public DialogButtonClick(DowningCourse dc) {
			// TODO Auto-generated constructor stub
			this.dc = dc;
		}
		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			dialog.cancel();
			actionbar.dismiss();
			//to do something
			Log.i("DownFinish","ɾ�������������ļ�");
			if(SmartFileDownloader.flagMap.get(dc.getFileurl())!=null)
			{
			//ֹͣ����,��ɾ���ļ�
			Intent myIntent = new Intent();// ����Intent����
			myIntent.setAction("commandFromActivity");
			myIntent.putExtra("cmd", 2);
			myIntent.putExtra("url", dc.getFileurl());
			myIntent.putExtra("path", dc.getFilePath());
			sendBroadcast(myIntent);// ���͹㲥
			}
			dao.deleteDowingCourse(dc.getFileurl(),dc.getUsername());
			//downloadTab�еļ�¼ɾ��
			list.remove(dc);
			System.out.println("list size = "+list.size());
			mAdapter.notifyDataSetChanged();
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
