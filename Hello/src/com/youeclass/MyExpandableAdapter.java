package com.youeclass;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class MyExpandableAdapter extends BaseExpandableListAdapter{
	private Context context;
	private String[] groups;
	private String[][] children;
	public MyExpandableAdapter(Context context,String[] group,String[][]child) {
		this.children = child;
		this.context= context;
		this.groups = group;
	}
	 //���ָ�����е�ָ����������ѡ������
	 public Object getChild(int groupPosition, int childPosition) {
		 try{
		 return children[groupPosition][childPosition];
		 }catch(Exception e)
		 {
			 return null;
		 }
	 }
	 //���ָ�������ID
	 public long getChildId(int groupPosition, int childPosition) {
	  return childPosition;
	 }
	     //���ָ�������view���
	 public View getChildView(int groupPosition, int childPosition,
	   boolean isLastChild, View convertView, ViewGroup parent) {
		 	LayoutInflater inflater = LayoutInflater.from(context);
		 	convertView = inflater.inflate(R.layout.listlayout_3, null);
		 	TextView txt = (TextView) convertView.findViewById(R.id.text3);
		 	txt.setText(getChild(groupPosition,childPosition).toString());
		 	return convertView;
	 }
	    //ȡ��ָ��������������ĸ���
	 public int getChildrenCount(int groupPosition) {
		 try{
			 return children[groupPosition].length;
		 }catch(Exception e)
		 {
			 return 0;
		 }
	 }
	     //ȡ��ָ���������
	 public Object getGroup(int groupPosition) {
	  return groups[groupPosition];
	 }
	  //ȡ��ָ����ĸ���
	 public int getGroupCount() {
	  return groups.length;
	 }
	  //ȡ��ָ��������ID
	 public long getGroupId(int groupPosition) {
	  return groupPosition;
	 }
	  //ȡ��ָ�����View���
	 public View getGroupView(int groupPosition, boolean isExpanded,
	   View convertView, ViewGroup parent) {
		 	LayoutInflater inflater = LayoutInflater.from(context);
		 	convertView = inflater.inflate(R.layout.listlayout_2, null);
		 	TextView txt = (TextView) convertView.findViewById(R.id.text2);
		 	txt.setText(groups[groupPosition]);
		 	return convertView;
	 }
	      //�������true��ʾ��������IDʼ�ձ�ʾһ���̶����������
	 public boolean hasStableIds() {
	  return true;
	 }
	//�ж�ָ������ѡ�����Ƿ�ѡ��
	 public boolean isChildSelectable(int groupPosition, int childPosition) {
	  return true;
	 }
}
