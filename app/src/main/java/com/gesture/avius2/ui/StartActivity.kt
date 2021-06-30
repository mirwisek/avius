package com.gesture.avius2.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.customui.CustomDialog
import com.gesture.avius2.network.ApiHelper
import com.gesture.avius2.utils.invisible
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.visible
import com.gesture.avius2.viewmodels.AppViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText


class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val vmApp = ViewModelProvider(this).get(AppViewModel::class.java)

        val etCompanyID = findViewById<TextInputEditText>(R.id.etCompanyID)
        val etPointID = findViewById<TextInputEditText>(R.id.etPointID)
        val btnSubmit = findViewById<MaterialButton>(R.id.btnSubmit)

        // DEBUG ONLY
        etPointID.setText("11")
        etCompanyID.setText("IMH0010")

        val errorView = findViewById<ConstraintLayout>(R.id.errorLayout)
        errorView.setOnClickListener {
            errorView.invisible()
        }

        // Set underlined style for error text
        val textKeepCalm = findViewById<TextView>(R.id.keepCalm)
        textKeepCalm.text = SpannableString(textKeepCalm.text).apply {
            setSpan(UnderlineSpan(), 0, length, 0)
        }

        val dialogLoading = CustomDialog(this) { parent, dialog ->
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


        btnSubmit.setOnClickListener {

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

    }

    private fun showError(parent: View, msg: String) {
        Snackbar.make(parent, msg, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.RED)
            .setBackgroundTint(Color.WHITE)
            .show()
    }

    private fun start() {
//        val intent = Intent(this, MainActivity::class.java)
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
        finish()
    }
}