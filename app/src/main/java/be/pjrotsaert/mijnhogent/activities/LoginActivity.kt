package be.pjrotsaert.mijnhogent.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import be.pjrotsaert.mijnhogent.R
import be.pjrotsaert.mijnhogent.api.Chamilo
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {


    companion object {
        fun createIntent(ctx: Context): Intent {
            val intent = Intent(ctx, LoginActivity::class.java)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up the login form.
        populateAutoComplete()
        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
    }

    private fun populateAutoComplete() {
        val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
        if(prefs.contains("username"))
            email.setText(prefs.getString("username", ""))
        if(prefs.contains("password"))
            password.setText(prefs.getString("password", ""))
    }

    /**
     * Attempts to sign in.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        // Reset errors.
        email.error = null
        password.error = null

        // Store values at the time of the login attempt.
        val emailStr = email.text.toString()
        val passwordStr = password.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(passwordStr) || !isPasswordValid(passwordStr)) {
            password.error = getString(R.string.error_field_required)
            focusView = password
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email.error = getString(R.string.error_field_required)
            focusView = email
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email.error = getString(R.string.error_invalid_email)
            focusView = email
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            val prefs = getSharedPreferences("app", Context.MODE_PRIVATE)
            prefs.edit().putString("username", emailStr).apply() // Store the username
            prefs.edit().putString("password", passwordStr).apply() // Store the username

            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            val imm: InputMethodManager = this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).rootView.windowToken, 0)
            Chamilo.getInstance(this).login(emailStr, passwordStr) {
                err ->
                if(err == null){
                    // Store session info so the user doesn't need to log in everytime the app is started.
                    prefs.edit().putString("session", Chamilo.getInstance(this).getSessionData()).apply()
                    startActivity(MainActivity.createIntent(this)) // Proceed to main activity
                    finish()
                } else {
                    showProgress(false)
                    if(err.getNameId() == R.string.err_wrongcredentials) {
                        password.error = getString(err.getDescriptionId())
                        password.requestFocus()
                    }
                    else {
                        email.error = getString(err.getDescriptionId())
                        email.requestFocus()
                    }
                }
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Add more checks
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @SuppressLint("ObsoleteSdkInt")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

            login_form.visibility = if (show) View.GONE else View.VISIBLE
            login_form.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 0 else 1).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_form.visibility = if (show) View.GONE else View.VISIBLE
                        }
                    })

            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            login_progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.visibility = if (show) View.VISIBLE else View.GONE
            login_form.visibility = if (show) View.GONE else View.VISIBLE
        }
    }


}
