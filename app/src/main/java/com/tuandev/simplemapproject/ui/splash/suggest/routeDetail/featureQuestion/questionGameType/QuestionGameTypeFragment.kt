package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionGameType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentGameTypeQuestionBinding
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment

class QuestionGameTypeFragment : Fragment() {

    private var binding: FragmentGameTypeQuestionBinding? = null

    companion object {
        fun newInstance() = QuestionGameTypeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGameTypeQuestionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val mainQuestionFragment = parentFragment as FeatureQuestionFragment

        binding?.run {
            mainQuestionFragment.run {
                btnThrillGame.setOnClickListener {
                    mUserFeature.isThrillOnly = true
                    mUserFeature.isFamilyOnly = false
                    openQuestionThrillLevelFragment()
                }
                btnFamilyGame.setOnClickListener {
                    mUserFeature.isThrillOnly = false
                    mUserFeature.isFamilyOnly = true
                    openQuestionTimeTypeFragment()
                }
                btnBoth.setOnClickListener {
                    mUserFeature.isThrillOnly = false
                    mUserFeature.isFamilyOnly = false
                    openQuestionThrillLevelFragment()
                }
            }
        }
    }
}