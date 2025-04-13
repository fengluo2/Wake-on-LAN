package com.thirdworlds.wakeonlan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.thirdworlds.wakeonlan.data.AppDatabase
import com.thirdworlds.wakeonlan.data.DatabaseManage
import com.thirdworlds.wakeonlan.databinding.FragmentLinkListBinding
import com.thirdworlds.wakeonlan.linkList.AdapterRecyclerViewLinkList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LinkListFragment : Fragment() {

    private var _binding: FragmentLinkListBinding? = null
    private val binding get() = _binding!!

    private var database: AppDatabase? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinkListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

        binding.buttonAddLink.setOnClickListener {
            findNavController().navigate(R.id.action_LinkListFragment_to_LinkEditFragment)
        }
    }

    private fun init() {
        val linkAdapter = AdapterRecyclerViewLinkList(emptyList(), findNavController()) // 先设置空列表
        binding.listLinkMain.layoutManager = LinearLayoutManager(requireContext())
        binding.listLinkMain.adapter = linkAdapter

        database = DatabaseManage.getDataBase(requireContext())

        CoroutineScope(Dispatchers.Main).launch {
            database!!.linkDao().loadAll()?.collect {
                if (isAdded && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    val customAdapter = AdapterRecyclerViewLinkList(it, findNavController())

                    binding.listLinkMain.layoutManager = LinearLayoutManager(requireContext())
                    binding.listLinkMain.adapter = customAdapter
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}