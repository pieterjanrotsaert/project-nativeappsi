package be.pjrotsaert.mijnhogent.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.Chamilo

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Chamilo.getInstance(this).login("", ""){
            err ->
            if(err != null)
                print(err.getName(this))
        }


    }
}
