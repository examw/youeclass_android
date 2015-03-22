package com.youeclass.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.youeclass.downloader.SmartDownloadProgressListener;
import com.youeclass.downloader.SmartFileDownloader;

/**
 * service�ǿ�������Activity ���ط���,�ڷ����������߳̽�������,ͬʱע��㲥,��������Activity�Ĺ㲥��Ϣ ���߳��з���
 * �ض��Ĺ㲥��activity���н���,����UI
 * 
 * @author Administrator
 * 
 */
public class DownloadService extends Service {
	// private List<SmartFileDownloader> downloaderList;
	private static int downloaderCount;
	private static Handler mHandler;
	private Map<String, SmartFileDownloader> downloaderMap;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		cmdReceiver = new CommandReceiver();
		mHandler = new MyHandler(this);
		// ע��㲥
		IntentFilter filter = new IntentFilter();
		filter.addAction("commandFromActivity");// �������ʲô���Ĺ㲥
		registerReceiver(cmdReceiver, filter);
		this.downloaderMap = new HashMap<String, SmartFileDownloader>();
		super.onCreate();
	}

	// service�����ظ�����
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		downloaderCount++;
		if (downloaderCount >= 3) {
			Toast.makeText(this, "���������������ͬʱ����", Toast.LENGTH_LONG).show();
			return super.onStartCommand(intent, flags, startId);
		}
		String url = intent.getStringExtra("url");
		String dir = intent.getStringExtra("dir");
		String username = intent.getStringExtra("username");
		dojob(url, dir, username); // �����߳�ȥ����
		return super.onStartCommand(intent, flags, startId);
	}

	private void dojob(String id, String dir, String username) {
		new MyThread(id, new File(dir + "/" + username), username).start();
	}

	// ��һ���߳�����������
	private class MyThread extends Thread {
		private String url;
		private File dir;
		private String username;

		public MyThread(String id, File dir, String username) {
			// TODO Auto-generated constructor stub
			this.url = id;
			this.dir = dir;
			this.username = username;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			// ��һ��������,�������ڳ�ʼ��ʱ�����һЩ��ʱ�Ĳ���
			SmartFileDownloader loader = new SmartFileDownloader(
					DownloadService.this, url, dir, 2, username,
					DownloadService.mHandler);
			downloaderMap.put(url, loader);
			try {
				// �������ز��Ҽ������ؽ���
				System.out
						.println("!!!!!!!!!!!!!start the download listener!!!!!!!!!!!!");
				loader.download(new SmartDownloadProgressListener() {
					@Override
					public void onDownloadSize(int size) {
						// TODO Auto-generated method stub
						if (size > 0) {
							Intent intent = new Intent();// ����Intent����
							intent.setAction("updateUI"); // ����㲥����
							intent.putExtra("url", url); // ������
							intent.putExtra("data", size);
							sendBroadcast(intent);// ���͹㲥
							System.out
									.println("!!!!!downloadService set broadcase!!!!!!");
						}
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// ����ʧ��
			}
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// ע���㲥
		unregisterReceiver(cmdReceiver);
		downloaderMap.clear();
		super.onDestroy();
	}

	// ����һ���㲥������
	private class CommandReceiver extends BroadcastReceiver {
		// ��������Activity�Ĺ㲥,�Ӷ����Ƹ��������߳�
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int cmd = intent.getIntExtra("cmd", -1);
			final String url = intent.getStringExtra("url");
			final String path = intent.getStringExtra("path");
			if (cmd == 0) // ��ʾ��ͣ
			{
				// ������������ı�ʶ
				SmartFileDownloader.flagMap.put(url, false);// ֹͣ�߳�
				// downloaderMap.remove(url);
				downloaderCount--;
			} else if (cmd == 2) {
				// ������������ı�ʶ
				if (SmartFileDownloader.flagMap.get(url)) {
					SmartFileDownloader.flagMap.put(url, false);// ֹͣ�߳�
					new Thread() {
						public void run() {
							SmartFileDownloader l = downloaderMap.get(url);
							while (!l.isStop()) {
								try {
									Thread.sleep(100);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							Log.i("DownloadService", "�����������Ѿ�ֹͣ");
							File file = l.getSaveFile();
							file.delete();
							Log.i("DownloadService", file.exists() + "�ļ�������ô?");
							mHandler.sendEmptyMessage(3);
							downloaderMap.remove(url);
							downloaderCount--;
						};
					}.start();
				} else {
					SmartFileDownloader l = downloaderMap.get(url);
					File f = null;
					if (l == null) {
						f = new File(path);
					} else {
						f = l.getSaveFile();
						downloaderMap.remove(url);
					}
					if (f.exists()) {
						boolean flag = f.delete();
						Toast.makeText(DownloadService.this,
								"ɾ���ļ�" + (flag ? "�ɹ�" : "ʧ��"),
								Toast.LENGTH_SHORT).show();
					}
				}
			} else {
				// �������н���ͬʱֹͣ����
				stopAll();
				downloaderMap.clear();
				downloaderCount = 0;
				// ֹͣ����
				stopSelf();

			}
		}
	}

	private void stopAll() {
		Map<String, Boolean> map = SmartFileDownloader.flagMap;
		for (String s : map.keySet()) {
			map.put(s, false);
		}
		downloaderCount = 0;
	}

	// �������һ�������ߵ�����,Ϊע������㲥���������
	private CommandReceiver cmdReceiver;

	private static class MyHandler extends Handler {
		private WeakReference<DownloadService> weak;

		public MyHandler(DownloadService context) {
			// TODO Auto-generated constructor stub
			weak = new WeakReference<DownloadService>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case -1:
				Toast.makeText(weak.get(), "������û����Ӧ", Toast.LENGTH_SHORT)
						.show();
				break;
			case -2:
				Toast.makeText(weak.get(), "�޷����ӵ����ص�ַ", Toast.LENGTH_SHORT)
						.show();
				break;
			case -3:
				Toast.makeText(weak.get(), "δ֪�ļ���С", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(weak.get(), "ɾ���ɹ�", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
}
