package com.example.bottomnavigation.ui.notifications

import android.content.ContentResolver
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bottomnavigation.MainActivity
import com.example.bottomnavigation.MainActivity.Companion.get_galleryView
import com.example.bottomnavigation.MainActivity.Companion.set_galleryView
import com.example.bottomnavigation.R
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*

class NotificationsFragment : Fragment() {

    //変数初期化
    private var root: View? = null
    private val handler = Handler()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //View呼び出し判定
        if(get_galleryView() == null){
            root = inflater.inflate(R.layout.fragment_notifications, container, false)
            Log.d("Fragment", "onCreateView")
        }else{
            root = get_galleryView()
            Log.d("Fragment", "onCreateView2")
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        //ギャラリースタートアニメーション
//        view.animationView_main.visibility = View.VISIBLE
//        handler.postDelayed({
//            view.animationView_main.visibility = View.INVISIBLE
//        }, 4500)

        //////////画像を取得する処理/////////////
        val contentResolver: ContentResolver = activity?.contentResolver!!
        var cursor: Cursor? = null

        // 例外を受け取る
        try {
            //cursorに端末内のすべての画像のURIを取得する
            cursor = contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, null, null, null)
            cursor?.moveToFirst()

            if (cursor != null && cursor.moveToFirst()) {
                val num = cursor.count - 1
                var galaly = mutableListOf<String>()
                for (i in 0..num) {
                    galaly.add((cursor.getString(cursor.getColumnIndex(
                            MediaStore.Images.Media.DATA))))
                    cursor.moveToNext()
                }

                view.recycler_view.adapter = CustomAdapter(galaly)
                view.recycler_view.layoutManager = GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)

                cursor.close()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            //MainActivityに戻す
        } finally {
            cursor?.close()
        }

    }

    override fun onPause() {
        set_galleryView(root)
        Log.d("Fragment", "onPause")
        super.onPause()
    }

}