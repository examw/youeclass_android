package com.youeclass.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.youeclass.DownloadActivity;
import com.youeclass.R;
import com.youeclass.VideoActivity;

public class MyCourseListAdapter extends BaseAdapter{
	private Context context;
	private List<String> courses;
	private List<String> urls;
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return courses.size();
	}
	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return courses.get(position);
	}
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public android.view.View getView(final int position, android.view.View convertView, android.view.ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.courselist_layout, null);
		TextView name = (TextView) convertView.findViewById(R.id.text4);
		name.setText(courses.get(position));
		TextView isDown = (TextView) convertView.findViewById(R.id.Downprogresstext);
		isDown.setText("δ����");
		ImageButton btn = (ImageButton) convertView.findViewById(R.id.playerBtn);
		name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context,VideoActivity.class);
				intent.putExtra("name", courses.get(position));
				intent.putExtra("url", urls.get(position));
				context.startActivity(intent);
			}
		});
		btn.setOnClickListener(new ClickEvent(position,courses,urls));
		return convertView;
		
	};
	public MyCourseListAdapter(Context context,List<String> course,List<String> urls) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.courses = course;
		this.urls = urls;
	}
	private class ClickEvent implements OnClickListener
	{
		private int position;
		private List<String> courses,urls;
		public ClickEvent(int position,List<String> courses,List<String>urls) {
			this.position = position;
			this.courses = courses;
			this.urls = urls;
		}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			//���sd���Ƿ����,
			//��ȡ�ļ��Ĵ�С
			//���sd�Ŀ��������Ƿ�
			
//			File pathFile = Environment.getExternalStorageDirectory();
//			StatFs statfs = new StatFs(pathFile.getPath());
//			//��ÿɹ�����ʹ�õ�Block����
//			long nAvailaBlock = statfs.getAvailableBlocks();
//			//���SDCard��ÿ��block��SIZE
//			long nBlocSize = statfs.getBlockSize();
//			//����SDCardʣ���СMB
//			long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;
			Intent intent = new Intent(context,DownloadActivity.class);
			intent.putExtra("name", courses.get(position));
			intent.putExtra("url", urls.get(position));
			context.startActivity(intent);
		}
		
	}
}
