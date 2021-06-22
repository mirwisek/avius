package com.gesture.avius2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.gesture.avius2.R
import com.gesture.avius2.model.QuestionMultiLang

class QuestionHolderFragment: Fragment() {

    private val questionEng: String? by lazy { arguments?.getString(KEY_QUESTION_ENG) }
    private val questionArabic: String? by lazy { arguments?.getString(KEY_QUESTION_ARABIC) }
    private val themeColor: Int? by lazy { arguments?.getInt(KEY_THEME_COLOR) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_q_text, container, false)

        v.findViewById<TextView>(R.id.question).apply {
            text = "$questionEng\n$questionArabic"
            themeColor?.let { color ->
                setTextColor(color)
            }
        }

        return v
    }

    companion object {
        private const val KEY_QUESTION_ENG = "question.eng"
        private const val KEY_QUESTION_ARABIC = "question.arabic"
        private const val KEY_THEME_COLOR = "theme.color"

        @JvmStatic
        fun newInstance(question: QuestionMultiLang, themeColor: Int): QuestionHolderFragment {
            val args = Bundle()
            args.putString(KEY_QUESTION_ENG, question.english)
            args.putString(KEY_QUESTION_ARABIC, question.arabic)
            args.putInt(KEY_THEME_COLOR, themeColor)
            val fragment = QuestionHolderFragment()
            fragment.arguments = args
            return fragment
        }
    }
}