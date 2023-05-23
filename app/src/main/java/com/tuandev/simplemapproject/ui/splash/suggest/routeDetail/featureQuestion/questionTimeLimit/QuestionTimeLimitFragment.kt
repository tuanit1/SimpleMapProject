package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionTimeLimit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentTimeLimitQuestionBinding

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}