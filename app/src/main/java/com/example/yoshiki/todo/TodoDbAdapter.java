package com.example.yoshiki.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Matrix;
import android.util.Log;

/**
 * DBアクセスクラス
 * @author 清兼
 */
public class    TodoDbAdapter {

    /**
     * クラス名定義
     */
    private static final String STR_CLASS_NAME = "TodoDbAdapter";

    /**
     * DB情報定義
     */
    private static final String STR_DATABASE_NAME   = "todo";         // DB名
    private static final String STR_DATABASE_TABLE  = "todoItem";     // テーブル名
    private static final int    N_DATABASE_VERSION  = 1;              // バージョン

    /**
     * Column定義
     */
    public static final String STR_KEY_PRIMARY  = "_id";    // Primary Key
    public static final String STR_KEY_TITLE    = "title";  // Todoアイテムのタイトル
    public static final String STR_KEY_BODY     = "body";   // Todoアイテムの内容
    public static final String STR_KEY_DATE     = "date";   // Todo実施期限
    public static final String STR_KEY_STATE    = "state";  // Todo状態
    private static final String[] STR_TARGET_COLUMNS =
        {STR_KEY_PRIMARY, STR_KEY_TITLE, STR_KEY_BODY, STR_KEY_DATE, STR_KEY_STATE};    // DBから取得するColumn名一覧

    /**
     * Todoアイテムの状態定義
     */
    public static final String STR_STATE_OPEN   = "open";   // Todoアイテムが未完了
    public static final String STR_STATE_CLOSE  = "close";  // Todoアイテムが完了

    /**
     * TABLE Create用構文定義
     *  Column：
     *      integer         _id     PrimaryKey
     *      text(not null)  title   Todoアイテムのタイトル
     *      text(not null)  body    Todoアイテムの内容
     *      text(not null)  date    Todo実施期限
     *      text(not null)  state   Todo状態（Open or Close）
     */
    private static final String DATABASE_CREATE =
            "create table TodoItem (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, date text not null, state text not null);";

    /**
     * TABLE Drop用構文定義
     *  テーブル名：todoItem
     */
    private static final String DATABASE_DROP = "DROP TABLE IF EXISTS todoItem";

    /**
     * メンバ変数定義
     */
    private DatabaseHelper mDbHelper;       // DatabaseHelperインスタンス
    private SQLiteDatabase mDb;             // DBハンドリング用のインスタンス
    private final Context mCtx;             // Contextインスタンス

    /**
     * コンストラクタ
     *
     * @param ctx   コンテキスト
     */
    public TodoDbAdapter(Context ctx)
    {
        this.mCtx = ctx;
    }

    /**
     * DBを生成し、メンバへインスタンスを保存.
     *
     * @return TodoDbAdapterインスタンス 清）なんでリターン必要？いらなくない？
     * @throws SQLException
     */
    public TodoDbAdapter open() throws SQLException
    {
        // DatabaseHelperのインスタンス生成
        mDbHelper = new DatabaseHelper(mCtx);

        // データベースハンドリング用のインスタンスを取得
        mDb = mDbHelper.getWritableDatabase();

        // TodoDbAdapterのインスタンスをリターン
        return this;
    }

    /**
     * DBのclose.
     *
     */
    public void close()
    {
        mDbHelper.close();
    }


    /**
     * TodoアイテムをDBへ保存する.
     *
     * @param strTitle Todoアイテムのタイトル
     * @param strBody  Todoアイテムの内容
     * @param strDate  Todo実施期限
     * @param strState Todo状態
     * @return _id or -1 if failed
     */
    public long createTodoItem(
            String strTitle,
            String strBody,
            String strDate,
            String strState)
    {
        // インスタンス生成
        ContentValues initialValues = setColumn(
                strTitle,    // Todoアイテムのタイトル
                strBody,     // Todoアイテムの内容
                strDate,     // Todo実施期限
                strState     // Todo状態
        );

        // DBへ保存
        return mDb.insert(
                STR_DATABASE_TABLE,     // テーブル名
                null,                   // null値の格納が許可されていないカラムに代わりに利用される値
                initialValues);         // DBへ保存する情報
    }

    /**
     * DBからTodoアイテム情報を全て取得.
     *
     * @return 取得した全TodoアイテムのDBカーソル
     */
    public Cursor fetchAllTodoItems()
    {
        // 全Todoアイテム取得
        return mDb.query(
                STR_DATABASE_TABLE,     // テーブル名
                STR_TARGET_COLUMNS,     // 取得対象のColumn
                null, null,             // 取得するレコードの条件
                null, null, null);      // groupby, Having, orderby, limit句
    }

