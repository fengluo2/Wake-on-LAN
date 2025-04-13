package com.thirdworlds.wakeonlan

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.view.allViews
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.thirdworlds.wakeonlan.content.LinkType
import com.thirdworlds.wakeonlan.content.LoginType
import com.thirdworlds.wakeonlan.data.AppDatabase
import com.thirdworlds.wakeonlan.data.DatabaseManage
import com.thirdworlds.wakeonlan.data.domain.Link
import com.thirdworlds.wakeonlan.databinding.FragmentLinkEditBinding
import com.thirdworlds.wakeonlan.type.EncryptedString
import com.thirdworlds.wakeonlan.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

class LinkEditFragment : Fragment() {

    private var _binding: FragmentLinkEditBinding? = null
    private val binding get() = _binding!!

    private var database: AppDatabase? = null
    private var link: Link = Link(createTime = ZonedDateTime.now())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListener()
    }

    @SuppressLint("DiscouragedApi")
    private fun init() {
        unlockValue()

        database = DatabaseManage.getDataBase(requireContext())

        binding.linkEditType.removeAllViews()
        for (type in LinkType.entries) {
            val radioButton = RadioButton(requireContext()) // 创建自定义的布局参数，设置间距

            val params = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)  // 设置左右间距为16dp，top 和 bottom 间距为0dp
            radioButton.setButtonDrawable(R.drawable.radio_selector)
            radioButton.layoutParams = params
            radioButton.text = getString(
                resources.getIdentifier(
                    "link_edit_type_" + type.name.lowercase(),
                    "string",
                    requireContext().packageName
                )
            )
            radioButton.tag = type.value
            radioButton.id = View.generateViewId()  // 动态生成一个唯一的ID
            binding.linkEditType.addView(radioButton)  // 添加到RadioGroup中
        }

        binding.linkEditProxyLoginType.removeAllViews()
        for (type in LoginType.entries) {
            val radioButton = RadioButton(requireContext()) // 创建自定义的布局参数，设置间距

            val params = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 16, 0)  // 设置左右间距为16dp，top 和 bottom 间距为0dp
            radioButton.setButtonDrawable(R.drawable.radio_selector)
            radioButton.layoutParams = params
            radioButton.text = getString(
                resources.getIdentifier(
                    "link_edit_proxy_login_type_" + type.name.lowercase(),
                    "string",
                    requireContext().packageName
                )
            )
            radioButton.tag = type.value
            radioButton.id = View.generateViewId()  // 动态生成一个唯一的ID
            binding.linkEditProxyLoginType.addView(radioButton)  // 添加到RadioGroup中
        }

        var id: Int = -1
        arguments?.let {
            id = it.getInt("id", -1)  // 获取 id 参数，默认值为 -1
        }
        if (id != -1) {
            CoroutineScope(Dispatchers.Main).launch {
                database?.linkDao()?.loadById(id)?.take(1)?.collect { linkDate ->
                    link = linkDate
                    binding.linkEditName.setText(link.name)
                    binding.linkEditType.allViews.forEach {
                        if (it is RadioButton) {
                            it.isChecked = it.tag == link.type
                        }
                    }
                    binding.linkEditProxyAddress.setText(link.proxyAddress?.getData())
                    binding.linkEditProxyPort.setText(link.proxyPort?.toString() ?: "")
                    binding.linkEditProxyLoginType.allViews.forEach {
                        if (it is RadioButton) {
                            it.isChecked = it.tag == link.proxyLoginType
                        }
                    }
                    binding.linkEditProxyLoginUser.setText(link.proxyLoginUser?.getData())
                    binding.linkEditProxyLoginPasswd.setText(link.proxyLoginPasswd?.getData())
                    binding.linkEditProxyLoginPrivate.setText(link.proxyLoginPrivate?.getData())
                    binding.linkEditDirectIp.setText(link.directIp?.getData())
                    binding.linkEditDirectMac.setText(link.directMac?.getData())
                    binding.linkEditRemark.setText(link.remark)
                }
            }
        }
    }

    private fun setListener() {
        binding.linkEditType.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (getRadioButtonTag(radioGroup)) {
                LinkType.DIRECT.value -> binding.linkEditProxyView.visibility = View.GONE
                LinkType.PROXY.value -> binding.linkEditProxyView.visibility = View.VISIBLE
            }
        }

        binding.linkEditProxyLoginType.setOnCheckedChangeListener { radioGroup, checkedId ->
            when (getRadioButtonTag(radioGroup)) {
                LoginType.PASSWD.value -> {
                    binding.linkEditProxyLoginPasswdView.visibility = View.VISIBLE
                    binding.linkEditProxyLoginPrivateView.visibility = View.GONE
                }

                LoginType.PRIVATE.value -> {
                    binding.linkEditProxyLoginPasswdView.visibility = View.GONE
                    binding.linkEditProxyLoginPrivateView.visibility = View.VISIBLE
                }
            }
        }

        binding.buttonSaveLink.setOnClickListener {
            lockValue()

            link.name = binding.linkEditName.text.toString()
            link.type = getRadioButtonTag(binding.linkEditType)
            link.proxyAddress = EncryptedString(binding.linkEditProxyAddress.text.toString())
            link.proxyPort = binding.linkEditProxyPort.text.toString().toIntOrNull()
            link.proxyLoginType = getRadioButtonTag(binding.linkEditProxyLoginType)
            link.proxyLoginUser = EncryptedString(binding.linkEditProxyLoginUser.text.toString())
            link.proxyLoginPasswd = EncryptedString(binding.linkEditProxyLoginPasswd.text.toString())
            link.proxyLoginPrivate = EncryptedString(binding.linkEditProxyLoginPrivate.text.toString())
            link.directIp = EncryptedString(binding.linkEditDirectIp.text.toString())
            link.directMac = EncryptedString(binding.linkEditDirectMac.text.toString())
            link.remark = binding.linkEditRemark.text.toString()
            link.updateTime = ZonedDateTime.now()

            var visibility = true

            if (link.name?.isEmpty() == true) {
                ToastUtil.showToast(requireContext(), "名称不能为空")
                visibility = false
            }
            if (visibility && link.type == LinkType.PROXY.value) {
                if (link.proxyAddress?.getData().isNullOrEmpty()) {
                    ToastUtil.showToast(requireContext(), "代理地址不能为空")
                    visibility = false
                }
                if (visibility && (link.proxyPort == null || link.proxyPort == 0)) {
                    ToastUtil.showToast(requireContext(), "代理端口不能为空")
                    visibility = false
                }
                if (visibility && link.proxyLoginUser?.getData().isNullOrEmpty()) {
                    ToastUtil.showToast(requireContext(), "代理用户名不能为空")
                    visibility = false
                }
                if (visibility && link.proxyLoginType == LoginType.PASSWD.value) {
                    if (link.proxyLoginPasswd?.getData().isNullOrEmpty()) {
                        ToastUtil.showToast(requireContext(), "代理密码不能为空")
                        visibility = false
                    }
                }
                if (visibility && link.proxyLoginType == LoginType.PRIVATE.value) {
                    if (link.proxyLoginPrivate?.getData().isNullOrEmpty()) {
                        ToastUtil.showToast(requireContext(), "私钥不能为空")
                        visibility = false
                    }
                }
            }
            if (visibility && (link.directIp?.getData().isNullOrEmpty())) {
                ToastUtil.showToast(requireContext(), "IP地址不能为空")
                visibility = false
            }
            if (visibility && (link.directMac?.getData().isNullOrEmpty())) {
                ToastUtil.showToast(requireContext(), "MAC地址不能为空")
                visibility = false
            }

            if (!visibility) {
                unlockValue()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                if (link.id == 0) {
                    database?.linkDao()?.insertAll(link)
                } else {
                    database?.linkDao()?.updateAll(link)
                }
                findNavController().navigate(R.id.action_LinkEditFragment_to_LinkListFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun lockValue() {
        binding.linkEditName.isEnabled = false
        binding.linkEditType.isEnabled = false
        binding.linkEditProxyAddress.isEnabled = false
        binding.linkEditProxyPort.isEnabled = false
        binding.linkEditProxyLoginType.isEnabled = false
        binding.linkEditProxyLoginUser.isEnabled = false
        binding.linkEditProxyLoginPasswd.isEnabled = false
        binding.linkEditProxyLoginPrivate.isEnabled = false
        binding.linkEditDirectIp.isEnabled = false
        binding.linkEditDirectMac.isEnabled = false
        binding.linkEditRemark.isEnabled = false
    }

    private fun unlockValue() {
        binding.linkEditName.isEnabled = true
        binding.linkEditType.isEnabled = true
        binding.linkEditProxyAddress.isEnabled = true
        binding.linkEditProxyPort.isEnabled = true
        binding.linkEditProxyLoginType.isEnabled = true
        binding.linkEditProxyLoginUser.isEnabled = true
        binding.linkEditProxyLoginPasswd.isEnabled = true
        binding.linkEditProxyLoginPrivate.isEnabled = true
        binding.linkEditDirectIp.isEnabled = true
        binding.linkEditDirectMac.isEnabled = true
        binding.linkEditRemark.isEnabled = true
    }

    private fun getRadioButtonTag(group: RadioGroup): Int {
        return group.findViewById<RadioButton>(group.checkedRadioButtonId)?.tag as? Int ?: 0
    }
}