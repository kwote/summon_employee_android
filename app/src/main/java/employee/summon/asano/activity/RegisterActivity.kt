package employee.summon.asano.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView

import employee.summon.asano.App
import employee.summon.asano.R
import employee.summon.asano.getStringTimeStampWithDate
import employee.summon.asano.model.AddPerson
import employee.summon.asano.model.Person
import employee.summon.asano.rest.IPeopleService
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.regex.Pattern

/**
 * A registration screen
 */
class RegisterActivity : AppCompatActivity() {
    private var inProgress = false

    private val app: App
        get() = application as App

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

        val mEmailRegisterButton = findViewById<Button>(R.id.email_register_button)
        mEmailRegisterButton.setOnClickListener { attemptRegister() }
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
        var patronymic : String? = patronymic_view.text.toString()
        var phone : String? = phone_view.text.toString()
        var post : String? = post_view.text.toString()

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
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true)
            val service = app.getService<IPeopleService>()
            val now = Calendar.getInstance().time.getStringTimeStampWithDate()
            val addPerson = AddPerson(firstName, lastName, patronymic, post, email, phone, password, now)
            val call = service.addPerson(addPerson)

            call.enqueue(object : Callback<Person> {
                override fun onResponse(call: Call<Person>, response: Response<Person>) {
                    showProgress(false)
                    if (!response.isSuccessful) {
                        val error = response.errorBody()

                        password_register.error = getString(R.string.error_incorrect_password)
                        password_register.requestFocus()
                    } else {
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onFailure(call: Call<Person>, t: Throwable) {
                    showProgress(false)
                }
            })
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
        if (phone != null) {
            return validatePhone(phone)
        }
        return false
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

