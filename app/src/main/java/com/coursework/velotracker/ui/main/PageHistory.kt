package com.coursework.velotracker.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.TrainingStatistics
import com.coursework.velotracker.BL.Model.toString
import com.coursework.velotracker.R
import java.util.*

class PageHistory: Fragment(), AdapterView.OnItemClickListener {
    private var pageNumber: Int = 1
    lateinit var listView: ListView

    public companion object{
        public fun newInstance(page: Int):PageHistory {
            val fragment:PageHistory = PageHistory()
            val args:Bundle = Bundle()
            args.putInt("num", page)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments?.getInt("num")?:1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.history_page, container, false)
        listView = result.findViewById(R.id.listView)
        listView.onItemClickListener = this
        return result
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val intent = Intent(context, LookTraining::class.java)
        intent.putExtra("INDEX", position)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val trainingController = TrainingController(context)
        val trainings: MutableList<TrainingStatistics> = trainingController.loadTrainings()
        val listDate: ArrayList<String> = ArrayList()
        for (training in trainings) {
            listDate.add(training.date.toString(AppConstants.DATE_FORMAT))
        }
        val adapter:MyArrayAdapter = MyArrayAdapter(context!!, android.R.layout.simple_list_item_1, listDate)
        listView.adapter = adapter
    }

    private class MyArrayAdapter(context: Context, resource: Int, objects: ArrayList<String>): ArrayAdapter<String>(context, resource, objects) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById<View>(android.R.id.text1) as TextView
            textView.setTextColor(Color.WHITE)
            textView.setBackgroundColor(Color.BLACK)
            return view
        }

    }

}