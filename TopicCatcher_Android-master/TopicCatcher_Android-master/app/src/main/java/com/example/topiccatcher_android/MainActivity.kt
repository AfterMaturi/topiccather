package com.example.topiccatcher_android

import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.*
import com.example.topiccatcher_android.databinding.ActivityMainBinding
import com.example.topiccatcher_android.ui.slideshow.SlideshowFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)
/*
        // メールボタンに関する内容
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

 */

/*
        // サイドメニューの表示・ナビゲーションに関すること（Android StudioのNavigationDrawerのテンプレートそのまま）
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val sideNavController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.makeRoomFragment, R.id.roomFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(sideNavController, appBarConfiguration)
        navView.setupWithNavController(sideNavController)
 */

/*
        // 下部メニューコンポーネントの取得
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_view)
        // ナビゲーションフラグメントの取得
        val bottomNavController = findNavController(R.id.mobile_navigation)

        // 下部メニューとナビゲーションを関連付け
        NavigationUI.setupWithNavController(bottomNavView, bottomNavController)

 */
/*
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottom_nav_view)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavView.setupWithNavController(navController)
 */
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        drawerLayout = findViewById(R.id.drawer_layout)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)

        // BottomNavigationに関する内容
        findViewById<BottomNavigationView>(R.id.bottom_nav_view).setOnItemSelectedListener {
            item ->
                when(item.itemId){
                    R.id.nav_home ->{
                        Log.d("BottomNavigation", "home selected")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_content_main, HomeFragment())
                            .commit()
                        true
                    }
                    R.id.nav_gallery ->{
                        Log.d("BottomNavigation", "account selected")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_content_main, accountFragment())
                            .commit()
                        true
                    }
                    R.id.nav_slideshow ->{
                        Log.d("BottomNavigation", "room selected")
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment_content_main, SlideshowFragment())
                            .commit()
                        true
                    }

                    else -> {
                        false
                    }
                }
        }



        // ホーム画面読み込み（呼ばれるタイミングを考える必要あり）
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, HomeFragment())
            .commit()


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        //return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        return  NavigationUI.navigateUp(navController, drawerLayout)
    }



}