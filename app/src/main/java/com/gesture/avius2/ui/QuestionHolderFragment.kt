package com.gesture.avius2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gesture.avius2.R

class QuestionHolderFragment: Fragment() {

    private val question: String? by lazy { arguments?.getString(KEY_QUESTION) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_q_text, container, false)

        v.findViewById<TextView>(R.id.question).text = question

        return v
    }

    companion object {
        private const val KEY_QUESTION = "question.value"

        @JvmStatic
        fun newInstance(question: String): QuestionHolderFragment {
            val args = Bundle()
            args.putString(KEY_QUESTION, question)
            val fragment = QuestionHolderFragment()
            fragment.arguments = args
            return fragment
        }
    }
}