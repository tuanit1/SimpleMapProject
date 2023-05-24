package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionTimeType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentTimeTypeQuestionBinding
import com.tuandev.simplemapproject.extension.showToast
import com.tuandev.simplemapproject.extension.toRoundedFloat
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment
import java.util.*

class QuestionTimeTypeFragment : Fragment() {

    private var binding: FragmentTimeTypeQuestionBinding? = null

    companion object {
        fun newInstance() = QuestionTimeTypeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeTypeQuestionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val mainQuestionFragment = parentFragment as FeatureQuestionFragment

        binding?.run {
            btnLimitTime.setOnClickListener {
                mainQuestionFragment.openTimeLimitFragment()
            }

            btnNoLimitTime.setOnClickListener {
                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 22)
                    set(Calendar.MINUTE, 0)
                }.time

                val current = Calendar.getInstance().apply {
                    if (get(Calendar.HOUR_OF_DAY) < 14) {
                        set(Calendar.HOUR_OF_DAY, 14)
                        set(Calendar.MINUTE, 0)
                    }
                }.time

                val diff = selectedTime.time - current.time

                val diffInHour = diff / (1000 * 60 * 60).toFloat().toRoundedFloat(3)

                if (diffInHour > 0) {
                    mainQuestionFragment.run {
                        mUserFeature.availableTime = diffInHour
                        submitUserFeature()
                    }
                } else {
                    context?.showToast("Asia Park closed. Come back tomorrow")
                }
            }
        }
    }
}