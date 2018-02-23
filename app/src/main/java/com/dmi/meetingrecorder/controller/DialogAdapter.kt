package com.dmi.meetingrecorder.controller

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.dmi.meetingrecorder.R
import com.dmi.meetingrecorder.extensions.inflate
import com.dmi.meetingrecorder.model.DialogConversation
import kotlinx.android.synthetic.main.row_item.view.*

/**
 * Created by dmi on 14/02/18.
 */
public class DialogAdapter : RecyclerView.Adapter<DialogAdapter.DialogHolder>() {
    lateinit var list: ArrayList<DialogConversation>

    override fun onBindViewHolder(holder: DialogHolder?, position: Int) {
        val dialogConversation = list[position]
        holder?.bindConversation(dialogConversation)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DialogHolder {
        val inflatedView = parent?.inflate(R.layout.row_item, false)
        return DialogHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class DialogHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        lateinit var dialogConversation: DialogConversation
        fun bindConversation(dialogConversation: DialogConversation) {
            this.dialogConversation = dialogConversation
            itemView.textView.text = dialogConversation.dialog
        }
    }
}