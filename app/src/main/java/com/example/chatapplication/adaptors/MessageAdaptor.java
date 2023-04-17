package com.example.chatapplication.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.databinding.ItemSendBinding;
import com.example.chatapplication.databinding.ReceivedItemBinding;
import com.example.chatapplication.models.MessageModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdaptor extends RecyclerView.Adapter{

    public static final int ITEM_RECEIVED = 2;
    public static final int ITEM_SEND = 1;
    private Context context;
    private ArrayList<MessageModel> messageModelArrayList;

    public MessageAdaptor() {
    }

    public MessageAdaptor(Context context, ArrayList<MessageModel> messageModelArrayList) {
        this.context = context;
        this.messageModelArrayList = messageModelArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == ITEM_SEND){
            view = LayoutInflater.from(context).inflate(R.layout.item_send,parent,false);
            return new SendViewHolder(view);
        }
        else{
            view = LayoutInflater.from(context).inflate(R.layout.received_item,parent,false);
            return new ReceivedViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {

        MessageModel messageModel = messageModelArrayList.get(position);
        if(messageModel.getSenderId().equals(FirebaseAuth.getInstance().getUid())){
            // if the current view and sender is same
            return ITEM_SEND;
        }
        else{
            // if the current view and sender are different
            return ITEM_RECEIVED;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModelArrayList.get(position);

        if(holder.getClass() == SendViewHolder.class){

            SendViewHolder sendViewHolder = (SendViewHolder) holder;
            sendViewHolder.binding.sendMessage.setText(messageModel.getMessage());

        }else{

            ReceivedViewHolder receivedViewHolder = (ReceivedViewHolder) holder;
            receivedViewHolder.binding.receivedMessage.setText(messageModel.getMessage());

        }

    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }

    public class SendViewHolder extends RecyclerView.ViewHolder{

        ItemSendBinding binding;
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ItemSendBinding.bind(itemView);

        }

    }

    public class ReceivedViewHolder extends RecyclerView.ViewHolder{

        ReceivedItemBinding binding;

        public ReceivedViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = ReceivedItemBinding.bind(itemView);

        }

    }

}
