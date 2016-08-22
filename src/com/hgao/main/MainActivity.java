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
	 * ����ƴ��������ListView�е�����
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
		String[] names = getResources().getStringArray(R.array.names);
		friends = new ArrayList<Friend>();
		for (int i = 0; i < names.length; i++) {
			Friend f = new Friend();
			f.setName(names[i]);
			String pinyin = characterParser.getSelling(names[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// ������ʽ���ж�����ĸ�Ƿ���Ӣ����ĸ
			if (sortString.matches("[A-Z]")) {
				f.setSortLetters(sortString.toUpperCase());
			} else {
				f.setSortLetters("#");
			}
			friends.add(f);
		}
		Collections.sort(friends, pinyinComparator);
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
						// ����ĸ�״γ��ֵ�λ��
						int position = adapter.getPositionForSection(s.charAt(0));
						if (position != -1) {
							list_friends.setSelection(position);
						}
					}
				});
	
		list_friends.setAdapter(adapter);
	}
}
