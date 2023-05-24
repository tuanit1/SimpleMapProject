package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionThrillLevel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.data.repositories.local.ThrillLevelRepository
import com.tuandev.simplemapproject.databinding.FragmentThrillLevelQuestionBinding
import com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.FeatureQuestionFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuestionThrillLevelFragment : Fragment() {

    private var binding: FragmentThrillLevelQuestionBinding? = null

    @Inject
    lateinit var thrillLevelRepository: ThrillLevelRepository

    companion object {
        fun newInstance() = QuestionThrillLevelFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThrillLevelQuestionBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mainQuestionFragment = parentFragment as FeatureQuestionFragment
        binding?.run {
            mainQuestionFragment.run {
                btnExtremeThrilling.setOnClickListener {
                    mUserFeature.maxThrill = thrillLevelRepository.thrillLevelExtreme
                    handleChooseThrillLevel()
                }
                btnHighlyThrilling.setOnClickListener {
                    mUserFeature.maxThrill = thrillLevelRepository.thrillLevelHigh
                    handleChooseThrillLevel()
                }
                btnMediumThrilling.setOnClickListener {
                    mUserFeature.maxThrill = thrillLevelRepository.thrillLevelMedium
                    handleChooseThrillLevel()
                }
            }
        }
    }

    private fun handleChooseThrillLevel() {
        (parentFragment as FeatureQuestionFragment).openQuestionTimeTypeFragment()
    }
}