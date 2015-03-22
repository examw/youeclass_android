package com.youeclass;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.umeng.analytics.MobclickAgent;
import com.youeclass.adapter.PopRuleListAdapter;
import com.youeclass.customview.CheckBoxGroup;
import com.youeclass.customview.MyCheckBox;
import com.youeclass.dao.PaperDao;
import com.youeclass.entity.ExamFavor;
import com.youeclass.entity.ExamQuestion;
import com.youeclass.entity.ExamRecord;
import com.youeclass.entity.ExamRule;

/**
 * ������� �м������������ô˽��� doExam, examTitle ��ʾ�Ծ����,���ؼ���ʾ�Ƿ��˳�����,�����ǽ���ť
 * �����ȼӿ��Լ�¼,ѡ��Ҫ���Ƿ��д�,�𰸵ĳ�ʼ�� doErrors,examTitle��ʾ���⼯,���ؼ�ֱ��finish,�����Ǵ𰸰�ť
 * doFavors,examTitle��ʾ�ҵ��ղ�,���ؼ�ֱ��finish,�����Ǵ𰸰�ť
 * doNotes,examTitle��ʾ�ҵıʼ�,���ؼ�ֱ��finish,�����Ǵ𰸰�ť,ѡ��û��,������û��
 * 
 * @author Administrator
 * 
 */
