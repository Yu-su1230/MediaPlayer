package com.example.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面がはじめて作成されたときにだけ、Fragmentを追加する
        if (savedInstanceState == null) {
            // fragmentの追加・削除はTransactionを使用する
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment, MainFragment())
                .commit()
        }
    }
}
