package be.pjrotsaert.mijnhogent.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.Chamilo

class SplashActivity : Activity() {

    companion object {
        fun createIntent(ctx: Context): Intent {
            return Intent(ctx, SplashActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs  = getSharedPreferences("app", Context.MODE_PRIVATE)
        if(prefs.contains("session")){
            Chamilo.getInstance(this).restoreSessionData(prefs.getString("session", "{}")) { result ->
                if(result == null){
                    startActivity(MainActivity.createIntent(this))
                    finish()
                } else {
                    startActivity(LoginActivity.createIntent(this))
                    finish()
                }
            }
        }
        else // No existing session, show the login screen after 2 seconds.
            Handler().postDelayed({
                startActivity(LoginActivity.createIntent(this))
                finish()
            }, 2000)
    }
}
