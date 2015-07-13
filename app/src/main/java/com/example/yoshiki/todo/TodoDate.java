package com.example.yoshiki.todo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

/**
 * Todo実施期限編集用のクラス.
 * @author 清兼
 */
public class TodoDate extends Activity {
    /**
     * キー値定義
     */
    public static final String STR_KEY_DATE   = "date";    // Intentから日付を取得
    public static final String STR_KEY_YEAR   = "year";    // Intentへ年を保存
    public static final String STR_KEY_MONTH  = "month";   // Intentへ月を保存
    public static final String STR_KEY_DAY    = "day";     // Intentへ日を保存

    /**
     * ToDoItem実施期限を設定する.
     *
     * @param savedInstanceState 保存されていたアプリケーション情報
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Viewの表示
        setContentView(R.layout.todo_date);

        // インテントから日付を取得
        Intent i = getIntent();
        final String strDate = i.getStringExtra(STR_KEY_DATE);

        // 取得した日付を分割
        String[] strWork = strDate.split("/", 0);

        // 取得した日付を設定
        final DatePicker datePicker = (DatePicker)findViewById(R.id.datePicker);
        datePicker.updateDate(
                Integer.parseInt(strWork[0]),       // 年
                Integer.parseInt(strWork[1]) - 1,   // 月
                Integer.parseInt(strWork[2])        // 日
        );

        //******************************************
        //
        // OnClickListener定義
        //
        //******************************************
        // dateSet
        final Button dateSet = (Button)findViewById(R.id.dateSet);
        dateSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DatePikerの値を設定
                Intent i = new Intent();
                i.putExtra(STR_KEY_YEAR , String.valueOf(datePicker.getYear()));        // 年
                i.putExtra(STR_KEY_MONTH, String.valueOf(datePicker.getMonth() + 1));   // 月
                i.putExtra(STR_KEY_DAY  , String.valueOf(datePicker.getDayOfMonth()));  // 日

                // Activityの戻りを設定
                setResult(RESULT_OK, i);

                // Activity終了
                finish();
            }
        });
    }

}
