package com.thirdworlds.wakeonlan

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.thirdworlds.wakeonlan.data.DatabaseManage
import com.thirdworlds.wakeonlan.data.domain.Link
import com.thirdworlds.wakeonlan.databinding.ActivityMainBinding
import com.thirdworlds.wakeonlan.util.FileUtil
import com.thirdworlds.wakeonlan.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.apache.sshd.common.util.OsUtils
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var importLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        OsUtils.setAndroid(true)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.root.findViewById<MaterialToolbar>(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 注册 ActivityResultLauncher
        importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { importFile(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_remove_all -> {
                val message = SpannableString("您确定要删除所有链接吗?\n删除后不可恢复")
                message.setSpan(ForegroundColorSpan(Color.RED), 0, message.length, 0)

                AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage(message)
                    .setPositiveButton("确认") { _, _ ->
                        val database = DatabaseManage.getDataBase(applicationContext)
                        CoroutineScope(Dispatchers.IO).launch { database.linkDao().deleteAll() }
                    }.setNegativeButton("取消") { _, _ ->
                    }.create().show()
                true
            }

            R.id.action_import -> {
                // 使用 ActivityResultLauncher 打开文件选择器
                importLauncher.launch("application/json") // 你可以根据需要传递 MIME 类型
                true
            }

            R.id.action_export -> {
                val database = DatabaseManage.getDataBase(applicationContext)
                val fileName = "wol_data.json"
                CoroutineScope(Dispatchers.IO).launch {
                    val dataJson = getGson()
                        .toJson(database.linkDao().loadAll()?.first())
                    val fileUri =
                        FileUtil.exportToDownloadFile(applicationContext, dataJson, fileName)
                    if (fileUri == null) {
                        ToastUtil.showToast(
                            applicationContext,
                            getString(R.string.toast_export_fail)
                        )
                    } else {
                        val filePath = FileUtil.getFilePathFromUri(applicationContext, fileUri)
                        ToastUtil.showToast(
                            applicationContext,
                            String.format(getString(R.string.toast_export_success), filePath)
                        )
                    }
                }
                true
            }

            R.id.action_license -> {
                val intent = Intent(this, LicenseActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.action_about -> true
            else -> true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun importFile(uri: Uri) {
        val contentResolver: ContentResolver = contentResolver
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val content = inputStream.bufferedReader().use { it.readText() }
            val linkList: List<Link> = getGson()
                .fromJson(content, object : TypeToken<List<Link>>() {}.type)

            // 处理链接并插入数据库
            linkList.forEach { it.id = 0 }
            CoroutineScope(Dispatchers.IO).launch {
                val database = DatabaseManage.getDataBase(applicationContext)
                database.linkDao().insertAll(*linkList.toTypedArray())
            }
        }
    }

    private fun getGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(
                ZonedDateTime::class.java,
                object : JsonSerializer<ZonedDateTime> {
                    override fun serialize(
                        src: ZonedDateTime?,
                        typeOfSrc: Type?,
                        context: JsonSerializationContext?
                    ): JsonElement {
                        return JsonPrimitive(src?.toInstant()?.toEpochMilli()) // 转换为时间戳
                    }
                }
            )  // 注册序列化器
            .registerTypeAdapter(
                ZonedDateTime::class.java,
                object : JsonDeserializer<ZonedDateTime> {
                    override fun deserialize(
                        json: JsonElement?,
                        typeOfT: Type?,
                        context: JsonDeserializationContext?
                    ): ZonedDateTime? {
                        return json?.asLong?.let {
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC)
                        } // 将时间戳转为 ZonedDateTime
                    }
                }
            )  // 注册反序列化器
            .create()
    }
}