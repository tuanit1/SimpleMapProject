package com.tuandev.simplemapproject.ui.splash.suggest.routeDetail.featureQuestion.questionTimeType

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tuandev.simplemapproject.databinding.FragmentTimeTypeQuestionBinding

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

    }
}