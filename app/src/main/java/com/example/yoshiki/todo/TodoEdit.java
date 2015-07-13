package com.example.yoshiki.todo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.MultiTapKeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * TodoItem編集用のクラス.
 * @author 清兼
 */
public class TodoEdit extends Activity {

    /**
     * メンバ定義
     */
    private EditText mTitleText;        // Todoアイテムのタイトル
    private EditText mBodyText;         // Todoアイテムの内容
    private Button mDate;               // Todo実施期限
    private Long mPrimaryKey;           // PrimaryKey
    private TodoDbAdapter mDbHelper;    // DBアクセスクラスのインスタンス
    private String mState = TodoDbAdapter.STR_STATE_OPEN;    // ToDoアイテムの状態

    /**
     * TodoItem編集画面を表示.
     *
     * @param savedInstanceState 保存されていたアプリケーション情報
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // DBアクセスクラスのインスタンスの生成
        mDbHelper = new TodoDbAdapter(this);

        // DBを開く
        mDbHelper.open();

        // Viewの表示
        setContentView(R.layout.todo_edit);

        // ViewのIDを取得
        mTitleText           = (EditText)findViewById(R.id.title);
        mBodyText            = (EditText)findViewById(R.id.body);
        mDate                = (Button)findViewById(R.id.data);
        final Button dateButton     = (Button)findViewById(R.id.data);
        final Button confirmButton  = (Button)findViewById(R.id.confirm);
        final Button closeButton    = (Button)findViewById(R.id.close);

        //******************************************
        //
        // Bundle又はIntentからPrimaryKey取得
        //
        //******************************************
        // Bundleから取得
        mPrimaryKey = null;
        if (savedInstanceState != null) {
            mPrimaryKey = savedInstanceState.getLong(TodoDbAdapter.STR_KEY_PRIMARY);
        }

        // Intentから取得
        if (mPrimaryKey == null) {
            // インテントを取得
            Bundle extras = getIntent().getExtras();

            // インテントからPrimaryKeyを取得（=TodoItemの更新）
            if(extras != null) {
                mPrimaryKey = extras.getLong(TodoDbAdapter.STR_KEY_PRIMARY);
            }
        }

        //******************************************
        //
        // OnClickListener定義
        //
        //******************************************
        // dateButton
        dateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // TodoDateへインテント
                Intent i = new Intent(TodoEdit.this, TodoDate.class);

                // ボタンに表示している日付を設定
                i.putExtra(TodoDate.STR_KEY_DATE, dateButton.getText());

                // Activity起動
                startActivityForResult(
                        i,                                          // インテント
                        CState.N_ACTIVITY_DATE);  // Todo実施期限設定
            }
        });

        // confirmButton
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Activityの戻りを設定
                setResult(RESULT_OK);

                // Activityの終了
                finish();
            }
        });

        // closeButton
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                // Todoアイテム状態の更新
                mState = TodoDbAdapter.STR_STATE_CLOSE;

                // Activityの終了
                finish();
            }
        });
    }

    /**
     * DBから取得した情報をViewに表示.
     *
     */
    private void populateFields() {
        // PrimaryKeyがある場合
        if (mPrimaryKey != null) {
            // DBからTodoアイテムを取得
            Cursor TodoItem = mDbHelper.fetchToDoItemByPrimaryKey(mPrimaryKey);

            // カーソル制御をシステムへ移譲
            startManagingCursor(TodoItem);

            // Todoアイテムのインデックスを取得
            int Title = TodoItem.getColumnIndexOrThrow(TodoDbAdapter.STR_KEY_TITLE);
            int Date  = TodoItem.getColumnIndexOrThrow(TodoDbAdapter.STR_KEY_DATE);
            int Body  = TodoItem.getColumnIndexOrThrow(TodoDbAdapter.STR_KEY_BODY);

            // Viewへ設定
            mTitleText.setText(TodoItem.getString(Title));
            mDate.setText(TodoItem.getString(Date));
            mBodyText.setText(TodoItem.getString(Body));

        } else {
            // Todo実施期限に本日の日付を設定
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date(System.currentTimeMillis());
            mDate.setText(df.format(date));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // onResumeに備えて
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    /**
     * TodoItemの内容をDBへ保存.
     *
     */
    private void saveState() {
        // DBへ保存対象のデータを取得
        String title = mTitleText.getText().toString();
        String date = mDate.getText().toString();
        String body = mBodyText.getText().toString();

        // PrimaryKeyがない場合は
        if (mPrimaryKey == null) {
            long id = mDbHelper.createTodoItem(
                    title,
                    body,
                    date,
                    mState);
            if (id > 0) {
                mPrimaryKey = id;
            }
        } else {
            mDbHelper.updateTodoItem(
                    mPrimaryKey,
                    title,
                    body,
                    date,
                    mState);
        }
    }

    @Override
    protected void onActivityResult(
            int nRequestCode,
            int nResultCode,
            Intent data)
    {
        super.onActivityResult(nRequestCode, nResultCode, data);

        switch (nRequestCode)
        {
            case CState.N_ACTIVITY_DATE:

                if (data != null) {
                    mDate.setText(
                            data.getStringExtra(TodoDate.STR_KEY_YEAR)  + "/" +
                            data.getStringExtra(TodoDate.STR_KEY_MONTH) + "/" +
                            data.getStringExtra(TodoDate.STR_KEY_DAY)
                    );
                    saveState();
                }
                break;

            default:
                // Nothing to do
                break;
        }
    }
}

