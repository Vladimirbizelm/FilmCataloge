package com.example.filmcataloge.uiConfiguration.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.filmcataloge.databinding.FragmentProfileBinding
import com.example.filmcataloge.netConfiguration.API
import com.example.filmcataloge.netConfiguration.RetrofitClient
import com.example.filmcataloge.netConfiguration.createSession.LoginRequest
import com.example.filmcataloge.netConfiguration.createSession.SessionRequest
import com.example.filmcataloge.netConfiguration.dataStoreManager.DataStoreManager
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var dataStoreManager: DataStoreManager
    private lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataStoreManager = DataStoreManager(requireContext())
        lifecycleScope.launch {
            val sessionId = dataStoreManager.getSessionId()
            if (sessionId != null) {
                Toast.makeText(requireContext(), "saved it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val api = RetrofitClient.api
        binding.loginInTMDB.setOnClickListener {
            lifecycleScope.launch {
                val token = getRequestToken(api)
                if (token != null) {
                    dataStoreManager.saveRequestToken(token)
                    openAuthUrl(token)
                } else {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.logIntoAccountBtn.setOnClickListener {
            val username = binding.userNameLogin.text.toString()
            val password = binding.password.text.toString()
            lifecycleScope.launch {
                val sessionId = login(username, password, api)
                if (sessionId != null) {
                    dataStoreManager.saveSessionId(sessionId)
                    binding.apply {
                        loginIntoAccountLayout.visibility = View.GONE
                        loginLayout.visibility = View.GONE
                        verifiedLayout.visibility = View.VISIBLE
                        userName.text = username

                    }
                } else {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        lifecycleScope.launch {
            val token = dataStoreManager.getRequestToken()
            val sessionId = dataStoreManager.getSessionId()
            if (token != null && sessionId == null) {
                binding.loginInTMDB.visibility = View.GONE
                binding.loginIntoAccountLayout.visibility = View.VISIBLE
            }
            if (sessionId != null) {
                binding.loginInTMDB.visibility = View.GONE
                binding.loginIntoAccountLayout.visibility = View.GONE
                binding.verifiedLayout.visibility = View.VISIBLE
            }
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }

    private suspend fun login(username: String, password: String, api: API): String? {
        try {
            val requestToken = dataStoreManager.getRequestToken()

            val loginResponse = api.validateLogin(
                loginRequest = LoginRequest(username, password, requestToken!!),
            )
            Log.d("ProfileFragment", "Login response: $loginResponse")
            if (!loginResponse.success) return null

            val sessionResponse = api.createSession(
                sessionRequest = SessionRequest(loginResponse.request_token)
            )

            if (sessionResponse.success) {
                Log.d("ProfileFragment", "Saving session id")
                dataStoreManager.saveSessionId(sessionResponse.session_id)
                return sessionResponse.session_id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private suspend fun getRequestToken(api: API): String? {
        val tokenResponse = api.getRequestToken()
        return if (tokenResponse.success) tokenResponse.request_token else null
    }

    private fun openAuthUrl(token: String) {
        val authUrl = "https://www.themoviedb.org/authenticate/$token?redirect_to=myapp://auth"
        val intent = Intent(Intent.ACTION_VIEW, authUrl.toUri())
        startActivity(intent)
    }
}