    /**
     * DBからStateが合致するToDoアイテムを全て取得.
     *
     * @param strState 取得対象のState
     * @return Cursor 取得したTodoアイテムのDBカーソル
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchAllTodoItemsByState(
            String strState) throws SQLException
    {
        String strWhere = "state like ?";

        // Stateが合致するTodoアイテムを取得
        Cursor mCursor = mDb.query(
                STR_DATABASE_TABLE,                  // テーブル名
                STR_TARGET_COLUMNS,                  // 取得対象のColumn
                "state like ?",                      // 取得するレコードの条件
                new String[]{strState},
                null, null, null, null);       // groupby, Having, orderby, limit句

        // Todoアイテムの取得に成功した場合
        if (mCursor != null) {
            // カーソルを先頭へ移動
            mCursor.moveToFirst();
        }

        // カーソルをリターン
        return mCursor;
    }

    /**
     * DBからPrimaryKeyで指定されたToDoアイテムを取得.
     *
     * @param nPrimaryKey 取得対象のPrimaryKey
     * @return Cursor 取得したTodoアイテムのDBカーソル
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchToDoItemByPrimaryKey(
            long nPrimaryKey) throws SQLException
    {
        // PrimaryKeyで指定されたTodoアイテムを取得
        Cursor mCursor = mDb.query(
                true,                                // 検索結果から重複する行を削除
                STR_DATABASE_TABLE,                  // テーブル名
                STR_TARGET_COLUMNS,                  // 取得対象のColumn
                STR_KEY_PRIMARY + "=" + nPrimaryKey, // 取得するレコードの条件
                null, null, null, null, null);       // groupby, Having, orderby, limit句

        // Todoアイテムの取得に成功した場合
        if (mCursor != null) {
            // カーソルを先頭へ移動
            mCursor.moveToFirst();
        }

        // カーソルをリターン
        return mCursor;
    }

    /**
     * PrimaryKeyで指定されたTodoアイテムをDBから削除.
     *
     * @param  nPrimaryKey 削除対象のPrimaryKey
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long nPrimaryKey[])
    {
        // 配列のサイズを取得
        long nArraySize = nPrimaryKey.length;

        // DBから削除
        for(int nIndex = 0; nIndex < nArraySize; nIndex++)
        {
            // Todoアイテムの削除
            mDb.delete(
                    STR_DATABASE_TABLE,                         // テーブル名
                    STR_KEY_PRIMARY + "=" + nPrimaryKey[nIndex],// 削除の対象となるレコードを特定にするための条件（WHERE句）
                    null);                                      // 不明
        }

        return true;
    }

    /**
     * Todoアイテムを編集した場合に、DBの内容をアップデートする.
     *
     * @param nPrimaryKey アップデート対象のPrimaryKey
     * @param strTitle Todoアイテムのタイトル
     * @param strBody Todoアイテムの内容
     * @param strDate Todo実施期限
     * @param strState Todo状態
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateTodoItem(
            long   nPrimaryKey,
            String strTitle,
            String strBody,
            String strDate,
            String strState)
    {
        // インスタンス生成
        ContentValues args = setColumn(
                strTitle,    // Todoアイテムのタイトル
                strBody,     // Todoアイテムの内容
                strDate,     // Todo実施期限
                strState     // Todo状態
        );

        // DBの内容をアップデート
        return mDb.update(
                STR_DATABASE_TABLE,                             // テーブル名
                args,                                           // アップデートする内容
                STR_KEY_PRIMARY + "=" + nPrimaryKey, null) > 0; // アップデート対象のPrimaryKey
    }

    /**
     * DB Createクラス
     *
     * @author 清兼
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        /**
         * コンストラクタ
         *
         * @param context   コンテキスト
         */
        DatabaseHelper(Context context)
        {
            super(  context,                // コンテキスト
                    STR_DATABASE_NAME,      // DB名
                    null,                   // ?
                    N_DATABASE_VERSION);    // バージョン
        }

        /**
         * DB Create.
         * DBが存在しない状態でDBをOpenしようとする場合に起動.
         *
         * @param db    新規作成したDBインスタンス
         */
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
        }

        /**
         * コンストラクタで渡されたDBのバージョンと、実際に存在するDBのバージョンが異なる場合、
         * テーブルの再構成を行う.
         *
         * @param db            DBインスタンス
         * @param nOldVersion    旧バージョン番号
         * @param nNewVersion    新バージョン番号
         */
        @Override
        public void onUpgrade(
                SQLiteDatabase db,
                int nOldVersion,
                int nNewVersion)
        {
            // ログ出力
            Log.w(STR_CLASS_NAME,                      // クラス名
                    "旧DBバージョン " + nOldVersion +    // 出力文字列
                            " 新DBバージョン " + nNewVersion + ", 旧データを全削除");

            // テーブルの削除
            db.execSQL(DATABASE_DROP);

            // DB Create
            onCreate(db);
        }
    }

    /**
     * Columnと保存する値をセットで設定する.
     *
     * @param strTitle Todoアイテムのタイトル
     * @param strBody Todoアイテムの内容
     * @param strDate Todo実施期限
     * @param strState Todo状態
     * @return true if the note was successfully updated, false otherwise
     */
    private ContentValues setColumn(
            String strTitle,
            String strBody,
            String strDate,
            String strState
    )
    {
        ContentValues Dst = new ContentValues();
        Dst.put(STR_KEY_TITLE, strTitle); // Todoアイテムのタイトル
        Dst.put(STR_KEY_BODY , strBody);  // Todoアイテムの内容
        Dst.put(STR_KEY_DATE , strDate);  // Todo実施期限
        Dst.put(STR_KEY_STATE, strState); // Todo状態
        return Dst;
    }
}
