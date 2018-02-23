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
            itemView.textViewSpeaker.text = dialogConversation.speaker
            itemView.textViewMessage.text = dialogConversation.message
            if(dialogConversation.speaker.contains("0")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker1)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message1)
            } else if(dialogConversation.speaker.contains("1")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker2)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message2)
            } else if(dialogConversation.speaker.contains("2")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker3)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message3)
            } else if(dialogConversation.speaker.contains("3")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker4)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message4)
            } else if(dialogConversation.speaker.contains("4")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker5)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message5)
            } else if(dialogConversation.speaker.contains("5")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker6)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message6)
            } else if(dialogConversation.speaker.contains("6")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker7)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message7)
            } else if(dialogConversation.speaker.contains("7")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker8)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message8)
            } else if(dialogConversation.speaker.contains("8")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker9)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message9)
            } else if(dialogConversation.speaker.contains("9")){
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker10)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message10)
            } else{
                itemView.textViewSpeaker.setBackgroundResource(R.drawable.speaker1)
                itemView.textViewMessage.setBackgroundResource(R.drawable.message1)
            }
        }
    }
}