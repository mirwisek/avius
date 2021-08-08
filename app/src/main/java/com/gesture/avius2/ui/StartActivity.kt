package com.gesture.avius2.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.App
import com.gesture.avius2.BuildConfig
import com.gesture.avius2.R
import com.gesture.avius2.customui.CustomDialog
import com.gesture.avius2.network.ApiHelper
import com.gesture.avius2.utils.*
import com.gesture.avius2.viewmodels.AppViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText


class StartActivity : AppCompatActivity() {

    private lateinit var etCompanyID: TextInputEditText
    private lateinit var etPointID: TextInputEditText
    private lateinit var btnSubmit: MaterialButton
    private lateinit var errorView: ConstraintLayout
    private lateinit var dialogLoading: CustomDialog

    private lateinit var vmApp: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val parent = findViewById<ViewGroup>(R.id.parent)
        val app = application as App

        vmApp = ViewModelProvider(this).get(AppViewModel::class.java)
        window.statusBarColor = app.themeColor

        vmApp.isOnline.observe(this) { isOnline ->
            if(!isOnline){  // When offline show snack red
                parent.makeNetworkSnack(isOnline)
            } else if(isOnline && !app.wasOnline) {
                // When offline and switched to back online show green
                parent.makeNetworkSnack(isOnline)
            }
            app.wasOnline = isOnline
        }

        etCompanyID = findViewById<TextInputEditText>(R.id.etCompanyID)
        etPointID = findViewById<TextInputEditText>(R.id.etPointID)
        btnSubmit = findViewById<MaterialButton>(R.id.btnSubmit)

        // DEBUG ONLY
//        etPointID.setText("11")
//        etCompanyID.setText("IMH0010")

        errorView = findViewById<ConstraintLayout>(R.id.errorLayout)
        errorView.setOnClickListener {
            errorView.invisible()
        }

        // Set underlined style for error text
        val textKeepCalm = findViewById<TextView>(R.id.keepCalm)
        textKeepCalm.text = SpannableString(textKeepCalm.text).apply {
            setSpan(UnderlineSpan(), 0, length, 0)
        }

        dialogLoading = CustomDialog(this) { parent, dialog ->
            val v = layoutInflater.inflate(R.layout.layout_dialog_loading, parent)
        }

        etCompanyID.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0.isNullOrEmpty() || p0.length < 3) {
                    etCompanyID.error = "Company ID must be > 3 characters"
                } else {
                    etCompanyID.error = null
                }
            }
        })

        etPointID.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                // Dismiss keyboard before submitting to show the error if any
                hideKeyboard(currentFocus!!.windowToken)
                checkNetworkAndSubmit(parent)
                return@setOnEditorActionListener true
            }
            false
        }

        btnSubmit.setOnClickListener {
            checkNetworkAndSubmit(parent)
        }

    }

    private fun checkNetworkAndSubmit(parent: View) {
        if(vmApp.isOnline.value!!) {
            submitForm()
        } else {
            parent.makeNetworkSnack(false)
        }
    }

    private fun submitForm() {
        if(etCompanyID.text.isNullOrEmpty() || etPointID.text.isNullOrEmpty()) {
            showError(errorView, "Text fields can't be empty")
        } else if(etCompanyID.error == null && etPointID.error == null) {
            val company = etCompanyID.text.toString()
            val pointId = etPointID.text.toString()

            dialogLoading.show()

            ApiHelper.getQuestions(company, pointId) { result ->
                result.fold(
                    onSuccess = {
                        vmApp.saveQuestions(it)
                        vmApp.saveSettings(it)
                        dialogLoading.dismiss()
                        start()
                    },
                    onFailure = {
                        errorView.visible()
                        it.printStackTrace()
                        dialogLoading.dismiss()
                    }
                )
            }
        } else {
            showError(errorView, "Please fix all errors first")
        }
    }

    // Hide Keyboard when clicked outside the focus area
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            hideKeyboard(currentFocus!!.windowToken)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun showError(parent: View, msg: String) {
        Snackbar.make(parent, msg, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.RED)
            .setBackgroundTint(Color.WHITE)
            .show()
    }

    private fun start() {
        // Custom variable DEBUG_MODE in BuildGradle
        val intent = if(BuildConfig.DEBUG_MODE)
            Intent(this, TestActivity::class.java)
        else
            Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}