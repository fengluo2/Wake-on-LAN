package com.thirdworlds.wakeonlan

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.thirdworlds.wakeonlan.databinding.ActivityLicenseBinding
import org.apache.sshd.common.util.OsUtils

class LicenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLicenseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OsUtils.setAndroid(true)

        binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = binding.root.findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.activity_license)
        val typedArray = theme.obtainStyledAttributes(intArrayOf(android.R.attr.homeAsUpIndicator))
        val navIcon = typedArray.getDrawable(0)
        typedArray.recycle()
        toolbar.navigationIcon = navIcon
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val ossRecyclerView = binding.ossRecyclerView
        ossRecyclerView.initialize("open_source_licenses.json")  // JSON 文件名与插件生成的报告名称一致
    }
}