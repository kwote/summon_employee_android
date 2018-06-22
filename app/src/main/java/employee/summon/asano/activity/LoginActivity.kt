package employee.summon.asano.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import employee.summon.asano.AndroidDisposable
import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.addTo
import employee.summon.asano.model.LoginCredentials
import employee.summon.asano.rest.PeopleService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_login.*

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    private var inProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        // Set up the login form.

        password_login.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        email_sign_in_button.setOnClickListener { attemptLogin() }
        email_register_login_button.setOnClickListener { registerNewPerson() }
    }

    private fun registerNewPerson() {
        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private val disposable = AndroidDisposable()
    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private fun attemptLogin() {
        if (inProgress) {
            return
        }
        inProgress = true

        // Reset errors.
        email_login.error = null
        password_login.error = null

        // Store values at the time of the login attempt.
        val emailStr = email_login.text.toString()
        val passwordStr = password_login.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password_login.error = getString(R.string.error_invalid_password)
            focusView = password_login
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(emailStr)) {
            email_login.error = getString(R.string.error_field_required)
            focusView = email_login
            cancel = true
        } else if (!isEmailValid(emailStr)) {
            email_login.error = getString(R.string.error_invalid_email)
            focusView = email_login
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            val peopleService = App.getApp(this).getService<PeopleService>()
            val credentials = LoginCredentials(emailStr, passwordStr)
            peopleService.login(credentials)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { showProgress(true) }
                    .doFinally { showProgress(false) }
                    .subscribe({
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                        intent.putExtra(App.ACCESS_TOKEN, it)
                        startActivity(intent)
                        finish()
                    }, {
                        Snackbar.make(email_login, R.string.connection_failed, Snackbar.LENGTH_LONG).show()
                    }).addTo(disposable)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private fun showProgress(show: Boolean) {
        inProgress = show
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
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
    }
}

