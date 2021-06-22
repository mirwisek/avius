package com.gesture.avius2.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.utils.log
import com.gesture.avius2.viewmodels.QuestionViewModel

class SubscriptionFragment: Fragment() {

    private val themeColor: Int? by lazy { arguments?.getInt(KEY_THEME_COLOR) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_subscription, container, false)


        v.findViewById<ImageView>(R.id.imageDone).apply {
            themeColor?.let { drawable.setTint(it) }
        }
        v.findViewById<TextView>(R.id.textSubscription).apply {
            themeColor?.let { setTextColor(it) }
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = (requireActivity().application as App).repository
        repo.answers.forEach {
            log("${it.index}) Q: ${it.question.english} -- A: ${it.answer}")
        }
    }

    companion object {
        const val TAG = "SubscriptionFragment"
        private const val KEY_THEME_COLOR = "theme.color"

        @JvmStatic
        fun newInstance(themeColor: Int): SubscriptionFragment {
            val args = Bundle()
            args.putInt(KEY_THEME_COLOR, themeColor)
            val fragment = SubscriptionFragment()
            fragment.arguments = args
            return fragment
        }
    }
}