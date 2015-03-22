package com.youeclass.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.youeclass.R;

public class DownloadButton extends LinearLayout{
	private ImageView iv;
	private TextView tv;
	public DownloadButton(Context context) {
		// TODO Auto-generated constructor stub
		this(context,null);
	}
	public DownloadButton(Context context, AttributeSet  attrs) {
		// TODO Auto-generated constructor stub
		 super(context, attrs); 
	        // ���벼�� 
	        LayoutInflater.from(context).inflate(R.layout.download_pause_continue_btn, this, true); 
	        iv = (ImageView) findViewById(R.id.btnImg); 
	        tv = (TextView) findViewById(R.id.btnTxt);  
	       
	}
	 /**
     * ����ͼƬ��Դ
     */ 
    public void setImageResource(int resId) { 
        iv.setImageResource(resId); 
    } 
 
    /**
     * ������ʾ������
     */ 
    public void setText(String text) { 
        tv.setText(text); 
    }  
	public void setText(int resId){
		tv.setText(resId);
	}
}
