package be.pjrotsaert.mijnhogent.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.Chamilo


class MainActivity : AppCompatActivity() {

    companion object {
        fun createIntent(ctx: Context): Intent {
            val intent = Intent(ctx, MainActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }
}