public class QuestionDoExamActivity extends Activity implements
		OnClickListener, OnGestureListener {
	// ���
	private ImageButton exitExamImgBtn, notebookImgBtn, nextBtn, preBtn,
			removeBtn, answerBtn, favoriteBtn;
	private TextView timeCountDown, examTitle, examTypeTextView,
			myAnswerTextView, sysAnswerTextView, analysisTextView;
	private ImageView answerResultImg;
	private Button chooseQuestionBtn, submitExamBtn;
	private GestureDetector mGestureDetector;
	private ScrollView scrollView;
	private LinearLayout nodataLayout, loadingLayout, ruleTypeLayout,
			modeLayout1, modeLayout2, modeLayout3, modeLayout4;
	private TextView examContent1, examContent2, examContent3;
	private EditText answerEditText;
	private CheckBoxGroup examOption2;
	private LinearLayout examAnswerLayout, examAnswerLayout2, examImages1,
			examImages2, examImages3, examAnswerLayout3;
	private RadioGroup examOption1;
	private Handler timeHandler;
	// ����
	private String papername, username;
	private String paperid;
	private String action;
	private String ruleListJson;
	private StringBuffer favorQids;
	private int paperTime, time, paperScore;
	private List<ExamRule> ruleList;
	private List<ExamQuestion> questionList;
	private ExamQuestion currentQuestion;
	private int questionCursor;
	private ExamRule currentRule;
	private StringBuffer answerBuf, txtAnswerBuf;
	private ExamRecord record;
	private SparseBooleanArray isDone;
	private Gson gson;
	private static boolean timerFlag = true;
	private ExamFavor favor;
	// ѡ�񵯳���
	private PopupWindow popupWindow;
	private ListView lv_group;
	private AlertDialog exitDialog;
	// ��ʾ����
	private PopupWindow tipWindow;
	private Handler mHandler;
	private SharedPreferences guidefile;
	// ���ݿ����
	private PaperDao dao;
	// ͼƬ����Ŀ¼
	private String imageSavePath;
	private ProgressDialog proDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_question_doexam);
		// ���ݳ�ʼ��
		Intent intent = this.getIntent();
		this.paperid = intent.getStringExtra("paperId");
		this.papername = intent.getStringExtra("paperName");
		this.ruleListJson = intent.getStringExtra("ruleListJson");
		this.username = intent.getStringExtra("username");
		this.paperTime = intent.getIntExtra("tempTime", 0);
		this.time = intent.getIntExtra("paperTime", 0) * 60; // ��
		this.paperScore = intent.getIntExtra("paperScore", 0);
		this.action = intent.getStringExtra("action");
		this.questionCursor = intent.getIntExtra("cursor", 0);
		// /mnt/sdcard/eschool/hahaha/image/1001
		imageSavePath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "eschool" + File.separator + username
				+ File.separator + "image" + File.separator + paperid;
		gson = new Gson();
		Type questionType = new TypeToken<ArrayList<ExamQuestion>>() {
		}.getType();
		Type ruleType = new TypeToken<ArrayList<ExamRule>>() {
		}.getType();
		this.questionList = gson.fromJson(
				intent.getStringExtra("questionListJson"), questionType);
		this.ruleList = gson.fromJson(ruleListJson, ruleType);
		if (dao == null)
			dao = new PaperDao(this);
		initView();
		if (favor == null)
			favor = new ExamFavor(username, paperid);
		this.favorQids = dao.findFavorQids(username, paperid);
		// ����action�Ĳ�ͬ,����
		if ("DoExam".equals(action)) {
			this.record = dao.insertRecord(new ExamRecord(paperid, username));
			isDone = this.record.getIsDone() == null ? new SparseBooleanArray()
					: gson.fromJson(this.record.getIsDone(),
							SparseBooleanArray.class);
			String tempAnswer = record.getTempAnswer();
			if (tempAnswer == null) {
				answerBuf = new StringBuffer();
				txtAnswerBuf = new StringBuffer();
			} else if (tempAnswer.indexOf("   ") == -1) {
				answerBuf = new StringBuffer(tempAnswer);
				txtAnswerBuf = new StringBuffer();
			} else {
				answerBuf = new StringBuffer(tempAnswer.substring(0,
						tempAnswer.indexOf("   ")));
				txtAnswerBuf = new StringBuffer(tempAnswer.substring(tempAnswer
						.indexOf("   ") + 3));
			}
			initQuestionAnswer(tempAnswer);
			this.examTitle.setText(this.papername); // �Ծ�����
		} else if ("myNoteBook".equals(action)) {
			this.examTitle.setText("�ҵıʼ�");
			this.examTypeTextView.setVisibility(View.GONE);
			this.chooseQuestionBtn.setVisibility(View.GONE);// ѡ��
		} else if ("myErrors".equals(action)) {
			this.examTitle.setText("���⼯");
			this.examTypeTextView.setVisibility(View.GONE);
			this.removeBtn.setVisibility(View.VISIBLE);
		} else if ("myFavors".equals(action)) {
			this.examTitle.setText("�ҵ��ղ�");
			this.examTypeTextView.setVisibility(View.GONE);
		} else if ("showNoteSource".equals(action)) {
			this.examTitle.setText("�ҵıʼ�");
			this.examTypeTextView.setVisibility(View.GONE);
		}
		mGestureDetector = new GestureDetector(this, this);
		this.preBtn.setOnClickListener(this);
		this.nextBtn.setOnClickListener(this);
		this.removeBtn.setOnClickListener(this);
		this.notebookImgBtn.setOnClickListener(this);
		this.exitExamImgBtn.setOnClickListener(this);
		this.chooseQuestionBtn.setOnClickListener(this);
		this.answerBtn.setOnClickListener(this);
		this.favoriteBtn.setOnClickListener(this);
		// ȥ���������������
		// this.scrollView.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// // TODO Auto-generated method stub
		// return mGestureDetector.onTouchEvent(event);
		// }
		// });
		this.scrollView.setFocusable(true);
		this.scrollView.setClickable(true);
		this.scrollView.setLongClickable(true);
		mGestureDetector.setIsLongpressEnabled(true);
		timeHandler = new TimerHandler(this);
		guidefile = this.getSharedPreferences("guidefile", 0);
		int firstExam = guidefile.getInt("isFirstExam", 0);
		if (firstExam == 0) {
			mHandler = new Handler();
			openPopupwin();
		}
	}

	// ȡ������������,ֻȡ�ò�����
	private void initView() {
		this.exitExamImgBtn = (ImageButton) this
				.findViewById(R.id.exitExamImgBtn);// �˳�����
		this.notebookImgBtn = (ImageButton) this
				.findViewById(R.id.notebook_ImgBtn);// �ʼǰ�ť
		this.preBtn = (ImageButton) this.findViewById(R.id.previousBtn); // ��һ��
		this.nextBtn = (ImageButton) this.findViewById(R.id.nextBtn); // ��һ��
		this.favoriteBtn = (ImageButton) this.findViewById(R.id.favoriteBtn);
		this.removeBtn = (ImageButton) this.findViewById(R.id.removeBtn);
		this.timeCountDown = (TextView) this
				.findViewById(R.id.timecount_down_TextView);// ����ʱ
		this.examTitle = (TextView) this.findViewById(R.id.examTitle_TextView);// ���Ա���
		this.chooseQuestionBtn = (Button) this
				.findViewById(R.id.selectTopicId_ImgBtn);// ѡ��
		this.examTypeTextView = (TextView) this
				.findViewById(R.id.examTypeTextView);// �������
		this.ruleTypeLayout = (LinearLayout) this
				.findViewById(R.id.ruleTypeLayout);
		this.scrollView = (ScrollView) this
				.findViewById(R.id.ContentscrollView);
		this.examAnswerLayout = (LinearLayout) this
				.findViewById(R.id.exam_answer_layout);
		this.submitExamBtn = (Button) this.findViewById(R.id.submitExamBtn); // �ύ��
		this.answerBtn = (ImageButton) this.findViewById(R.id.answerBtn);// ������߲鿴��
		this.analysisTextView = (TextView) this
				.findViewById(R.id.exam_analysisTextView);
		this.myAnswerTextView = (TextView) this
				.findViewById(R.id.myAnswerTextView);
		this.sysAnswerTextView = (TextView) this
				.findViewById(R.id.sysAnswerTextView);
		this.answerResultImg = (ImageView) this
				.findViewById(R.id.answerResultImg);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.nodataLayout.setVisibility(8);
		this.loadingLayout = (LinearLayout) this
				.findViewById(R.id.loadingLayout);
		this.loadingLayout.setVisibility(8);
		// this.contentLayout = (LinearLayout) this
		// .findViewById(R.id.examContentLayout);
		this.modeLayout1 = (LinearLayout) this
				.findViewById(R.id.doexam_mode1layout);
		initModeLayout1();
		this.modeLayout2 = (LinearLayout) this
				.findViewById(R.id.doexam_mode2layout);
		initModeLayout2();
		this.modeLayout2.setVisibility(8);
		this.modeLayout3 = (LinearLayout) this
				.findViewById(R.id.doexam_mode3layout);
		initModeLayout3();
		this.modeLayout3.setVisibility(8);
		this.modeLayout4 = (LinearLayout) this
				.findViewById(R.id.doexam_mode4layout);
		this.modeLayout4.setVisibility(8);
	}

	// ��ѡ��Ĳ���
	private void initModeLayout1() {
		this.examContent1 = ((TextView) findViewById(R.id.exam_Content)); // exam_Content1
		// /
		this.examImages1 = (LinearLayout) findViewById(R.id.examImages1);

		this.examOption1 = ((RadioGroup) findViewById(R.id.examOption)); // examOption1
		// this.examListView1 = ((ListView) findViewById(R.id.exam_ListView1));
		// // exam_ListView1
	}

	// ��ѡ��
	private void initModeLayout2() {
		this.examContent2 = (TextView) this.findViewById(R.id.exam_Content2);// ��Ŀ����
		this.examOption2 = (CheckBoxGroup) this.findViewById(R.id.examOption2);// checkbox�������
//		this.examAnswerLayout2 = (LinearLayout) this
//				.findViewById(R.id.exam_answer_layout2);
		this.examImages2 = (LinearLayout) findViewById(R.id.examImages2);
		// this.examAnswerLayout2.setVisibility(8);
	}

	// �ʴ���
	private void initModeLayout3() {
		this.examContent3 = (TextView) this.findViewById(R.id.exam_Content3);
		this.answerEditText = (EditText) this
				.findViewById(R.id.exam_answerEditText);
//		this.examAnswerLayout3 = (LinearLayout) this
//				.findViewById(R.id.exam_answer_layout3);
		this.examImages3 = (LinearLayout) findViewById(R.id.examImages3);
		// this.examAnswerLayout3.setVisibility(8);
	}

	private void showContent() {
		currentQuestion = questionList.get(questionCursor);
		if (ruleList != null && ruleList.size() > 0) {
			currentRule = ruleList
					.get(ruleList.indexOf(new ExamRule(currentQuestion
							.getRuleId(), currentQuestion.getPaperId())));
			this.examTypeTextView.setText(currentRule.getRuleTitle());
		}
		this.examImages1.removeAllViews();
		String type = currentQuestion.getQType();
		String answer = currentQuestion.getUserAnswer();
		// String str = currentQuestion.getQid()+"-";
		// String answer = null;
		// String tempAnswer = record.getTempAnswer();
		// if(tempAnswer!=null&&tempAnswer.indexOf(str)!=-1)
		// {
		// String temp = tempAnswer.substring(tempAnswer.indexOf(str));
		// if("�ʴ���".equals(type))
		// {
		// answer = temp.substring(str.length(),temp.indexOf("   "));
		// }else
		// answer= temp.substring(str.length(),temp.indexOf("&"));
		// }
		if ("��ѡ��".equals(type)) {
			this.modeLayout1.setVisibility(0);
			this.modeLayout2.setVisibility(8);
			this.modeLayout3.setVisibility(8);
			this.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "")
					.replaceAll("[A-Z][.����)]", "@@@").split("@@@");
			String title = arr[0];
			// ��ʾͼƬ
			String zuheName = currentQuestion.getRuleId() + "-"
					+ currentQuestion.getQid();
			;
			showPics(title, imageSavePath, zuheName, examImages1, examContent1);
			// this.examOption1.clearCheck();
			if (this.examOption1.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < this.examOption1
						.getChildCount(); j++) {
					this.examOption1.removeViewAt(j);
				}
			}
			for (int i = 1; i < arr.length; i++) {
				int viewCount = this.examOption1.getChildCount();
				RadioButton rb;
				if (i > viewCount) {
					rb = new RadioButton(this);
					rb.setId(i);
					rb.setTextColor(getResources().getColor(R.color.black));
					rb.setButtonDrawable(R.drawable.radio_btn_img);
					this.examOption1.addView(rb, i - 1);
				}
				rb = (RadioButton) this.examOption1.getChildAt(i - 1);
				rb.setText((char) (64 + i) + "��" + arr[i]);
				if (answer != null
						&& answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
					rb.setChecked(true);
				}
			}
			this.examOption1
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// TODO Auto-generated method stub
							int id = examOption1.getCheckedRadioButtonId();
							if (id == -1)
								return;
							saveChoiceAnswer(((char) (64 + id)) + "");
						}
					});
		} else if ("��ѡ��".equals(type)) {
			this.modeLayout1.setVisibility(8);
			this.modeLayout2.setVisibility(0);
			this.modeLayout3.setVisibility(8);
			this.modeLayout4.setVisibility(8);
			String[] arr = currentQuestion.getContent().replaceAll("\n", "")
					.replaceAll("[A-Z][.����)]", "@@@").split("@@@");
			String title = arr[0];
			// ��ʾͼƬ
			String zuheName = currentQuestion.getRuleId() + "-"
					+ currentQuestion.getQid();
			;
			showPics(title, imageSavePath, zuheName, examImages2, examContent2);
			// ��ʾѡ��
			if (this.examOption2.getChildCount() > arr.length - 1) {
				for (int j = arr.length - 1; j < this.examOption2
						.getChildCount(); j++) {
					this.examOption2.removeViewAt(j);
				}
			}
			// this.examOption2.clearCheck();
			for (int i = 1; i < arr.length; i++) {
				int viewCount = this.examOption2.getChildCount();
				MyCheckBox cb;
				if (i > viewCount) {
					cb = new MyCheckBox(this);
					cb.setTextColor(getResources().getColor(R.color.black));
					cb.setButtonDrawable(R.drawable.checkbox_button_img);
					cb.setValue(String.valueOf((char) (64 + i)));
					this.examOption2.addView(cb, i - 1);
				}
				cb = (MyCheckBox) this.examOption2.getChildAt(i - 1);
				cb.setText((char) (64 + i) + "��" + arr[i]);
				if (answer != null
						&& answer.indexOf(String.valueOf((char) (64 + i))) != -1) {
					cb.setChecked(true);
				}
				cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						// TODO Auto-generated method stub
						MyCheckBox mcb = (MyCheckBox) buttonView;
						if (mcb.getFlag() == -1) {
							mcb.setFlag(0);
							return;
						}
						String s = examOption2.getValue();
						saveChoiceAnswer(s);
					}
				});
			}
		} else if ("�ж���".equals(type)) {
			this.modeLayout1.setVisibility(0);
			this.modeLayout2.setVisibility(8);
			this.modeLayout3.setVisibility(8);
			this.modeLayout4.setVisibility(8);
			// this.examContent1.setText(questionCursor + 1 + "��"
			// + currentQuestion.getContent());
			// ����ͼƬ
			String title = currentQuestion.getContent();
			// ��ʾͼƬ
			String zuheName = currentQuestion.getRuleId() + "-"
					+ currentQuestion.getQid();
			;
			showPics(title, imageSavePath, zuheName, examImages1, examContent1);
			//
			RadioButton rb_t, rb_f;
			if (examOption1.getChildCount() == 0) {
				rb_t = new RadioButton(this);
				rb_t.setId(1);
				rb_f = new RadioButton(this);
				rb_f.setId(2);
				rb_t.setText(" ��");
				rb_t.setTextColor(getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_btn_img);
				rb_f.setText(" ��");
				rb_f.setTextColor(getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_btn_img);
				this.examOption1.addView(rb_t, 0);
				this.examOption1.addView(rb_f, 1);
			}
			// this.examOption1.clearCheck();
			rb_t = (RadioButton) this.examOption1.getChildAt(0);
			rb_f = (RadioButton) this.examOption1.getChildAt(1);
			if (examOption1.getChildCount() > 2) {
				this.examOption1.removeAllViews();
				rb_t.setId(1);
				rb_f.setId(2);
				rb_t.setText(" ��");
				rb_t.setTextColor(getResources().getColor(R.color.black));
				rb_t.setButtonDrawable(R.drawable.radio_btn_img);
				rb_f.setText(" ��");
				rb_f.setTextColor(getResources().getColor(R.color.black));
				rb_f.setButtonDrawable(R.drawable.radio_btn_img);
				this.examOption1.addView(rb_t, 0);
				this.examOption1.addView(rb_f, 1);
			}
			if (answer != null) {
				if (answer.indexOf("F") != -1) {
					rb_f.setChecked(true);
					rb_t.setChecked(false);
				} else if (answer.indexOf("T") != -1) {
					rb_t.setChecked(true);
					rb_f.setChecked(false);
				} else {
					rb_t.setChecked(false);
					rb_f.setChecked(false);
				}
			}
			this.examOption1
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(RadioGroup group,
								int checkedId) {
							// TODO Auto-generated method stub
							if (checkedId == -1)
								return;
							if (checkedId == 1)
								saveChoiceAnswer("T");
							else
								saveChoiceAnswer("F");
						}
					});
		} else if ("�ʴ���".equals(type)) {
			this.modeLayout1.setVisibility(8);
			this.modeLayout2.setVisibility(8);
			this.modeLayout3.setVisibility(0);
			this.modeLayout4.setVisibility(8);
			String title = currentQuestion.getContent();
			// ��ʾͼƬ
			String zuheName = currentQuestion.getRuleId() + "-"
					+ currentQuestion.getQid();
			;
			showPics(title, imageSavePath, zuheName, examImages3, examContent3);
			if (answer != null) {
				this.answerEditText.setText(answer);
			}
			this.submitExamBtn.setVisibility(0);
			this.submitExamBtn.setOnClickListener(this);
		}
		if (!"DoExam".equals(action)) {
			String trueAnswer = currentQuestion.getAnswer();
			this.myAnswerTextView.setText(answer);
			this.sysAnswerTextView.setText(trueAnswer);
			this.analysisTextView.setText(currentQuestion.getAnalysis());
			if ("�ʴ���".equals(type)) {
				this.answerResultImg.setVisibility(View.GONE);
			} else {
				this.answerResultImg.setVisibility(View.VISIBLE);
				if (trueAnswer.equals(answer)) {
					this.answerResultImg
							.setImageResource(R.drawable.correct_answer_pto);
				} else if (answer != null && !"".equals(answer)
						&& isContain(trueAnswer, answer)) {
					this.answerResultImg
							.setImageResource(R.drawable.halfcorrect_pto);
				} else {
					this.answerResultImg
							.setImageResource(R.drawable.wrong_answer_pto);
				}
			}
		}
		if (favorQids != null
				&& favorQids.indexOf(currentQuestion.getQid()) != -1) {
			this.favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
		} else {
			this.favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
		}
	}

	private boolean isContain(String trueAnswer, String answer) {
		if (answer.length() == 1) {
			return trueAnswer.contains(answer);
		}
		String[] arr = answer.split(",");
		boolean flag = true;
		for (String s : arr) {
			flag = flag && trueAnswer.contains(s);
		}
		return flag;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.previousBtn:
			preQuestion();
			break;
		case R.id.nextBtn:
			nextQuestion();
			break;
		case R.id.favoriteBtn:
			favorQuestion();
			break;
		case R.id.exitExamImgBtn:
			if ("DoExam".equals(action)) {
				showDialog();
			} else {
				this.finish();
			}
			break;
		case R.id.exitExamBtn:
			exitExam();
			break;
		case R.id.exitCancelExamBtn:
			this.exitDialog.dismiss();
			break;
		case R.id.exitSubmitExamBtn:
			submitExam();
		case R.id.ruleTypeLayout:
			if (ruleList != null && ruleList.size() > 0) {
				showWindow(v);
			}
			break;
		case R.id.notebook_ImgBtn:
			showNoteBookActivity();
			break;
		case R.id.selectTopicId_ImgBtn:
			gotoChooseActivity();
			break;
		case R.id.submitExamBtn:
			saveTextAnswer();
			break;
		case R.id.answerBtn:
			submitOrSeeAnswer();
			break;
		case R.id.removeBtn:
			removeFromErrors();
			break;
		}
	}

	private void removeFromErrors() {
		currentQuestion = questionList.get(questionCursor);
		dao.deleteError(username, currentQuestion.getQid());
		Toast.makeText(this, "�Ƴ��ɹ�,�´β�����ʾ", Toast.LENGTH_SHORT).show();
	}

	private void favorQuestion() {
		currentQuestion = questionList.get(questionCursor);
		String qid = currentQuestion.getQid();
		favor.setQid(qid);
		if ("myFavors".equals(action)) {
			// ��ʾ�Ѿ��ղ���,����Ҫȡ���ղ�
			if (favorQids.indexOf(qid) == -1) {
				Toast.makeText(this, "�Ѿ�ȡ��", Toast.LENGTH_SHORT).show();
				return;
			}
			this.favoriteBtn.setImageResource(R.drawable.exam_favorite_img);
			dao.deleteFavor(favor);
			favorQids.replace(favorQids.indexOf(qid), favorQids.indexOf(qid)
					+ qid.length() + 1, "");
			Toast.makeText(this, "ȡ���ɹ�,�´β�����ʾ", Toast.LENGTH_SHORT).show();
			return;
		}
		if (favorQids.indexOf(qid) != -1) {
			Toast.makeText(this, "�Ѿ��ղ�", Toast.LENGTH_SHORT).show();
			return;
		} else {
			// û�ղ�,Ҫ�ղ�
			this.favoriteBtn.setImageResource(R.drawable.exam_favorited_img);
			dao.insertFavor(favor);
			favorQids.append(qid).append(",");
			Toast.makeText(this, "�ղسɹ�", Toast.LENGTH_SHORT).show();
		}
	}

	private void submitOrSeeAnswer() {
		if ("DoExam".equals(action)) {
			// showDialog();
			submitExam();
		} else {
			if (this.examAnswerLayout.getVisibility() == View.GONE) {
				this.examAnswerLayout.setVisibility(View.VISIBLE);
			} else if (this.examAnswerLayout.getVisibility() == View.VISIBLE) {
				this.examAnswerLayout.setVisibility(View.GONE);
			}
		}
	}

	private void gotoChooseActivity() {
		Intent mIntent = new Intent(this, QuestionChooseActivity.class);
		// ������
		if ("DoExam".equals(action)) {
			mIntent.putExtra("action", "chooseQuestion");
			mIntent.putExtra("ruleListJson", ruleListJson);
			mIntent.putExtra("isDone", gson.toJson(isDone));
		} else {
			mIntent.putExtra("action", "otherChooseQuestion");
			mIntent.putExtra("questionList", gson.toJson(questionList));
		}
		this.startActivityForResult(mIntent, 1);
	}

	private void gotoChooseActivity2() {
		Intent mIntent = new Intent(this, QuestionChooseActivity.class);
		// ������
		mIntent.putExtra("action", "submitPaper");
		mIntent.putExtra("questionList", gson.toJson(questionList));
		mIntent.putExtra("paperScore", paperScore);
		mIntent.putExtra("paperTime", time / 60);
		mIntent.putExtra("username", username);
		mIntent.putExtra("paperid", paperid);
		mIntent.putExtra("useTime", record.getUseTime());
		mIntent.putExtra("isDone", gson.toJson(isDone));
		mIntent.putExtra("userScore", record.getScore()); // ���ε÷�
		mIntent.putExtra("hasDoneNum", isDone.size()); // ���˶�����
		this.startActivityForResult(mIntent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (20 == resultCode) {
			// ��������,��ǰ����
			String ruleTitle = data.getStringExtra("ruleTitle");
			this.examTypeTextView.setText(ruleTitle);
			questionCursor = data.getIntExtra("cursor", 0);
			action = data.getStringExtra("action");
			this.scrollView.fullScroll(33);
			showContent();
		} else if (30 == resultCode) {
			action = "DoExam";
			questionCursor = 0;
			record.setTempAnswer("");
			record.setIsDone("");
			answerBuf.delete(0, answerBuf.length());
			txtAnswerBuf.delete(0, txtAnswerBuf.length());
			isDone.clear();
			setNull4UserAnswer();
		} else if (0 == resultCode) {
			this.finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void showNoteBookActivity() {
		Intent mIntent = new Intent(this, QuestionWriteNoteActivity.class);
		// ������,��ǰ�������id,username
		mIntent.putExtra("paperid", paperid);
		mIntent.putExtra("qid", questionList.get(questionCursor).getQid());
		mIntent.putExtra("username", username);
		this.startActivity(mIntent);

	}

	private void preQuestion() {
		this.scrollView.fullScroll(33);
		if (questionCursor == 0) {
			Toast.makeText(this, "�Ѿ��ǵ�һ����", Toast.LENGTH_SHORT).show();
			return;
		}
		this.examOption1.clearCheck();
		this.examOption2.clearCheck();
		questionCursor--;
		showContent();
	}

	private void nextQuestion() {
		this.scrollView.fullScroll(33);
		if (questionCursor == questionList.size() - 1) {
			Toast.makeText(this, "�Ѿ������һ����", Toast.LENGTH_SHORT).show();
			return;
		}
		this.examOption1.clearCheck();
		this.examOption2.clearCheck();
		questionCursor++;
		showContent();
	}

	// ����ѡ����(��ѡ�Ͷ�ѡ)��
	private void saveChoiceAnswer(String abcd) // 1001-A&1002-B&
	{
		if (!"DoExam".equals(action)) {
			return;
		}
		String str = currentQuestion.getQid() + "-";
		if (answerBuf.indexOf(str) == -1) {
			answerBuf.append(str + abcd).append("&");
			isDone.append(questionCursor, true);
		} else {
			String left = answerBuf.substring(0, answerBuf.indexOf(str));
			String temp = answerBuf.substring(answerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("&") + 1);
			if ("".equals(abcd)) // ��ѡ��,û��ѡ��
			{
				// �Ӵ���ȥ��
				answerBuf.delete(0, answerBuf.length()).append(left)
						.append(right);
				isDone.delete(questionCursor);
			} else {
				answerBuf.delete(0, answerBuf.length()).append(left)
						.append(str).append(abcd).append("&").append(right);
				isDone.append(questionCursor, true);
			}
		}
		record.setTempAnswer(answerBuf.toString()
				+ (txtAnswerBuf.length() == 0 ? "" : "   "
						+ txtAnswerBuf.toString()));
		// ÿ����5�����Զ������
		if (answerBuf.toString().split("&").length % 5 == 0) {
			record.setIsDone(gson.toJson(isDone));
			dao.updateTempAnswerForRecord(record);
		}
		currentQuestion.setUserAnswer("".equals(abcd) ? null : abcd); // ����ѧԱ��
	}

	// �����ʴ����
	private void saveTextAnswer() {
		if (!"DoExam".equals(action)) {
			return; // �ǿ��Բ��ر����
		}
		String str = currentQuestion.getQid() + "-";
		String txtAnswer = this.answerEditText.getText().toString();
		if ("".equals(txtAnswer.trim())) {
			Toast.makeText(this, "����д��", Toast.LENGTH_LONG).show();
			return;
		}
		if (txtAnswerBuf.indexOf(str) == -1) {
			txtAnswerBuf.append(str + txtAnswer.replace("\\s", "")).append(
					"   ");
		} else {
			String left = txtAnswerBuf.substring(0, txtAnswerBuf.indexOf(str));
			String temp = txtAnswerBuf.substring(txtAnswerBuf.indexOf(str));
			String right = temp.substring(temp.indexOf("   ") + 3);
			txtAnswerBuf.delete(0, txtAnswerBuf.length()).append(left)
					.append(str).append(txtAnswer).append("   ").append(right);
		}
		isDone.append(questionCursor, true);
		currentQuestion.setUserAnswer(txtAnswer);
		record.setTempAnswer(answerBuf.toString() + "   "
				+ txtAnswerBuf.toString());
		record.setIsDone(gson.toJson(isDone));
		dao.updateTempAnswerForRecord(record);
		Toast.makeText(this, "����ɹ�", Toast.LENGTH_SHORT).show();
	}

	// ����,���з�
	private void submitPaper() {
		/**
		 * 
		 */
		if (record.getTempAnswer() == null
				|| "".equals(record.getTempAnswer().trim())) {
			Toast.makeText(this, "��û����ë��", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			double score = 0; // �ܷ�
			double score1 = 0; // ���۷ֵ����
			double score2 = 0; // ����������ʱ����
			StringBuffer buf = new StringBuffer();
			StringBuffer scoreBuf = new StringBuffer("eachScore&");
			for (int k = 0; k < ruleList.size(); k++) // ѭ������
			{
				ExamRule r = ruleList.get(k);
				double fen = r.getScoreForEach();// ÿ��ķ���
				String fenRule = r.getScoreSet();// �зֹ��� 0|N��ʾÿ����ٷ־��Ƕ��ٷ֣�
													// 1|N,��ʾ���һ��ѡ���N�֣�ȫ����Եø��������
													// 2|N,��ʾ����N��,���ٵ�0��
				for (int j = 0; j < questionList.size(); j++) // ѭ����Ŀ
				{
					ExamQuestion q = questionList.get(j);
					double tempScore = 0;
					if (q.getRuleId().equals(r.getRuleId())) // ���ڸô������Ŀ�����ù�������з�
					{
						System.out.println(q.getAnswer() + ", userAnswer:"
								+ q.getUserAnswer());
						if (fenRule.startsWith("0|")) // ����۷֣�ȫ�Բŵ�����
						{
							if (q.getAnswer().equals(q.getUserAnswer())) {
								score = score + fen; // �÷�
								tempScore = fen;
							}
						} else if (fenRule.startsWith("1|"))// ���һ��ѡ��ö��ٷ�
						{
							String answer = q.getAnswer();
							String userAnswer = q.getUserAnswer() == null ? "@"
									: q.getUserAnswer();
							if (answer.contains(userAnswer)) { // ���������
								if (answer.equals(userAnswer)) {
									score = score + fen;
									tempScore = fen;
								} else {
									String[] ua = userAnswer.split("[,]"); // ��ѡ�÷֣���ÿ��ѡ��ĵ÷ֻ���ֻҪ����ѡ�͵ö��ٷ�
									double fen1 = Double.parseDouble(fenRule
											.split("[|]")[1]) * ua.length;
									score = score + fen1;
									tempScore = fen1;
								}
							}
						} else if (fenRule.startsWith("2|"))// ���۷�
						{
							if (q.getAnswer().equals(q.getUserAnswer())) // ���
							{
								score1 = score1
										+ Double.parseDouble(fenRule
												.split("[|]")[1]);
								tempScore = Double.parseDouble(fenRule
										.split("[|]")[1]);
							} else // ���
							{
								score1 = score1
										- Double.parseDouble(fenRule
												.split("[|]")[1]);
								tempScore = 0 - Double.parseDouble(fenRule
										.split("[|]")[1]);
							}
						}
						scoreBuf.append(r.getRuleId()).append("-")
								.append(q.getQid()).append("-")
								.append(tempScore).append("&"); // ÿ����ĵ÷�
					}
				}
				// ÿ����÷�
				if (fenRule.startsWith("2|")) {
					buf.append(r.getRuleId());
					buf.append("=");
					buf.append(score1 > 0 ? score1 : 0);
					buf.append(";");
				} else {
					buf.append(r.getRuleId());
					buf.append("=");
					score2 = score - score2;
					buf.append(score2);
					buf.append(";");
					score2 = score;
				}
			}
			score = score1 > 0 ? (score + score1) : score;
			// ѧԱ�𰸴��ȥ
			record.setScore(score);
			System.out.println(scoreBuf.toString());
			// ����record��¼
			// record.setRcdScoreForEachQuestion(scoreBuf.toString());//ÿ��ĵ÷����
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		// Log.i("MyGesture", "onDown");
		// Toast.makeText(this, "onDown", Toast.LENGTH_SHORT).show();
		return true; // �¼��Ѵ�����true
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		final int FLING_MIN_DISTANCE = 100, FLING_MIN_VELOCITY = 200;
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling left
			Log.i("MyGesture", "Fling left");
			nextQuestion();
			// Toast.makeText(this, "Fling Left", Toast.LENGTH_SHORT).show();
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			// Fling right
			Log.i("MyGesture", "Fling right");
			// Toast.makeText(this, "Fling Right", Toast.LENGTH_SHORT).show();
			preQuestion();
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		mGestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		this.scrollView.onTouchEvent(event);
		return false;
	}

	private void showWindow(View parent) {
		if (popupWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = layoutInflater.inflate(
					R.layout.popupwindow_rule_layout, null);

			lv_group = (ListView) view.findViewById(R.id.lvGroup);
			// ��������

			PopRuleListAdapter groupAdapter = new PopRuleListAdapter(this,
					ruleList);
			lv_group.setAdapter(groupAdapter);
			// ����һ��PopuWidow����
			popupWindow = new PopupWindow(view, 200, 250);
		}

		// ʹ��ۼ�
		popupWindow.setFocusable(true);
		// ����������������ʧ
		popupWindow.setOutsideTouchable(true);

		// �����Ϊ�˵��������Back��Ҳ��ʹ����ʧ�����Ҳ�����Ӱ����ı���
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
		WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		// ��ʾ��λ��Ϊ:��Ļ�Ŀ�ȵ�һ��-PopupWindow�ĸ߶ȵ�һ��
		int xPos = windowManager.getDefaultDisplay().getWidth() / 2
				- popupWindow.getWidth() / 2;

		Log.i("coder", "windowManager.getDefaultDisplay().getWidth()/2:"
				+ windowManager.getDefaultDisplay().getWidth() / 2);
		//
		Log.i("coder", "popupWindow.getWidth()/2:" + popupWindow.getWidth() / 2);

		Log.i("coder", "xPos:" + xPos);

		popupWindow.showAsDropDown(parent, xPos, -5);

		lv_group.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// ����,�ı��������,�е��ô����һ��
				// ��ǰ����
				ExamRule rule = QuestionDoExamActivity.this.ruleList
						.get(position);
				int questionPosition = 0;
				for (int i = position - 1; i >= 0; i--) {
					questionPosition += QuestionDoExamActivity.this.ruleList
							.get(i).getQuestionNum();
				}
				QuestionDoExamActivity.this.examTypeTextView.setText(rule
						.getRuleTitle());
				QuestionDoExamActivity.this.questionCursor = questionPosition; // cursor��0��ʼ
				QuestionDoExamActivity.this.showContent();
				if (popupWindow != null) {
					popupWindow.dismiss();
				}
			}
		});
	}

	// �˳�����(������ֱ���˳�)
	private void exitExam() {
		// ����һ��record
		timerFlag = false;
		record.setLastTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		record.setTempTime(this.paperTime);
		record.setIsDone(gson.toJson(isDone));
		dao.saveOrUpdateRecord(record);
		this.exitDialog.dismiss();
		this.finish();
	}

	// ����
	private void submitExam() {
		if (this.exitDialog != null && this.exitDialog.isShowing()) {
			this.exitDialog.dismiss();
		}
		if (record.getTempAnswer() == null
				|| "".equals(record.getTempAnswer().trim())) {
			Toast.makeText(this, "��û����ë��", Toast.LENGTH_SHORT).show();
			return;
		}
		//��һ���߳̽��н���  2013-09-25�޸�
		if(proDialog == null)
		{
			proDialog = ProgressDialog.show(this, null, "���ڽ���..", true, false);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}else
		{
			proDialog.show();
		}
		new Thread(){
			public void run() {
				submitPaper();// ����
				timerFlag = false;
				// ���¼�¼,ת�� ѡ�����
				record.setLastTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date()));
				record.setAnswers(record.getTempAnswer());
				record.setTempAnswer(null);
				record.setIsDone(null);
				record.setTempTime(0);
				record.setUseTime((time - paperTime) < 60 ? 1 : (time - paperTime) / 60);
				dao.saveOrUpdateRecord(record);
				timeHandler.sendEmptyMessage(10);
			};
		}.start();
	}

	// �����ؼ�,��ʾ
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
		if ((paramKeyEvent.getKeyCode() == 4)
				&& (paramKeyEvent.getRepeatCount() == 0)) {
			if ("DoExam".equals(action)) {
				showDialog();
				return true;
			}
		}
		return super.onKeyDown(paramInt, paramKeyEvent);
	}

	private void showDialog() {
		if (exitDialog == null) {
			View v = LayoutInflater.from(this).inflate(R.layout.exit_layout,
					null);
			Button exitBtn = (Button) v.findViewById(R.id.exitExamBtn);
			Button submitBtn = (Button) v.findViewById(R.id.exitSubmitExamBtn);
			Button cancelBtn = (Button) v.findViewById(R.id.exitCancelExamBtn);
			AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			localBuilder.setTitle("ע��").setMessage("�Ƿ�ע���û�").setView(v);
			exitDialog = localBuilder.create();
			exitBtn.setOnClickListener(this);
			submitBtn.setOnClickListener(this);
			cancelBtn.setOnClickListener(this);
		}
		exitDialog.show();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		if ("DoExam".equals(action)) {
			// timerFlag = true;
			// new TimerThread().start();
			this.answerBtn.setImageResource(R.drawable.exam_submit_img);
			this.examAnswerLayout.setVisibility(View.GONE);
			if (ruleList != null && ruleList.size() > 0) {
				currentRule = ruleList.get(0);
				this.examTypeTextView.setText(currentRule.getRuleTitle()); // ��������
				this.ruleTypeLayout.setOnClickListener(this);
				showContent();
			} else {
				this.nodataLayout.setVisibility(0);
			}
		} else if ("showQuestionWithAnswer".equals(action)) {
			this.examTitle.setText(this.papername);
			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
			this.examAnswerLayout.setVisibility(View.VISIBLE);
			this.ruleTypeLayout.setOnClickListener(this);
			showContent();
		} else {
			this.answerBtn.setImageResource(R.drawable.exam_answer_img);
			this.examAnswerLayout.setVisibility(View.VISIBLE);
			showContent();
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if ("DoExam".equals(action)) {
			timerFlag = true;
			new TimerThread().start();
		}
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		timerFlag = false;
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		timerFlag = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (exitDialog != null) {
			exitDialog.dismiss();
		}
		this.examOption1.removeAllViews();
		this.examOption2.removeAllViews();
		super.onDestroy();
	}

	private static class TimerHandler extends Handler {
		private WeakReference<QuestionDoExamActivity> weak;

		public TimerHandler(QuestionDoExamActivity a) {
			// TODO Auto-generated constructor stub
			this.weak = new WeakReference<QuestionDoExamActivity>(a);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			QuestionDoExamActivity theActivity = weak.get();
			switch (msg.what) {
			case 1:
				theActivity.paperTime--;
				theActivity.timeCountDown
						.setText(getTimeText(theActivity.paperTime));
				if (theActivity.paperTime == 0) {
					// ����
					timerFlag = false;
					Toast.makeText(theActivity, "Time Over", Toast.LENGTH_LONG)
							.show();
					theActivity.submitExam();
				}
				break;
			case 10:
				if(theActivity.proDialog!=null)
				{
					theActivity.proDialog.dismiss();
				}
				theActivity.gotoChooseActivity2(); //����
				break;
			}
		}

		private String getTimeText(int count) {
			int h = count / 60 / 60;
			int m = count / 60 % 60;
			int s = count % 60;
			return (h > 0 ? h : 0) + ":" + (m > 9 ? m : "0" + m) + ":"
					+ (s > 9 ? s : "0" + s);
		}
	}

	private class TimerThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (timerFlag) {
				timeHandler.sendEmptyMessage(1);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void initQuestionAnswer(String tempAnswer) {
		if (tempAnswer == null || "".equals(tempAnswer.trim())) {
			return;
		}
		int listSize = questionList.size();
		String choiceAnswer = null, textAnswer = null;
		if (tempAnswer.indexOf("   ") != -1) {
			choiceAnswer = tempAnswer.substring(0, tempAnswer.indexOf("   "));
			textAnswer = tempAnswer.substring(tempAnswer.indexOf("   ") + 3);
		} else {
			choiceAnswer = tempAnswer;
		}
		for (int i = 0; i < listSize; i++) {
			ExamQuestion q = questionList.get(i);
			String str = q.getQid() + "-";
			if ((!"�ʴ���".equals(q.getQType()))
					&& choiceAnswer.indexOf(str) != -1) {
				String temp = choiceAnswer.substring(choiceAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(), temp.indexOf("&")));
			} else if (textAnswer != null && "�ʴ���".equals(q.getQType())
					&& textAnswer.indexOf(str) != -1) {
				String temp = textAnswer.substring(textAnswer.indexOf(str));
				q.setUserAnswer(temp.substring(str.length(),
						temp.indexOf("   ")));
			}
		}
	}

	private void setNull4UserAnswer() {
		for (ExamQuestion q : questionList) {
			q.setUserAnswer(null);
		}
	}

	// ����ͼƬ���ص�ַ
	private String[] parseAddress(String address) {
		String[] addr = null;
		// if(address.contains("<IMG "))
		// {
		String[] arr = address.split("<IMG ");
		addr = new String[arr.length];
		for (int i = 1; i < arr.length; i++) {
			String s = arr[i];
			if (!"".equals(s)) {
				String right = s.substring(s.indexOf("src=\"") + 5);
				addr[i] = right.substring(0, right.indexOf("\""));
			} else {
				addr[i] = null;
			}
		}
		// }
		return addr;
	}

	// �첽����ͼƬ
	private class GetImageTask extends AsyncTask<String, Void, Bitmap> {
		private String fileName;

		public GetImageTask(String fileName) {
			// TODO Auto-generated constructor stub
			this.fileName = fileName;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			URL url;
			byte[] b = null;
			try {
				fileName = fileName
						+ params[0].substring(params[0].lastIndexOf("."));
				url = new URL(params[0]); // ����URL
				HttpURLConnection con;
				con = (HttpURLConnection) url.openConnection(); // ������
				con.setRequestMethod("GET"); // �������󷽷�
				// �������ӳ�ʱʱ��Ϊ5s
				con.setConnectTimeout(5000);
				InputStream in = con.getInputStream(); // ȡ���ֽ�������
				int len = 0;
				byte buf[] = new byte[1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((len = in.read(buf)) != -1) {
					out.write(buf, 0, len); // ������д���ڴ�
				}
				byte[] data = out.toByteArray();
				out.close(); // �ر��ڴ������
				// ��������������λͼ
				Bitmap bit = BitmapFactory
						.decodeByteArray(data, 0, data.length);
				return bit;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			if (result == null) {
				return;
			}
			ImageView img = new ImageView(QuestionDoExamActivity.this);
			img.setScaleType(ImageView.ScaleType.FIT_START);
			examImages1.addView(img);
			try {
				img.setImageURI(Uri.parse(saveFile(result, fileName)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			super.onPostExecute(result);
		}

		public String saveFile(Bitmap bm, String fileName) throws IOException {
			String filePath = imageSavePath + File.separator + fileName;
			File myCaptureFile = new File(filePath);
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(myCaptureFile));
			bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bm.recycle();
			bos.flush();
			bos.close();
			return filePath;
		}
	}

	private void openPopupwin() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
				R.layout.pop_doexam_tips, null, true);
		tipWindow = new PopupWindow(menuView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, true);
		tipWindow.setFocusable(true);
		tipWindow.setBackgroundDrawable(new BitmapDrawable());
		tipWindow.setAnimationStyle(R.style.AnimationFade);
		/***************** ���´�������ѭ�����activity�Ƿ��ʼ����� ***************/
		Runnable showPopWindowRunnable = new Runnable() {
			@Override
			public void run() {
				// �õ�activity�еĸ�Ԫ��
				View view = findViewById(R.id.parent);
				// ��θ�Ԫ�ص�width��height����0˵��activity�Ѿ���ʼ�����
				if (view != null && view.getWidth() > 0 && view.getHeight() > 0) {
					// ��ʾpopwindow
					tipWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
					// ֹͣ���
					mHandler.removeCallbacks(this);
				} else {
					// ���activityû�г�ʼ�������ȴ�5�����ٴμ��
					mHandler.postDelayed(this, 5);
				}
			}
		};
		// ��ʼ���
		mHandler.post(showPopWindowRunnable);
		/****************** ���ϴ�������ѭ�����activity�Ƿ��ʼ����� *************/
	}

	public void disPopupWin(View v) {
		if (tipWindow != null && tipWindow.isShowing())
			tipWindow.dismiss();
		SharedPreferences.Editor editor = guidefile.edit();
		if (guidefile.contains("isFirstExam")) {
			editor.remove("isFirstExam");
		}
		editor.putInt("isFirstExam", 1);
		editor.commit();
	}

	// ��ʾ��Ŀ�е�ͼƬ
	private void showPics(String title, String imageSavePath, String zuheName,
			LinearLayout examImages, TextView examContent) {
		if (title.contains("<IMG ")) // ����ͼƬ
		{
			// String s = currentRule.getRuleId()+"-"+currentQuestion.getQid();
			// ��ȥsd����,�ҵ�����ʾ,�Ҳ�������������ʾ
			File dir = new File(imageSavePath);
			if (dir.exists()) {
				// dir.mkdirs();
				File[] files = dir.listFiles();
				int count = files.length;
				for (File f : files) {
					if (f.getName().contains(zuheName)) {
						ImageView img = new ImageView(this);
						img.setImageURI(Uri.parse(f.getPath()));
						examImages.addView(img);
						count--;
					}
				}
				if (count == files.length) // û��ͼƬ,û���Ƕ�ͼƬû�м�����ȫ�����
				{
					String[] imageUrls = parseAddress(title);
					for (int i = 0; i < imageUrls.length; i++) {
						String url = imageUrls[i];
						if ("".equals(url) || url == null) {
							continue;
						}
						new GetImageTask(zuheName + "-" + i).execute(url);
					}
				}
			} else {
				dir.mkdirs();
				String[] imageUrls = parseAddress(title);
				for (int i = 0; i < imageUrls.length; i++) {
					String url = imageUrls[i];
					if ("".equals(url)) {
						continue;
					}
					new GetImageTask(zuheName + "-" + i).execute(url);
				}
			}
			examContent.setText(questionCursor + 1 + "��"
					+ title.replaceAll("<IMG[\\S\\s]+>", ""));
		} else
			examContent.setText(questionCursor + 1 + "��" + title);
	}
}
