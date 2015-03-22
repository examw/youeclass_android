package com.youeclass.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class FileUtil {
	public static boolean checkSDCard(int fileSize)
	{
		File pathFile = Environment.getExternalStorageDirectory();
		if(!pathFile.exists())
		{
			return false;
		}
		if(fileSize>0)
		{
			StatFs statfs = new StatFs(pathFile.getPath());
			//��ÿɹ�����ʹ�õ�Block����
			long nAvailaBlock = statfs.getAvailableBlocks();
			//���SDCard��ÿ��block��SIZE
			long nBlocSize = statfs.getBlockSize();
			//����SDCardʣ���С Byte
			long nSDFreeSize = nAvailaBlock * nBlocSize;
			return nSDFreeSize > fileSize;
		}else
		{
			return true;
		}
	}
}
