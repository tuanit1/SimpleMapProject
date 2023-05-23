package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionGameType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentGameTypeQuestionBinding

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

    }
}