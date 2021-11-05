package com.perpetio.squat.challenge.adapter.spinnerview

import android.content.Context
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.perpetio.squat.challenge.R
import com.perpetio.squat.challenge.util.ChallengeEnum.Companion.getAllExercises

class ExercisesAdapter(context: Context, private val customAdapterRes: CustomAdapterRes) :
    ArrayAdapter<String>(context, 0, getAllExercises()) {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (convertView == null) {
            view = layoutInflater.inflate(customAdapterRes.mainItemRes, parent, false)
        } else {
            view = convertView
        }
        getItem(position)?.let { country ->
            setItemForExercises(view, country)
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        if (position == 0) {
            view = layoutInflater.inflate(customAdapterRes.headerItemRes, parent, false)
            view.setOnClickListener {
                val root = parent.rootView
                root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
                root.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
            }
        } else {
            view = layoutInflater.inflate(customAdapterRes.dropDownItemRes, parent, false)
            getItem(position)?.let { country ->
                setItemForExercises(view, country)
            }
        }
        return view
    }

    override fun getItem(position: Int): String? {
        if (position == 0) {
            return null
        }
        return super.getItem(position - 1)
    }

    override fun getCount() = super.getCount() + 1
    override fun isEnabled(position: Int) = position != 0

    private fun setItemForExercises(view: View, exercises: String) {
        val tvExercises = view.findViewById<TextView>(R.id.tvExercise)
        tvExercises?.text = exercises
    }

}