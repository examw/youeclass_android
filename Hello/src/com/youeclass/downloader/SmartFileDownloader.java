package com.youeclass.downloader;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.youeclass.dao.DownloadDao;
import com.youeclass.entity.DownloadItem;

/**
 * �ļ�������
 * 
 */
public class SmartFileDownloader {
	private static final String TAG = "SmartFileDownloadItem";
	private Context context;
	private String username;
	private DownloadDao DownloadDao;
	/* �������ļ����� */
	private int downloadSize = 0;
	/* ԭʼ�ļ����� */
	private int fileSize = 0;
	/* �߳��� */
	private SmartDownloadThread[] threads;
	/* ÿ���߳����صĳ��� */
	private int piece;
	/* ���ر����ļ� */
	private File saveFile;
	/* ����·�� */
	private String downloadUrl;
	/* ���� �߳����ݵļ��� */
	private List<DownloadItem> list;
	/* �Ƿ�û���� */
	private boolean isExist = false;
	private boolean isOver = false;
	/* �����������ͣ��ʶ */
	public static Map<String,Boolean> flagMap = new HashMap<String,Boolean>();
	/*handler*/
	private Handler mHandler;
	/**
	 * ��ȡ�߳���
	 */
	public int getThreadSize() {
		return threads.length;
	}
	/**
	 * ��ȡ�ļ���С
	 * 
	 * @return
	 */
	public int getFileSize() {
		return fileSize;
	}

	/**
	 * �ۼ������ش�С
	 * 
	 * @param size
	 */
	protected synchronized void append(int size) {
		downloadSize += size;
	}

