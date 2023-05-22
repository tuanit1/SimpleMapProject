package com.tuandev.simplemapproject.ui.splash.login

import androidx.fragment.app.viewModels
import com.tuandev.simplemapproject.base.BaseFragment
import com.tuandev.simplemapproject.base.ViewState
import com.tuandev.simplemapproject.databinding.FragmentLoginBinding


class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel, ViewState>(FragmentLoginBinding::inflate) {

    override val viewModel: LoginViewModel by viewModels()
    override val viewStateObserver: (viewState: ViewState) -> Unit = {}

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }

    override fun initListener() {
        binding?.run {
            tvHome.setOnClickListener {
                parentActivity?.openSuggestRouteFragment()
            }

            tvTool.setOnClickListener {
                parentActivity?.openToolMapFragment()
            }
        }
    }
}