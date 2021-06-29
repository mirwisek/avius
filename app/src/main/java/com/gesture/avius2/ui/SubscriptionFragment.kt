package com.gesture.avius2.ui

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.gesture.avius2.App
import com.gesture.avius2.R
import com.gesture.avius2.network.ApiHelper
import com.gesture.avius2.network.models.AnswerResponse
import com.gesture.avius2.utils.log
import com.gesture.avius2.utils.visible
import com.gesture.avius2.viewmodels.QuestionViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SubscriptionFragment: Fragment() {

    private val themeColor: Int? by lazy { arguments?.getInt(KEY_THEME_COLOR) }
    private var countDownCompleteListener: OnCountDownCompleteListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            countDownCompleteListener = context as OnCountDownCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnCountDownCompleteListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        countDownCompleteListener = null
    }


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

        ApiHelper.submitAnswer(repo.answers, object: Callback<AnswerResponse> {

            override fun onResponse(
                call: Call<AnswerResponse>,
                response: Response<AnswerResponse>
            ) {
                var isSuccess = false
                response.body()?.let {
                    if(it.status.contentEquals("success")) {
                        isSuccess = true
                    }
                }
                if(isSuccess)
                    log("Answers submitted successfully")
                else
                    log("Answers couldn't be submitted ${response.code()}")
            }

            override fun onFailure(call: Call<AnswerResponse>, t: Throwable) {
                t.printStackTrace()
            }

        })

        val animation =  view.findViewById<LottieAnimationView>(R.id.animation)

        animation.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                countDownCompleteListener?.onCountDownCompleted()
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

            override fun onAnimationRepeat(p0: Animator?) {
            }

        })
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

    interface OnCountDownCompleteListener {
        fun onCountDownCompleted()
    }
}