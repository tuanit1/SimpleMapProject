package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.R
import com.tuandev.simplemapproject.activity.MainActivity
import com.tuandev.simplemapproject.data.models.UserFeature
import com.tuandev.simplemapproject.databinding.FragmentFeatureQuestionBinding
import com.tuandev.simplemapproject.extension.openFragment
import com.tuandev.simplemapproject.ui.splash.suggest.SuggestFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionGameType.QuestionGameTypeFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionThrillLevel.QuestionThrillLevelFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionTimeLimit.QuestionTimeLimitFragment
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionTimeType.QuestionTimeTypeFragment

class FeatureQuestionFragment : Fragment() {

    private var binding: FragmentFeatureQuestionBinding? = null
    val mUserFeature = UserFeature()
    var isInitFeature = false

    companion object {
        fun newInstance() = FeatureQuestionFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFeatureQuestionBinding.inflate(inflater, container, false)

        initListener()

        return binding?.root
    }

    private fun initListener() {
        binding?.run {
            ivBack.setOnClickListener {
                (activity as? MainActivity)?.invokeBackPress()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        openQuestionGameTypeFragment()
    }

    private fun openQuestionGameTypeFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = QuestionGameTypeFragment.newInstance()
        )
    }

    fun openQuestionThrillLevelFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = QuestionThrillLevelFragment.newInstance()
        )
    }

    fun openQuestionTimeTypeFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = QuestionTimeTypeFragment.newInstance()
        )
    }

    fun openTimeLimitFragment() {
        openFragment(
            containerId = getContainerId(),
            fragment = QuestionTimeLimitFragment.newInstance()
        )
    }

    fun submitUserFeature() {
        (activity as MainActivity).showConfirmDialog(
            title = "Warning",
            content = "Submit your answer?"
        ) {
            (parentFragment as SuggestFragment).run {
                updateUserFeature(mUserFeature)
                childFragmentManager.popBackStack()

                if (isInitFeature) {
                    showRouteDetailFragment()
                }
            }
        }
    }

    private fun getContainerId() = R.id.container_feature_question
}