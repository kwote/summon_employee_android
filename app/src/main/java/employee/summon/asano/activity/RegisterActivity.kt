package employee.summon.asano.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import employee.summon.asano.AndroidDisposable
import employee.summon.asano.R
import employee.summon.asano.addTo
import employee.summon.asano.app
import employee.summon.asano.model.AddPerson
import employee.summon.asano.rest.PeopleService
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_register.*
import java.util.regex.Pattern

/**
 * A registration screen
 */
class RegisterActivity : AppCompatActivity() {
    private var inProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        password_register.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptRegister()
                return@OnEditorActionListener true
            }
            false
        })

        email_register_button.setOnClickListener { attemptRegister() }
    }

    private val disposable = AndroidDisposable()
    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }


    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private fun attemptRegister() {
        if (inProgress) {
            return
        }
        inProgress = true

        // Reset errors.
        email_register.error = null
        password_register.error = null

        // Store values at the time of the attempt.
        val email = email_register.text.toString()
        val password = password_register.text.toString()
        val passwordConfirm = password_confirm.text.toString()
        val firstName = firstname.text.toString()
        val lastName = lastname.text.toString()
        var patronymic: String? = patronymic_view.text.toString()
        var phone: String? = phone_view.text.toString()
        var post: String? = post_view.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password)) {
            if (!isPasswordValid(password)) {
                password_register.error = getString(R.string.error_invalid_password)
                focusView = password_register
                cancel = true
            } else if (password != passwordConfirm) {
                password_confirm.error = getString(R.string.error_password_dont_match)
                focusView = password_confirm
                cancel = true
            }
        } else if (TextUtils.isEmpty(firstName)) {
            // Check for a valid first name, if the user entered one.
            firstname.error = getString(R.string.error_field_required)
            focusView = firstname
            cancel = true
        } else if (TextUtils.isEmpty(lastName)) {
            // Check for a valid last name, if the user entered one.
            lastname.error = getString(R.string.error_field_required)
            focusView = lastname
            cancel = true
        } else {
            if (TextUtils.isEmpty(patronymic)) {
                patronymic = null
            }

            if (TextUtils.isEmpty(post)) {
                post = null
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(email)) {
                email_register.error = getString(R.string.error_field_required)
                focusView = email_register
                cancel = true
            } else if (!isEmailValid(email)) {
                email_register.error = getString(R.string.error_invalid_email)
                focusView = email_register
                cancel = true
            } else {
                if (TextUtils.isEmpty(phone)) {
                    phone = null
                } else if (!isPhoneValid(phone)) {
                    phone_view.error = getString(R.string.error_invalid_phone)
                    focusView = phone_view
                    cancel = true
                }
            }
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            val service = app.getService<PeopleService>()
            val addPerson = AddPerson(firstName, lastName, patronymic, post, email, phone, password)
            service.addPerson(addPerson)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe { showProgress(true) }
                    .doFinally { showProgress(false) }
                    .subscribe(
                            {
                                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                                startActivity(intent)
                                finish()
                            },
                            {
                                password_register.error = getString(R.string.error_unknown)
                                password_register.requestFocus()
                            })
                    .addTo(disposable)
        }
    }

    companion object Validator {
        private val PHONE_REGEX = Pattern.compile("^\\+([0-9\\-]?){9,11}[0-9]$")
        fun validatePhone(phone: String): Boolean {
            val matcher = PHONE_REGEX.matcher(phone)
            return matcher.matches()
        }
    }

    private fun isPhoneValid(phone: String?): Boolean {
        return phone?.let { validatePhone(it) } ?: false
    }

    private fun isEmailValid(email: String): Boolean {
        return email.contains("@")
    }

    private fun isPasswordValid(password: String): Boolean {
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
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        register_form.visibility = if (show) View.GONE else View.VISIBLE
        register_form.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                register_form.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        register_progress.visibility = if (show) View.VISIBLE else View.GONE
        register_progress.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                register_progress.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }
}

