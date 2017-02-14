# QuickSearchView
***仿微信通讯录右侧快速定位字母表控件***

![image](http://ohazfcl3s.bkt.clouddn.com/quicksearch.gif)

##### 这种控件在很多应用的通讯录的界面，MIUI里面的通讯录都有这个功能，其实这是一个自定义View，相对来说，这个并不是一个多么复杂的自定义View。

下面介绍一下这种控件的简单实现方法：
##### 首先，自定义View，一般是对View的增强，因为系统提供的控件不能满足需求，一般情况下，都是继承View，然后重写里面的onDraw等方法。

来看一下这个控件，A~#这27个符号需要绘制到view上去，这个当然需要在onDraw方法中实现。
另外，这个控件能够响应用户的Touch事件，在用户触碰到相应的字母的时候，左边需要展示相应字母的对话框，
其实这里可以用一个TextView来实现，在Touch事件里面设置TextView的状态是否可见就可以了，
处理需要展示对话框，还需要控制左边ListView的位置，当手指碰到相应的字母时，ListView需要跳到相应的字母位置处。

***自定义View的代码***
```
package com.hgao.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.hgao.main.R;
import com.hgao.utils.PixelUtil;

public class MyLetterView extends View {

	public static String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H",
			"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
			"V", "W", "X", "Y", "Z", "#" };

	private Paint paint = new Paint();
	/**
	 * 用于标记哪个位置被选中
	 */
	private int choose = -1;
       //该TextView是左边显示的对话框
	private TextView mTextDialog;

	public void setTextDialog(TextView mTextDialog) {
		this.mTextDialog = mTextDialog;
	}

	private OnTouchingLetterChangedListener listener;

	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener listener) {
		this.listener = listener;
	}

	public MyLetterView(Context context) {
		super(context);
	}

	public MyLetterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyLetterView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 获取该自定义View的宽度和高度
		int width = getWidth();
		int height = getHeight();

		// 单个字母的高度
		int singleHeight = height / letters.length;

		for (int i = 0; i < letters.length; i++) {
			paint.setColor(getResources().getColor(
					R.color.color_bottom_text_normal));
			paint.setTypeface(Typeface.DEFAULT_BOLD);
			paint.setAntiAlias(true);
			paint.setTextSize(PixelUtil.sp2px(12, getContext()));

			// 如果选中的话,改变样式和颜色
			if (i == choose) {
				paint.setColor(Color.parseColor("#3399ff"));
				paint.setFakeBoldText(true);
			}

			// 首先确定每个字母的横坐标的位置，横坐标：该自定义View的一半 -（减去） 单个字母宽度的一半
			float xPos = width / 2 - paint.measureText(letters[i]) / 2;
			float yPos = singleHeight * (i + 1);

			canvas.drawText(letters[i], xPos, yPos, paint);

			// 重置画笔
			paint.reset();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		int action = event.getAction();
		float y = event.getY();

		int oldChoose = choose;
		// 根据y坐标确定当前哪个字母被选中
		int pos = (int) (y / getHeight() * letters.length);

		switch (action) {
		case MotionEvent.ACTION_UP:
			// 当手指抬起时,设置View的背景为白色
			setBackgroundDrawable(new ColorDrawable(0x00000000));
			// 重置为初始状态
			choose = -1;
			// 让View重绘
			invalidate();

			// 将对话框设置为不可见
			if (mTextDialog != null) {
				mTextDialog.setVisibility(View.INVISIBLE);
			}

			break;
		default:
			// 设置右边字母View的背景色
			setBackgroundResource(R.drawable.v2_sortlistview_sidebar_background);
			if (pos != oldChoose) {
				// 如果之前选中的和当前的不一样，需要重绘
				if (pos >= 0 && pos < letters.length) {
					if(listener != null) {
						//当前字母被选中，需要让ListView去更新显示的位置
						listener.onTouchingLetterChanged(letters[pos]);
					}
					//在左边显示选中的字母，该字母放在TextView上，相当于一个dialog
					if (mTextDialog != null) {
						mTextDialog.setText(letters[pos]); //让对话框显示响应的字母
						mTextDialog.setVisibility(View.VISIBLE);
					}
					choose = pos;  //当前位置为选中位置
				}
			}
			break;
		}

		return true;
	}

	/**
	 * 该回调接口用于通知ListView更新状态
	 */
	public interface OnTouchingLetterChangedListener {
		public void onTouchingLetterChanged(String s);
	}

}
```
在onDraw方法中，将A~#这些字母绘制成功，这里面的逻辑相对简单，计算下高度初始化画笔，设置画笔相关的属性和style。
该控件对Touch事件进行响应，显然需要重写Touch事件相关的方法，其实这里重写onTouchEvent事件和dispatchTouchEvent都是可以的。

我们这里重写的是dispatchTouchEvent这个方法，这个涉及到事件的分发机制，有兴趣的同学可以去研究下。
其实View的事件遵循这样的流程：dispatchTouchEvent------>onTouch------>onTouchEvent------->onClick。
在dispatchTouchEvent中重写，返回true，这样不会往下分发，其实重写onTouchEvent是一样的，都是响应用户的Touch事件，
然后让View作出相应的重绘。

```
/**
* 该回调接口用于通知ListView更新状态
 */
public interface OnTouchingLetterChangedListener {
	public void onTouchingLetterChanged(String s);
}
```
注意到定义了这个接口，这个接口是暴露给ListView，用于ListView能够更新自己显示的位置。


***主界面代码***
```
package com.hgao.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.hgao.utils.CharacterParser;
import com.hgao.utils.PinyinComparator;
import com.hgao.view.MyLetterView;
import com.hgao.view.MyLetterView.OnTouchingLetterChangedListener;

public class MainActivity extends Activity {

	private ListView list_friends;
	private TextView dialog;
	private MyLetterView right_letter;

	private List<Friend> friends;
	
	/**
	 * 根据拼音来排列ListView中的数据
	 */
	private PinyinComparator pinyinComparator;

	private CharacterParser characterParser;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initData();
		initView();
	}

	private void initData() {
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		String[] names = getResources().getStringArray(R.array.names);  //这里ListView展示的数据不是从网络加载的
		friends = new ArrayList<Friend>();				//数据写在string.xml文件中
		for (int i = 0; i < names.length; i++) {
			Friend f = new Friend();
			f.setName(names[i]);
			String pinyin = characterParser.getSelling(names[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				f.setSortLetters(sortString.toUpperCase());
			} else {
				f.setSortLetters("#");
			}
			friends.add(f);
		}
		Collections.sort(friends, pinyinComparator);  //将数据进行,pinyinComparator是一个比较器
	}

	private void initView() {
		list_friends = (ListView) findViewById(R.id.list_friends);
		dialog = (TextView) findViewById(R.id.dialog);
		right_letter = (MyLetterView) findViewById(R.id.right_letter);
		right_letter.setTextDialog(dialog);
		final FriendsAdapter adapter = new FriendsAdapter(this,friends);
		right_letter
				.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
					@Override
					public void onTouchingLetterChanged(String s) {
						// 该字母首次出现的位置
						int position = adapter.getPositionForSection(s.charAt(0));
						if (position != -1) {
							list_friends.setSelection(position);
						}
					}
				});
	
		list_friends.setAdapter(adapter);
	}
}

```
主界面的逻辑其实很简单，初始化数据，并初始化相关控件，并为控件设置响应的事件监听，这样，这个控件的实现简单吧。
这里面用到了一些工具类，用于汉字和拼音的转换，后面有源码，还是看源码吧。

```

package com.hgao.main;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FriendsAdapter extends BaseAdapter {

	List<Friend> friends;
	Context context;
	public FriendsAdapter(Context context, List<Friend> friends) {
		this.friends = friends;
		this.context = context;
	}

	@Override
	public int getCount() {
		return friends.size();
	}

	@Override
	public Object getItem(int position) {
		return friends.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_user_friend, null);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.tv_friend_name);
			viewHolder.alpha = (TextView) convertView.findViewById(R.id.alpha);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		Friend friend = friends.get(position);
		viewHolder.name.setText(friend.getName());
		
		// 根据position获取分类的首字母的Char ascii值
		int section = getSectionForPosition(position);
		// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
		if (position == getPositionForSection(section)) {
			viewHolder.alpha.setVisibility(View.VISIBLE);
			viewHolder.alpha.setText(friend.getSortLetters());
		} else {
			viewHolder.alpha.setVisibility(View.GONE);
		}
		return convertView;
	}
	
	static class ViewHolder {
		TextView name;
		TextView alpha;;
	}
//判断当前位置是否是该位置对应的字母第一次出现
	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = friends.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (section == firstChar) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 根据ListView的当前位置获取分类的首字母的Char ascii值
	 */
	private int getSectionForPosition(int position) {
		return friends.get(position).getSortLetters().charAt(0);
	}
}


```


博客地址 [链接](http://blog.csdn.net/h_gao/article/details/52280521)