	/**
	 * ����ָ���߳�������ص�λ��
	 */ 
	protected synchronized void update(DownloadItem loader) {
		this.DownloadDao.update(loader);
	}
	/**
	 * �����ļ�������
	 * 
	 * @param downloadUrl
	 *            ����·��
	 * @param fileSaveDir
	 *            �ļ�����Ŀ¼
	 * @param threadNum
	 *            �����߳���
	 */
	public SmartFileDownloader(Context context, String downloadUrl,
			File fileSaveDir, int threadNum,String username,Handler handler) {
		try {
			System.out.println("!!!! init the downloader !!!!");
			this.context = context;
			this.downloadUrl = downloadUrl;
			this.username = username;
			this.mHandler = handler;
			DownloadDao = new DownloadDao(this.context);
			URL url = new URL(this.downloadUrl);
			if (!fileSaveDir.exists()) {
				fileSaveDir.mkdirs();
			}
			this.threads = new SmartDownloadThread[threadNum];
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", downloadUrl);
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
			//printResponseHeader(conn);
			if (conn.getResponseCode() == 200) {
				this.fileSize = conn.getContentLength();// ������Ӧ��ȡ�ļ���С
				if (this.fileSize <= 0)
				{
					//throw new RuntimeException("Unkown file size ");
					mHandler.sendEmptyMessage(-3);
				}
				String filename = getFileName(conn);
				this.saveFile = new File(fileSaveDir, filename);/* �����ļ� */
				list = DownloadDao.findByUrl(downloadUrl,username);
				if (this.saveFile.exists()) {
					System.out.println(list.size());
					if (list.size() > 0) {
						// ��ʾ����û��������ɵ�
						if (list.size() == this.threads.length) {
							for (int i = 0; i < list.size(); i++) {
								// �Ѿ����صĳ���
								this.downloadSize += list.get(i)
										.getCompleteSize();
							}
							print("�Ѿ����صĳ���" + this.downloadSize);
						}
						isExist = true;
					} else {
						print("�ļ��Ѿ�����");
						//ɾ���ļ�,��������
						this.saveFile.delete();
					}
				} 
				// �ļ���������
				if(!this.saveFile.exists()){
					// ���ļ��ֿ�,�����������,�����ŵ����һ��
					this.piece = this.fileSize / this.threads.length; // ÿһ��Ĵ�С
					//��һ�� 0-(piece-1)
					//�ڶ��� piece*1-piece*2-1
					if (!isExist) {	
						DownloadDao.deleteAll(this.downloadUrl,username);//�ļ����ڼ�¼����û������
						list.clear();
						DownloadItem loader = new DownloadItem();
						loader.setStartPos(0);
						loader.setThreadId(1);
						loader.setEndPos(this.piece-1);
						loader.setCompleteSize(0);
						loader.setUrl(downloadUrl);
						loader.setUsername(username);
						list.add(loader);
						for (int i = 1; i < threads.length - 1; i++) {
							DownloadItem loader1 = new DownloadItem();
							loader1.setStartPos(this.piece*i);
							loader1.setThreadId(i + 1);
							loader1.setEndPos(this.piece*(i+1)-1);
							loader1.setCompleteSize(0);
							loader1.setUrl(downloadUrl);
							loader1.setUsername(username);
							list.add(loader1);
						}
						if (threadNum > 1) {
							DownloadItem loader2 = new DownloadItem();
							loader2.setStartPos(list.get(threadNum - 2)
									.getEndPos()+1);
							loader2.setEndPos(fileSize-1);
							loader2.setThreadId(threadNum);
							loader2.setCompleteSize(0);
							loader2.setUrl(downloadUrl);
							loader2.setUsername(username);
							list.add(loader2);
						}
						//�����ŵ��̱߳�������ݿ�
						DownloadDao.save(list);
					}
				}
			} else {
				//throw new RuntimeException("server no response ");
				//û����Ӧ
				//to do something !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				handler.sendEmptyMessage(-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			print(e.toString());
			//throw new RuntimeException("don't connection this url");
			//�޷�����
			//to do something !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			handler.sendEmptyMessage(-1);
		}
	}
	/**
	 * ��ȡ�ļ���
	 */
	private String getFileName(HttpURLConnection conn) {
		String filename = this.downloadUrl.substring(this.downloadUrl
				.lastIndexOf('/') + 1);
		if (filename == null || "".equals(filename.trim())) {// �����ȡ�����ļ�����
			for (int i = 0;; i++) {
				String mine = conn.getHeaderField(i);
				if (mine == null)
					break;
				if ("content-disposition".equals(conn.getHeaderFieldKey(i)
						.toLowerCase())) {
					Matcher m = Pattern.compile(".*filename=(.*)").matcher(
							mine.toLowerCase());
					if (m.find())
						return m.group(1);
				}
			}
			filename = UUID.randomUUID() + ".tmp";// Ĭ��ȡһ���ļ���
		}
		return filename;
	}

	/**
	 * ��ʼ�����ļ�
	 * 
	 * @param listener
	 *            �������������ı仯,�������Ҫ�˽�ʵʱ���ص�����,��������Ϊnull
	 * @return �������ļ���С
	 * @throws Exception
	 */
	public int download(SmartDownloadProgressListener listener)
			throws Exception {
		try {
			RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rw");
			if (this.fileSize > 0)
				randOut.setLength(this.fileSize);
			randOut.close();
			URL url = new URL(this.downloadUrl);
			for (int i = 0; i < this.list.size(); i++) {
				DownloadItem l = list.get(i);
				int downLength = l.getCompleteSize();
				int needLength = l.getEndPos() - l.getStartPos();
				if (downLength < needLength
						&& this.downloadSize < this.fileSize) {
					this.threads[i] = new SmartDownloadThread(this, url,
							list.get(i), this.saveFile);
					this.threads[i].setPriority(7);
					this.threads[i].start();
				} else {
					this.threads[i] = null;
				}
			}
			boolean notFinish = true;// ����δ���
			while (notFinish&&SmartFileDownloader.flagMap.get(downloadUrl)) {// ѭ���ж��Ƿ��������
				Thread.sleep(900);
				notFinish = false;// �ٶ��������
				for (int i = 0; i < this.threads.length; i++) {
					if (this.threads[i] != null && !this.threads[i].isFinish()) {
						notFinish = true;// ����û�����
						if (this.threads[i].getDownLength() == -1) {// �������ʧ��,����������
							this.threads[i] = new SmartDownloadThread(this,
									url, list.get(i), this.saveFile);
							this.threads[i].setPriority(7);
							this.threads[i].start();
						}
					}
				}
				if (listener != null)
					System.out.println(this.downloadSize);
					listener.onDownloadSize(this.downloadSize);
			}
			if(!notFinish)	//�Ѿ�������
			{
				DownloadDao.deleteAll(this.downloadUrl,this.downloadSize,this.saveFile.getPath(),username);
			}else
			{
				DownloadDao.updateCourse(this.downloadUrl,this.downloadSize,this.saveFile.getPath(),username);
			}
			isOver = true;
		} catch (Exception e) {
			e.printStackTrace();
			print(e.toString());
			throw new Exception("file download fail");
		}
		return this.downloadSize;
	}

	/**
	 * ��ȡHttp��Ӧͷ�ֶ�
	 * 
	 * @param http
	 * @return
	 */
	public static Map<String, String> getHttpResponseHeader(
			HttpURLConnection http) {
		Map<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null)
				break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		return header;
	}

	/**
	 * ��ӡHttpͷ�ֶ�
	 * 
	 * @param http
	 */
	public static void printResponseHeader(HttpURLConnection http) {
		Map<String, String> header = getHttpResponseHeader(http);
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
			print(key + entry.getValue());
		}
	}

	// ��ӡ��־
	private static void print(String msg) {
		Log.i(TAG, msg);
	}

	public int getCompleteSize() {
		// TODO Auto-generated method stub
		int size = 0;
		for (DownloadItem l : list) {
			size += l.getCompleteSize();
		}
		return size;
	}
	
	public boolean isStop()
	{
		boolean flag = true ;
		for(SmartDownloadThread t:threads)
		{
			//isAliveΪ���ʾΪ��߳�
			flag = flag&&!t.isAlive();
		}
		return flag&&isOver;
	}
	
	public File getSaveFile()
	{
		return this.saveFile;
	}
	public static int getDowningCount()
	{
		int count=0;
		for(Boolean b : flagMap.values())
		{
			if(b) count++;
		}
		return count;
	}
 }
