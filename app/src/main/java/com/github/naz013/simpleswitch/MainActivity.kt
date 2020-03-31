package com.github.naz013.simpleswitch

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.naz013.animatedswitch.TwoColorSwitch
import com.github.naz013.simpleswitch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.infoButton.setOnClickListener { openGithub() }
        binding.switchView.onStateChangeListener = object : TwoColorSwitch.OnStateChangeListener {
            override fun onStateChanged(isChecked: Boolean) {
                Log.d("MainActivity", "onStateChanged: $isChecked")
            }
        }
    }

    private fun openGithub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/naz013/two-color-switch"))
        try {
            startActivity(intent)
        } catch (e: Exception) {
        }
    }
}
