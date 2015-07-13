package com.example.yoshiki.todo;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * TodoAppのメインクラス.本クラスから各クラスへ処理を流していく.
 * @author 清兼
 */
public class TodoApp extends ListActivity {

    /**
     * メニューボタンのID定義
     */
    private static final int N_INSERT_ID = Menu.FIRST;        // Todoアイテム追加
    private static final int N_DELETE_ID = Menu.FIRST + 1;    // Todoアイテム削除

    /**
     * メンバ変数定義
     */
    private TodoDbAdapter mDbHelper;
    private Cursor mTodoCursor;

    /**
     * アプリケーションのメイン画面を表示し、Todoアイテムが保存されているDBを読み込み.
     *
     * @param savedInstanceState 保存されていたアプリケーション情報
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Viewの表示
        setContentView(R.layout.todo_list);

        // DBアクセスクラスのインスタンスの生成
        mDbHelper = new TodoDbAdapter(this);

        // DBを開く。DBが存在しない場合はDBを生成する。
        mDbHelper.open();

        // Todoアイテムリストを表示
        fillData();

/* 清）テストのためコメントアウト
        // registerForContextMenu
        // ->長押しするとコンテキストメニューが表示される。
        // getListView
        // ->Activityに対するローカルListViewオブジェクトを返却する。
        registerForContextMenu(getListView());
*/
    }


    /**
     * DBからTodoアイテムを取得し、アプリケーションメイン画面に配置する.
     */
    private void fillData() {
        // DBよりデータ取得(State=Open)
        mTodoCursor = mDbHelper.fetchAllTodoItemsByState(TodoDbAdapter.STR_STATE_OPEN);

        // カーソル制御をシステムへ移譲
        startManagingCursor(mTodoCursor);

        // アプリケーションメイン画面に表示させたいColumn名を指定
        String[] strFrom = new String[]{TodoDbAdapter.STR_KEY_TITLE};

        // 表示させるViewを指定
        int[] nTo = new int[]{android.R.id.text1};

        // SimpleCursorAdapterインスタンス生成
        SimpleCursorAdapter TodoItems = new SimpleCursorAdapter(
                this,                  // Context
                R.layout.todo_row,     // 表示先のViewGroup
                mTodoCursor,           // DBのカーソル
                strFrom,               // 表示させたいColumn名
                nTo);                  // 表示先のView

        // アプリケーションメイン画面へ表示
        setListAdapter(TodoItems);

        // リスナー登録
        ListView listView = getListView();
        listView.setMultiChoiceModeListener(new Callback());
    }

    /**
     * Todoアイテムを長押しした際に起動する複数選択モードのリスナー.
     * @author 清兼
     */
    private class Callback implements ListView.MultiChoiceModeListener {

        /**
         * 最初の1つ目を長押しして複数選択モードへ遷移時に、初期化処理を実行するメソッド.
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // アクションモード初期化処理
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // アクションアイテム選択時
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // 決定ボタン押下時
            final int checkedCount = getListView().getCheckedItemCount();
            SparseBooleanArray list = getListView().getCheckedItemPositions();
            String str = "";
            /*
            for(int i=0;i<GENRES.length;i++){
                boolean checked = list.get(i);
                if (checked == true){
                    str = str+GENRES[i] + " ";
                }
            }

            Intent intent = new Intent(getApplicationContext(), ResultDisp.class);
            intent.putExtra("checked_list", str);
            startActivity(intent);
            */
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // アクションモード表示事前処理
            return true;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                                              int position, long id, boolean checked) {
            // アクションモード時のアイテムの選択状態変更時
            return;
        }
    }
    /**
     * メニューボタンを押したときに表示されるアイテムを生成する
     *
     * @param menu メニューインスタンス
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add TodoItem
        menu.add(
                0,                      // メニューのグループ識別子
                N_INSERT_ID,            // メニューID
                0,                      // アイテムの順序（0は優先度なし）
                R.string.menu_insert);       // メニューに表示する文字列

        return true;
    }

    /**
     * メニューのアイテム選択時の動作
     *
     * @param nFeatureId ?
     * @param item 選択されたメニューアイテム
     */
    @Override
    public boolean onMenuItemSelected(
            int nFeatureId,
            MenuItem item) {

        super.onMenuItemSelected(nFeatureId, item);

        // メニューボタンのIDで分岐
        switch(item.getItemId()) {

            // Add TodoItem
            case N_INSERT_ID:
                // Todoアイテムを作成
                createTodoItem();
                break;

            default:
                // Nothing to do
                break;
        }

        // ?
        return true;
    }


    /**
     * Todoアイテムを長押ししたときに表示するメニューを生成.
     *
     * @param menu メニュー
     * @param v メニューを取得するView
     * @param menuInfo ?
     */
    @Override
    public void onCreateContextMenu(
            ContextMenu menu,
            View v,
            ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        // メニューを追加
        menu.add(
                0,                      // メニューのグループ識別子
                N_DELETE_ID,              // メニューID
                0,                      // アイテムの順序（0は優先度なし）
                R.string.menu_delete);  // メニューに表示する文字列
    }

    /**
     * 長押しメニューのアイテムを選択した時の動作を定義
     * @param item 対象のTodoアイテム
     * @return 動作結果
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        // メニューボタンのIDで分岐
        switch(item.getItemId()) {

            // Delete TodoItem
            case N_DELETE_ID:
                // 対象のTodoアイテムを取得
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

                // 対象のTodoアイテムのidを引き渡し、削除を実施
//               mDbHelper.deleteNote(info.id);

                // 削除後のTodoアイテムリストを表示
                fillData();
                break;

            default:
                // Nothing to do
                break;
        }

        return true;
    }

    /**
     * Todoアイテム編集用のActivityを起動(新規作成).
     */
    private void createTodoItem() {
        // TodoEditへインテント
        Intent i = new Intent(this, TodoEdit.class);

        // Activity起動
        startActivityForResult(
                i,                                             // インテント
                CState.N_ACTIVITY_CREATE);   // Todoアイテム作成
    }

    /**
     * Todoアイテム編集用のActivityを起動(既存Todoアイテムの更新).
     *
     * @param l 呼び出し元のListViewオブジェクト
     * @param v ユーザが選択したTodoItem
     * @param nPosition ユーザが選択したTodoItemのポジション
     * @param nId ユーザがクリックしたTodoItemのID
     */
    @Override
    protected void onListItemClick(
            ListView l,
            View v,
            int nPosition,
            long nId)
    {
        super.onListItemClick(l, v, nPosition, nId);

        // TodoEditへインテント
        Intent i = new Intent(this, TodoEdit.class);

        // インテントへidを設定
        i.putExtra(TodoDbAdapter.STR_KEY_PRIMARY, nId);

        // Activity起動
        startActivityForResult(
                i,                                           // インテント
                CState.N_ACTIVITY_EDIT);   // TodoItem編集
    }

    /**
     * Activityが結果を戻してきた時にコールされるメソッド
     *
     * @param nRequestCode Activity起動時の状態定義
     * @param nResultCode Activityの結果
     * @oaram intent インテント
     */
    @Override
    protected void onActivityResult(
            int nRequestCode,
            int nResultCode,
            Intent intent)
    {
        super.onActivityResult(nRequestCode, nResultCode, intent);

        switch (nRequestCode)
        {
            case CState.N_ACTIVITY_CREATE:  // Throw
            case CState.N_ACTIVITY_EDIT:
                // 最新のTodoアイテムリストを表示
                fillData();
                break;

            default:
                // Nothing to do
                break;
        }
    }
}
