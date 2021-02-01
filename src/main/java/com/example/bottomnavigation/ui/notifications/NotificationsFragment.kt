package com.example.bottomnavigation.ui.notifications

import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.fragment_notifications.view.*

class NotificationsFragment : Fragment() {

    //変数初期化
    private var root: View? = null

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

        val animalList = listOf<Animal>(
            Animal(R.drawable.pen),
            Animal(R.drawable.raight),
            Animal(R.drawable.start),
            Animal(R.drawable.color),
            Animal(R.drawable.delete),
            Animal(R.drawable.delete),
            Animal(R.drawable.delete),
            Animal(R.drawable.delete),
            Animal(R.drawable.delete)
        )

        view.recycler_view.adapter = CustomAdapter(animalList)
        view.recycler_view.layoutManager = GridLayoutManager(context, 1, RecyclerView.VERTICAL, false)
    }

    override fun onPause() {
        set_galleryView(root)
        Log.d("Fragment", "onPause")
        super.onPause()
    }
}