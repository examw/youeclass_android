package com.youeclass.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.youeclass.R;
import com.youeclass.customview.DownloadButton;
import com.youeclass.downloader.SmartFileDownloader;
import com.youeclass.entity.DowningCourse;
import com.youeclass.service.DownloadService;
import com.youeclass.util.FileUtil;

public class DowningListAdapter extends BaseAdapter {
	private Context context;
	private List<DowningCourse> list;
	private String username;
	private SharedPreferences settingfile;
	public DowningListAdapter(Context context, List<DowningCourse> list,String username) {
		super();
		this.context = context;
		this.list = list;
		this.username = username;
		this.settingfile = context.getSharedPreferences("settingfile", 0);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub.Boolean flag =
		// SmartFileDownloader.flagMap.get(url);
		//if (convertView == null) {
		/*
		 * ��ʼ�������������
		 * һ��ʼ���ݿ⻹û��������,�����µ�,�ļ���С����
		 * �����ݵ����
		 */
			DowningListItem item = null;
			if(convertView==null)	//converView��������
			{
				LayoutInflater inflater = LayoutInflater.from(context);
				convertView = inflater.inflate(R.layout.list_downing_layout, null);
				item = new DowningListItem();
				// �ļ���
				item.filenameLab = (TextView) convertView
						.findViewById(R.id.filenameLab);
				// ������
				item.finishProgress = (ProgressBar) convertView
						.findViewById(R.id.finishProgress);
				item.finishProgress.setMax(100);
				// ������
				item.downing = (TextView) convertView
						.findViewById(R.id.fileDownText);
				// �ٷ���
				item.percent = (TextView) convertView
						.findViewById(R.id.fileFininshProgressLab);
				// ������
				item.connecting = (TextView) convertView
						.findViewById(R.id.finishSizeTextView);
				// ��ͣ�������ť
				item.pauseBtn = (DownloadButton) convertView
						.findViewById(R.id.pauseBtn);
				convertView.setTag(item);
			}else
			{
				item = (DowningListItem) convertView.getTag();
			}
			DowningCourse dc = list.get(position);
			System.out.println(dc.getFileurl()+" ����״̬ : "+dc.getStatus());
			item.filenameLab.setText(dc.getCourseName());
			//��ȡ����״̬
			Boolean flag = SmartFileDownloader.flagMap.get(dc.getFileurl());
			//Ϊ�ձ�ʾ��δ���ع�,Ϊ�ٱ�ʾ��ͣ��
			if(flag == null || !flag)
			{
				//��ʼ��,--��ͣ,
				if(dc.getStatus()==0)	//��ǰû���������״̬�궼Ϊ0;��ʾ����
				{
					int percentNum = (int) (dc.getFinishsize()*100.0/dc.getFilesize());
					item.percent.setText(percentNum	+ "%");
					item.finishProgress.setProgress(percentNum);
					// �������ص���û����������
					item.pauseBtn.setImageResource(R.drawable.continuedown);// ��ʾ������ť
					item.pauseBtn.setText(R.string.continueDown);// ��ʾ����
					item.downing.setText("��ͣ��");
				}else if (dc.getStatus()== -1) {	//��ʾ�ոռ������,��û�п�ʼ����
					item.connecting.setText("������...");
					item.downing.setText("");// ��û��ʼ����
					// ���ð�ť��������ȡ��
					//item.pauseBtn.setEnabled(false);
				}else if(dc.getStatus() == 4)
				{
					item.downing.setText("�ȴ���");
					item.pauseBtn.setImageResource(R.drawable.waitdown);// ��ʾ�ȴ���ť
					item.pauseBtn.setText(R.string.waitDown);// ��ʾ�ȴ�
//					item.pauseBtn.setEnabled(false);//���ð�ť
				}else if(dc.getStatus() == -2)
				{
					item.connecting.setText("����ʧ��!");
					item.pauseBtn.setImageResource(R.drawable.retry);
					item.pauseBtn.setText(R.string.retry);
				}
			}else	//��������
			{
				if(dc.getStatus()==1)	//�ոռ�������Ŀ�ʼ������
				{
					item.connecting.setText("");
					item.pauseBtn.setEnabled(true);
					//item.finishProgress.setProgress(0);
					//dc.setStatus(0);
				}
				dc.setStatus(1);
				//��������  
				// ���½�����ֵ
				int percentNum = (int) (dc.getFinishsize()*100.0/dc.getFilesize());
				item.percent.setText(percentNum	+ "%");
				//���½�����
				item.finishProgress.setProgress(percentNum);
				item.downing.setText("������");
				item.pauseBtn.setImageResource(R.drawable.pausedown);// ��ʾ������ť
				item.pauseBtn.setText(R.string.pauseDown);// ��ʾ����
				System.out.println("!!!!!!! update the progress !!!!!!!!!");
			}
			// ���ð�ť�¼�
			item.pauseBtn.setOnClickListener(new PauseClickEvent(position, list,
					item.downing));
		return convertView;
	}

