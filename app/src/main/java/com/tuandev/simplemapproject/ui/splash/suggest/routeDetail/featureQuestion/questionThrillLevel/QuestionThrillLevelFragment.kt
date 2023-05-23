package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionThrillLevel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentThrillLevelQuestionBinding

class QuestionThrillLevelFragment : Fragment() {

    private var binding: FragmentThrillLevelQuestionBinding? = null

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

    }
}