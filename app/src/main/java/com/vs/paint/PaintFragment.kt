package com.vs.paint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class PaintFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val canvas = PaintCanvas(context!!)
        canvas.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        return canvas
    }
}