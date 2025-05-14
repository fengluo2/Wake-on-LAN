package com.thirdworlds.wakeonlan.linkList

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat.getString
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.thirdworlds.wakeonlan.R
import com.thirdworlds.wakeonlan.data.DatabaseManage
import com.thirdworlds.wakeonlan.data.domain.Link
import com.thirdworlds.wakeonlan.util.SendWolUtil
import com.thirdworlds.wakeonlan.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdapterRecyclerViewLinkList(
    private val itemList: List<Link>,
    private val navController: NavController
) :
    RecyclerView.Adapter<AdapterRecyclerViewLinkList.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var id: Int = 0
        val name = view.findViewById<TextView>(R.id.item_link_list_name)!!
        val buttonEx = view.findViewById<Button>(R.id.item_link_list_button_ex)!!

        val context: Context = view.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_link_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.id = item.id
        holder.name.text = item.name
        holder.buttonEx.setOnClickListener {
            val popup = PopupMenu(it.context, it)
            popup.menuInflater.inflate(R.menu.menu_link_item, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_link_list_edit -> {
                        val bundle = Bundle()
                        bundle.putInt("id", holder.id)
                        navController.navigate(
                            R.id.action_LinkListFragment_to_LinkEditFragment,
                            bundle
                        )
                        true
                    }

                    R.id.action_link_list_remove -> {
                        val appDatabase = DatabaseManage.getDataBase(it.context)
                        CoroutineScope(Dispatchers.IO).launch {
                            appDatabase.linkDao().deleteByIds(item.id)
                        }
                        true
                    }

                    R.id.action_link_list_send -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                SendWolUtil.sendWal(item, holder.context)
                                ToastUtil.showToast(
                                    it.context,
                                    getString(it.context, R.string.toast_send_wal_success)
                                )
                            } catch (e: Exception) {
                                Log.e(this::class.simpleName, "发送 WOL 包失败: ${e.message}", e)
                                ToastUtil.showToast(
                                    it.context,
                                    String.format(
                                        getString(
                                            it.context,
                                            R.string.toast_send_wal_fail
                                        ), e.message
                                    )
                                )
                            }
                        }
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}