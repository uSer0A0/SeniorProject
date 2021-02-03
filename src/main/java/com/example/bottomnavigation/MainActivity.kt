package com.example.bottomnavigation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bottomnavigation.MainActivity.Companion.seedCoordinateList
import com.example.bottomnavigation.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    //座標・色情報を保持するためのクラス
    class SeedCoordinate constructor(var cX: Double, var cY: Double)
    class ColorCoordinate constructor(var red: Double, var green: Double, var blue: Double)

    companion object{
        private var drawView: View? = null
        private var paintView: View? = null
        private var galleryView: View? = null
        private var bitmap: Bitmap? = null
        private var colorFlag: Boolean = false
        private var colorPaintFlag: Boolean = false
        private var eraserFlag: Boolean = false
        private var eraserPaintFlag: Boolean = false
        private var setFlag: Boolean = true
        val seedCoordinateList: MutableList<SeedCoordinate> = ArrayList()
        val colorList: MutableList<ColorCoordinate> = ArrayList()
        val undoSeedList: MutableList<SeedCoordinate> = ArrayList()
        val undoColorList: MutableList<ColorCoordinate> = ArrayList()

        fun set_drawView(saveView: View?){
            drawView = saveView
        }

        fun get_drawView(): View?{
            return drawView
        }

        fun set_paintView(saveView: View?){
            paintView = saveView
        }

        fun get_paintView(): View?{
            return paintView
        }

        fun set_galleryView(saveView: View?){
            galleryView = saveView
        }

        fun get_galleryView(): View?{
            return galleryView
        }


        fun set_Bitmap(saveBitmap: Bitmap?){
            bitmap = saveBitmap
        }

        fun get_Bitmap(): Bitmap?{
            return bitmap
        }

        fun set_drawColorFlag(){
            colorFlag = !colorFlag
        }

        fun get_drawColorFlag(): Boolean {
            return colorFlag
        }

        fun set_drawEraserFlag(){
            eraserFlag = !eraserFlag
        }

        fun get_drawEraserFlag(): Boolean {
            return eraserFlag
        }

        fun set_paintColorFlag(){
            colorPaintFlag = !colorPaintFlag
        }

        fun get_paintColorFlag(): Boolean {
            return colorPaintFlag
        }

        fun set_paintEraserFlag(){
            eraserPaintFlag = !eraserPaintFlag
        }

        fun get_paintEraserFlag(): Boolean {
            return eraserPaintFlag
        }

        fun set_Flag(flag: Boolean) {
            setFlag = flag
        }

        fun get_Flag(): Boolean {
            return setFlag
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)  //superコールの前にスタイル設定（LauncherScreenを入れたので）
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications))

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

}
