package com.coursework.velotracker.ui.fragments.main

import android.R
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.coursework.velotracker.AppConstants
import com.coursework.velotracker.BL.Controller.TrainingController
import com.coursework.velotracker.BL.Model.Extensions.toStringExtension
import com.coursework.velotracker.BL.Model.Training.TrainingStatistics
import com.coursework.velotracker.LookTraining
import com.coursework.velotracker.databinding.HistoryPageBinding
import java.util.*


class PageHistory: Fragment(), AdapterView.OnItemClickListener {

    private var _binding: HistoryPageBinding? = null
    private val binding get() = _binding!!
    private var pageNumber: Int = 1

    companion object{
        fun newInstance(page: Int): PageHistory {
            val fragment = PageHistory()
            val args = Bundle()
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
        _binding = HistoryPageBinding.inflate(inflater, container, false)
        binding.listView.onItemClickListener = this
        return binding.root
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
        trainings.forEach {
            listDate.add(it.date.toStringExtension(AppConstants.DATE_FORMAT))
        }
        val adapter = MyArrayAdapter(
            requireContext(),
            R.layout.simple_list_item_1,
            listDate
        )
        binding.listView.adapter = adapter
    }

    private class MyArrayAdapter(context: Context, resource: Int, objects: ArrayList<String>): ArrayAdapter<String>(
        context,
        resource,
        objects
    ) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            val textView = view.findViewById<View>(android.R.id.text1) as TextView
            val typedValue = TypedValue()
            if (context.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)) {
                val textColor = typedValue.data
                textView.setTextColor(textColor)
            }
            if(context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)){
                val backgroundColor = typedValue.data
                textView.setBackgroundColor(backgroundColor)
            }
            return view
        }
    }
}