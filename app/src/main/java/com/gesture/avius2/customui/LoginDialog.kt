package com.gesture.avius2.customui

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.gesture.avius2.R
import com.gesture.avius2.network.ApiHelper
import com.gesture.avius2.network.models.AnswerResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginDialog() : DialogFragment() {

    private var successListener: ((errorMessage: String?) -> Unit)? = null
    private var errorListener: ((t: Throwable) -> Unit)? = null
    private var dismissListener: ((isScheduledLogout: Boolean) -> Unit)? = null

    private var textError: TextView? = null

    private var isScheduledLogout = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val v = inflater.inflate(R.layout.layout_login, container, false)

        textError = v.findViewById<TextView>(R.id.textError)

        val etPassword = v.findViewById<TextInputEditText>(R.id.etPassword)
        val etEmail = v.findViewById<TextInputEditText>(R.id.etEmail)
        val btnSubmit = v.findViewById<MaterialButton>(R.id.btnSubmit)

        etEmail.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0 != null && p0.length > 5 && p0.contains("@")) {
                    etEmail.error = null
                } else {
                    etEmail.error = "Please provide valid email"
                }
            }
        })

        btnSubmit.setOnClickListener {
            textError!!.text = ""   // Reset error before trying
            if(etEmail.text.isNullOrEmpty() || etPassword.text.isNullOrEmpty()) {
                textError!!.text = "Text fields can't be empty"
            } else if(etEmail.error == null && etPassword.error == null) {
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                /**
                 * IF users dismiss from this moment, parent will know there is possibility of logout
                 */
                isScheduledLogout = true
                authenticate(email, password)
            }
        }

        return v
    }


    private fun authenticate(email: String, password: String) {
        ApiHelper.logout(email, password, object: Callback<AnswerResponse> {
            override fun onResponse(
                call: Call<AnswerResponse>,
                response: Response<AnswerResponse>
            ) {
                var isSuccess = false
                var msg: String? = null
                response.body()?.let {
                    if(it.status.contentEquals("success")) {
                        isSuccess = true
                    } else if(it.status.contentEquals("failed")) {
                        isSuccess = true
                        msg = "Invalid email or password, please try again!"
                    }
                }
                if(isSuccess)
                    successListener?.invoke(msg)
                else
                    errorListener?.invoke(Throwable("Couldn't logout, error ${response.code()}"))
                textError?.text = msg
                isScheduledLogout = false   // Reset value
            }

            override fun onFailure(call: Call<AnswerResponse>, t: Throwable) {
                errorListener?.invoke(t)
                t.printStackTrace()
                isScheduledLogout = false   // Reset value
            }

        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.invoke(isScheduledLogout)
    }

    fun setOnDismiss(onDismiss: (isScheduledLogout: Boolean) -> Unit) { dismissListener = onDismiss }
    fun setOnLoginError(onError: (t: Throwable) -> Unit) { errorListener = onError }
    fun setOnLoginSuccess(onSuccess: (errorMessage: String?) -> Unit) { successListener = onSuccess }

    companion object {
        const val TAG = "Login.Dialog"
    }

}