	// �ж���������ͣ�ı�ʶ��ʲô
	/*
	 * map��ȡ���� url��Ӧ��ֵ,��ʾû�п�ʼ���� ȡ��Ϊfalse,��ͣ������ ȡ��Ϊtrue,��������
	 */
	private class PauseClickEvent implements OnClickListener {
		private TextView textView;
		private DowningCourse dc;

		public PauseClickEvent(int position, List<DowningCourse> l, TextView t) {
			// TODO Auto-generated constructor stub
			this.textView = t;
			this.dc = l.get(position);
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(dc.getStatus()==4||dc.getStatus()==-1) return;
			String url  = dc.getFileurl();
			Boolean flag = SmartFileDownloader.flagMap.get(url);
			// ֹͣ������
			if (flag == null || !flag) {
				// ��������
				if(SmartFileDownloader.getDowningCount()>=2)
				{
					dc.setStatus(4);  //�ȴ�״̬
					((DownloadButton) v).setImageResource(R.drawable.waitdown);// ��ʾ��ͣ��ť
					((DownloadButton) v).setText(R.string.waitDown);
					return;
				}
				Boolean wifiState = checkWifiNetworkInfo();
				Boolean isDownUse3G = settingfile.getBoolean("setDownIsUse3G", true);
				if(wifiState==null)
				{
					//print("������������");
					Toast.makeText(context, "������������", Toast.LENGTH_SHORT).show();
					return;
				}
				if(wifiState==false&&isDownUse3G==false)//û��wifi,�ֲ�����3G����
				{
					//print("��ǰ����Ϊ2G/3G,Ҫ�������޸����û���wifi");
					Toast.makeText(context, "��ǰ����Ϊ2G/3G,Ҫ�������޸����û���wifi", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!FileUtil.checkSDCard(dc.getFilesize()))
				{
					Toast.makeText(context, "û��SD�����߿ռ䲻��", Toast.LENGTH_SHORT).show();
					return;
				}
				SmartFileDownloader.flagMap.put(url, true);
				Intent intent = new Intent(context, DownloadService.class);
				intent.putExtra("url", url);
				File dir = Environment.getExternalStorageDirectory();
				intent.putExtra("dir", dir.getPath() + "/eschool");
				intent.putExtra("username", username);
				context.startService(intent);
				textView.setText("������");
				dc.setStatus(1);
				((DownloadButton) v).setImageResource(R.drawable.pausedown);// ��ʾ��ͣ��ť
				((DownloadButton) v).setText(R.string.pauseDown);
			} else {
				// ���㲥֪ͨ��̨service��ͣ
				SmartFileDownloader.flagMap.put(url, false);
				Intent myIntent = new Intent();// ����Intent����
				myIntent.setAction("commandFromActivity");
				myIntent.putExtra("cmd", 0);
				myIntent.putExtra("url", url);
				context.sendBroadcast(myIntent);// ���͹㲥
				textView.setText("��ͣ��");
				//����״̬��Ϊ0
				dc.setStatus(0);
				((DownloadButton) v).setImageResource(R.drawable.continuedown);// ��ʾ������ť
				((DownloadButton) v).setText(R.string.continueDown);// ��ʾ����
				//ͬʱ���Ҵ�����ͣ״̬������
				DowningCourse waiting = getFirstWait();
				if(waiting!=null)
				{
					SmartFileDownloader.flagMap.put(waiting.getFileurl(), true);
					Intent intent = new Intent(context, DownloadService.class);
					intent.putExtra("url", waiting.getFileurl());
					File dir = Environment.getExternalStorageDirectory();
					intent.putExtra("dir", dir.getPath() + "/eschool");
					intent.putExtra("username", username);
					context.startService(intent);
					waiting.setStatus(1);
					DowningListAdapter.this.notifyDataSetChanged();
				}
			}
		}
	}
	static class DowningListItem
	{
		TextView filenameLab,downing,percent,connecting;
		DownloadButton pauseBtn;
		ProgressBar finishProgress;
	}
	//����б��е�һ���ȴ�������
	private DowningCourse getFirstWait()
	{
		for(DowningCourse dc:list)
		{
			if(dc.getStatus()==4)
			{
				return dc;
			}
		}
		return null;
	}
	private Boolean checkWifiNetworkInfo()
	{
		ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		 //mobile 3G Data Network
        State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        //wifi
        State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        if(wifi==State.CONNECTED||wifi==State.CONNECTING)
        {
        	return true;
        }
        if(mobile==State.CONNECTED||mobile==State.CONNECTING)
        {
        	return false;
        }
		return null;
	}
}
