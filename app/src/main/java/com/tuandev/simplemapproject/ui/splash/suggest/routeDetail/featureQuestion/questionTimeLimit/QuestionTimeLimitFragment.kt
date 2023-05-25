package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionTimeLimit

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentTimeLimitQuestionBinding
import com.tuandev.simplemapproject.extension.showToast
import com.tuandev.simplemapproject.extension.toRoundedFloat
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment
import java.util.*

class QuestionTimeLimitFragment : Fragment() {

    private var binding: FragmentTimeLimitQuestionBinding? = null

    companion object {
        fun newInstance() = QuestionTimeLimitFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeLimitQuestionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initView(){
        binding?.run {
            if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 14){
                timePicker.hour = 14
                timePicker.minute = 0
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initListener(){
        binding?.run {
            timePicker.setOnTimeChangedListener { timePicker, hour, _ ->
                if (hour < 14) {
                    timePicker.hour = 14
                }
                if (hour > 22) {
                    timePicker.hour = 22
                }
                if (hour == 22) {
                    timePicker.minute = 0
                }
            }

            btnSubmit.setOnClickListener {

                val selectedTime = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    set(Calendar.MINUTE, timePicker.minute)
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
                    (parentFragment as FeatureQuestionFragment).run {
                        mUserFeature.availableTime = diffInHour
                        submitUserFeature()
                    }
                } else {
                    context?.showToast("Your leave time is passed. Please select another time")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initListener()
    }
}