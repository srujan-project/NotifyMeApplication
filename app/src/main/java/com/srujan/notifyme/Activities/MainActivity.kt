
package com.srujan.notifyme.Activities

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.srujan.notifyme.Adapters.ApkInformationExtractor
import com.srujan.notifyme.Adapters.AppsAdapter
import com.srujan.notifyme.Classes.AppInfo
import com.srujan.notifyme.Classes.NotifyMe
import com.srujan.notifyme.Constants.Constants
import com.srujan.notifyme.R
import com.srujan.notifyme.R.array.spinner_app_type
import com.srujan.notifyme.databinding.ActivityMainBinding
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.*
import java.lang.ref.WeakReference



class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var adapter: AppsAdapter? = null

    private lateinit var appList: MutableList<AppInfo>
    private lateinit var appListAlternate: MutableList<AppInfo>
    private lateinit var userAppList: MutableList<AppInfo>
    private lateinit var systemAppList: MutableList<AppInfo>

    private var appManOb: NotifyMe? = null

    private lateinit var actvityContext: Context

    private var apkInformationExtractor: ApkInformationExtractor? = null

    private var arrAppType: Array<String>? = null
    private var recyclerViewLayoutManager: RecyclerView.LayoutManager? = null
    private var numberOfUserApps: String? = Constants.STRING_EMPTY
    private var numberOfSystemApps: String? = Constants.STRING_EMPTY

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            DynamicColors.applyToActivityIfAvailable(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        appList = ArrayList()
        arrAppType = arrayOf("User Apps", "System Apps")

        appManOb = NotifyMe()

        userAppList = ArrayList()
        systemAppList = ArrayList()
        appListAlternate = ArrayList()

        actvityContext = this

        val spinnerArrayAdapter = ArrayAdapter.createFromResource(
            actvityContext,
            spinner_app_type,
            R.layout.support_simple_spinner_dropdown_item
        )
        binding.spinnerAppType.adapter = spinnerArrayAdapter

        apkInformationExtractor = ApkInformationExtractor(this)
        recyclerViewLayoutManager = GridLayoutManager(actvityContext, 1)


        getApps(actvityContext)

        binding.spinnerAppType.isSelected = false
        binding.spinnerAppType.isEnabled = false

        binding.aboutLink.movementMethod = LinkMovementMethod.getInstance()
    }


    private fun getApps(context: Context) {
        val contextRef: WeakReference<Context> = WeakReference(context)

        //Coroutine
        launch(Dispatchers.Default) {
            try {
                val context1 = contextRef.get()
                appManOb = ApkInformationExtractor(context1!!).appManagerInitValues()

                if (appManOb != null) {
                    numberOfUserApps = Constants.STRING_EMPTY + appManOb!!.userAppSize
                    numberOfSystemApps = Constants.STRING_EMPTY + appManOb!!.systemAppSize

                    userAppList.addAll(appManOb!!.userApps)
                    systemAppList.addAll(appManOb!!.systemApps)

                    appListAlternate.addAll(userAppList)
                    appList.addAll(userAppList)

                    adapter = AppsAdapter(context1, appListAlternate)
                } else {

                    numberOfUserApps = Constants.STRING_EMPTY + "0"
                    numberOfSystemApps = Constants.STRING_EMPTY + "0"

                    userAppList.clear()
                    systemAppList.clear()
                    appListAlternate.clear()
                    appList.clear()

                    adapter = AppsAdapter(context1, appListAlternate)
                }

                //UI Thread
                withContext(Dispatchers.Main) {

                    binding.recyclerViewApps.layoutManager = recyclerViewLayoutManager

                    if (adapter!!.itemCount > 0) {
                        binding.recyclerViewApps.adapter = adapter
                        val text = "$numberOfUserApps User apps"
                        binding.appCounterAppManager.text = text

                        binding.spinnerAppType.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                                ) {

                                    val selectedItem = parent.getItemAtPosition(position).toString()

                                    if (selectedItem == arrAppType!![0]) {
                                        //User Apps
                                        val textUser = "$numberOfUserApps User apps"
                                        binding.appCounterAppManager.text = textUser
                                        appList.clear()
                                        appList.addAll(userAppList)
                                        adapter?.updateList(userAppList)
                                    } else if (selectedItem == arrAppType!![1]) {
                                        //System Apps
                                        val textSystem = "$numberOfSystemApps System apps"
                                        binding.appCounterAppManager.text = textSystem
                                        appList.clear()
                                        appList.addAll(systemAppList)
                                        adapter?.updateList(systemAppList)
                                    }
                                } // to close the onItemSelected

                                override fun onNothingSelected(parent: AdapterView<*>) {

                                }
                            }

                        binding.spinnerAppType.isEnabled = true
                        binding.spinnerAppType.setSelection(0, true)

                    } else {
                        binding.appCounterAppManager.text = getString(R.string.No_Apps)
                        binding.appsRecyclerLayooutLl.visibility = View.GONE
                        binding.recyclerViewApps.visibility = View.GONE
                        binding.spinnerAppType.isEnabled = false
                        binding.listEmptyAppsAppmanager.visibility = View.VISIBLE
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }
}

